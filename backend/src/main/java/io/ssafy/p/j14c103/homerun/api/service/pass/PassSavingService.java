package io.ssafy.p.j14c103.homerun.api.service.pass;

import io.ssafy.p.j14c103.homerun.api.service.pass.request.PassSaveServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassHistoryResponse;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassSaveResponse;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassWidgetResponse;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.api.service.financial.UserFinancialSummaryService;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyDemandDepositClient;
import io.ssafy.p.j14c103.homerun.domain.account.AccountTransactionType;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccount;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransaction;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscription;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscriptionRepository;
import io.ssafy.p.j14c103.homerun.domain.pass.UserPassTransaction;
import io.ssafy.p.j14c103.homerun.domain.pass.UserPassTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransaction;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PassSavingService {

    private static final int DEFAULT_WEEKLY_GOAL = 50000;

    private final PassSubscriptionRepository passSubscriptionRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserAccountTransactionRepository userAccountTransactionRepository;
    private final SeedmoneyTransactionRepository seedmoneyTransactionRepository;
    private final UserPassTransactionRepository userPassTransactionRepository;
    private final SsafyDemandDepositClient demandDepositClient;
    private final UserAuthContextService userAuthContextService;
    private final UserFinancialSummaryService userFinancialSummaryService;

    @Transactional
    public PassSaveResponse save(final Long userId, final PassSaveServiceRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        final PassSubscription subscription = passSubscriptionRepository.findByIdAndUserId(request.getSubscriptionId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 구독입니다."));

        if (!subscription.getIsActive()) {
            throw new IllegalStateException("해지된 구독에서는 저축할 수 없습니다.");
        }

        final UserAccount mainAccount = userAccountRepository.findByUserIdAndAccountType(userId, AccountType.MAIN)
                .orElseThrow(() -> new IllegalArgumentException("주계좌가 없습니다."));
        final UserAccount seedmoneyAccount = userAccountRepository.findByUserIdAndAccountType(userId, AccountType.SEEDMONEY)
                .orElseThrow(() -> new IllegalArgumentException("저축 계좌가 없습니다. 먼저 계좌를 개설해주세요."));
        final String userKey = userAuthContextService.getRequiredSsafyUserKey(userId);

        final int amount = subscription.getSavingAmount();

        demandDepositClient.transferAccount(
                userKey,
                seedmoneyAccount.getAccountNumber(),
                mainAccount.getAccountNumber(),
                amount);

        final UserAccountTransaction saveOutTransaction = UserAccountTransaction.create(
                userId,
                AccountType.MAIN,
                subscription.getId(),
                AccountTransactionType.PASS_SAVE_OUT,
                amount,
                seedmoneyAccount.getAccountNumber());
        userAccountTransactionRepository.save(saveOutTransaction);

        final UserAccountTransaction saveInTransaction = UserAccountTransaction.create(
                userId,
                AccountType.SEEDMONEY,
                subscription.getId(),
                AccountTransactionType.PASS_SAVE_IN,
                amount,
                mainAccount.getAccountNumber());
        userAccountTransactionRepository.save(saveInTransaction);

        // 시드머니 거래내역 기록
        final SeedmoneyTransaction transaction = SeedmoneyTransaction.createSave(
                userId,
                subscription.getId(),
                amount);
        seedmoneyTransactionRepository.save(transaction);

        // 유저패스거래내역 기록
        final UserPassTransaction passTransaction = UserPassTransaction.create(
                subscription.getId(), amount);
        userPassTransactionRepository.save(passTransaction);

        // 총 저축 합계 계산
        final int totalSaved = calculateTotalSaved(userId);

        // 잔액 조회
        final List<Map<String, Object>> accounts = demandDepositClient.inquireAccountList(userKey);
        final int mainBalance = fetchRealTimeBalance(accounts, mainAccount.getAccountNumber());
        final int remainingBalance = fetchRealTimeBalance(accounts, seedmoneyAccount.getAccountNumber());
        mainAccount.updateBalance(mainBalance);
        seedmoneyAccount.updateBalance(remainingBalance);
        userFinancialSummaryService.getSummary(userId);

        return PassSaveResponse.of(amount, totalSaved, remainingBalance);
    }

    public PassWidgetResponse getWidget(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        final LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        final LocalDateTime weekStart = LocalDate.now()
                .with(DayOfWeek.MONDAY)
                .atStartOfDay();

        final int todaySaved = sumSavings(userId, todayStart);
        final int weeklySaved = sumSavings(userId, weekStart);

        return PassWidgetResponse.of(todaySaved, weeklySaved, DEFAULT_WEEKLY_GOAL);
    }

    public Page<PassHistoryResponse> getHistory(final Long userId, final int page, final int size) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        return seedmoneyTransactionRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(PassHistoryResponse::from);
    }

    private int sumSavings(final Long userId, final LocalDateTime after) {
        final List<SeedmoneyTransaction> transactions = seedmoneyTransactionRepository
                .findByUserIdAndTransactionTypeAndCreatedAtAfter(userId, "SAVE", after);

        return transactions.stream()
                .mapToInt(SeedmoneyTransaction::getAmount)
                .sum();
    }

    private int calculateTotalSaved(final Long userId) {
        return sumSavings(userId, LocalDateTime.MIN);
    }

    private int fetchRealTimeBalance(final List<Map<String, Object>> accounts, final String accountNo) {
        return accounts.stream()
                .filter(account -> accountNo.equals(account.get("accountNo")))
                .findFirst()
                .map(account -> Integer.parseInt(String.valueOf(account.get("accountBalance"))))
                .orElse(0);
    }
}
