package io.ssafy.p.j14c103.homerun.api.service.pass;

import io.ssafy.p.j14c103.homerun.api.controller.pass.request.PassSaveRequest;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassHistoryResponse;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassSaveResponse;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassWidgetResponse;
import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyDemandDepositClient;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscription;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscriptionRepository;
import io.ssafy.p.j14c103.homerun.domain.pass.UserPassTransaction;
import io.ssafy.p.j14c103.homerun.domain.pass.UserPassTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyAccount;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyAccountRepository;
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
    private final SeedmoneyAccountRepository seedmoneyAccountRepository;
    private final SeedmoneyTransactionRepository seedmoneyTransactionRepository;
    private final UserPassTransactionRepository userPassTransactionRepository;
    private final SsafyDemandDepositClient demandDepositClient;

    @Transactional
    public PassSaveResponse save(final PassSaveRequest request) {
        final PassSubscription subscription = passSubscriptionRepository.findById(request.getSubscriptionId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 구독입니다."));

        if (!subscription.getIsActive()) {
            throw new IllegalStateException("해지된 구독에서는 저축할 수 없습니다.");
        }

        final SeedmoneyAccount seedmoneyAccount = seedmoneyAccountRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("시드머니 계좌가 없습니다. 먼저 계좌를 개설해주세요."));

        final int amount = subscription.getSavingAmount();

        demandDepositClient.transferAccount(
                request.getUserKey(),
                seedmoneyAccount.getAccountNumber(),
                request.getSourceAccountId(),
                amount);

        // 시드머니 거래내역 기록
        final SeedmoneyTransaction transaction = SeedmoneyTransaction.createSave(
                request.getUserId(),
                subscription.getPassProduct().getId(),
                amount);
        seedmoneyTransactionRepository.save(transaction);

        // 유저패스거래내역 기록
        final UserPassTransaction passTransaction = UserPassTransaction.create(
                subscription.getId(), amount);
        userPassTransactionRepository.save(passTransaction);

        // 총 저축 합계 계산
        final int totalSaved = calculateTotalSaved(request.getUserId());

        // 잔액 조회
        final int remainingBalance = fetchRealTimeBalance(request.getUserKey(), seedmoneyAccount.getAccountNumber());
        seedmoneyAccount.updateBalance(remainingBalance);

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

    @SuppressWarnings("unchecked")
    private int fetchRealTimeBalance(final String userKey, final String accountNo) {
        final List<Map<String, Object>> accounts = demandDepositClient.inquireAccountList(userKey);
        return accounts.stream()
                .filter(account -> accountNo.equals(account.get("accountNo")))
                .findFirst()
                .map(account -> Integer.parseInt(String.valueOf(account.get("accountBalance"))))
                .orElse(0);
    }
}
