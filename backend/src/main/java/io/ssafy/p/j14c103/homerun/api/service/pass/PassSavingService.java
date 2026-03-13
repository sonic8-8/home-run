package io.ssafy.p.j14c103.homerun.api.service.pass;

import io.ssafy.p.j14c103.homerun.api.controller.pass.request.PassSaveRequest;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassHistoryResponse;
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
    public void save(final PassSaveRequest request) {
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
                seedmoneyAccount.getMaskedAccountNo(),
                subscription.getSourceAccountNo(),
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
    }

    public PassWidgetResponse getWidget(final Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        final LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        final LocalDateTime weekStart = LocalDate.now()
                .with(DayOfWeek.MONDAY)
                .atStartOfDay();

        final int todaySaving = sumSavings(userId, todayStart);
        final int weekSaving = sumSavings(userId, weekStart);

        return PassWidgetResponse.of(todaySaving, weekSaving, DEFAULT_WEEKLY_GOAL);
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
}
