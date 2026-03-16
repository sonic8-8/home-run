package io.ssafy.p.j14c103.homerun.api.service.home.credit;

import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscription;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscriptionRepository;
import io.ssafy.p.j14c103.homerun.domain.pass.UserPassTransaction;
import io.ssafy.p.j14c103.homerun.domain.pass.UserPassTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransaction;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * FICO 프레임워크 기반 자체 CSS (Credit Scoring System).
 * <p>
 * 5대 요소 비중 (FICO 공식):
 * - Payment History: 35% (350점)
 * - Amounts Owed:    30% (300점)
 * - Length of History:15% (150점)
 * - Credit Mix:      10% (100점)
 * - New Credit:      10% (100점)
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FicoCreditScoringService implements CreditScoreProvider {

    private final PassSubscriptionRepository passSubscriptionRepository;
    private final UserPassTransactionRepository passTransactionRepository;
    private final SeedmoneyAccountRepository seedmoneyAccountRepository;
    private final SeedmoneyTransactionRepository seedmoneyTransactionRepository;

    @Override
    public CreditScore calculate(final Long userId) {
        int paymentHistory = calcPaymentHistory(userId);
        int amountsOwed = calcAmountsOwed(userId);
        int creditLength = calcCreditLength(userId);
        int creditMix = calcCreditMix(userId);
        int newCredit = calcNewCredit(userId);

        CreditScore result = CreditScore.of(paymentHistory, amountsOwed, creditLength, creditMix, newCredit);
        log.info("CSS 점수 산출 [userId={}]: {} ({}등급 {})", userId, result.getScore(), result.getGrade(), result.getGradeLabel());
        return result;
    }

    // ──────────────────────────────────────────────────
    // 1. Payment History (350점) — PASS 저축 이행률
    //    미가입 = 만점 (빚 없음 = 연체 없음)
    // ──────────────────────────────────────────────────
    private int calcPaymentHistory(Long userId) {
        List<PassSubscription> activeSubscriptions =
                passSubscriptionRepository.findByUserIdAndIsActiveTrue(userId);

        if (activeSubscriptions.isEmpty()) {
            return 350; // PASS 미가입 = 만점
        }

        // 최근 30일 저축 거래 수 vs 기대 거래 수 (구독 수 * 30)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long actualSaves = 0;
        long expectedSaves = activeSubscriptions.size() * 30L;

        for (PassSubscription sub : activeSubscriptions) {
            List<UserPassTransaction> txns = passTransactionRepository
                    .findBySubscriptionIdAndTransactionDateAfter(sub.getId(), thirtyDaysAgo.toString());
            actualSaves += txns.size();
        }

        if (expectedSaves == 0) {
            return 350;
        }

        double fulfillmentRate = (double) actualSaves / expectedSaves;
        if (fulfillmentRate >= 0.90) return 350;
        if (fulfillmentRate >= 0.75) return 280;
        if (fulfillmentRate >= 0.60) return 210;
        if (fulfillmentRate >= 0.40) return 140;
        return 70;
    }

    // ──────────────────────────────────────────────────
    // 2. Amounts Owed (300점) — 시드머니 출금/입금 비율
    //    소비율이 낮을수록 높은 점수
    // ──────────────────────────────────────────────────
    private int calcAmountsOwed(Long userId) {
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);

        List<SeedmoneyTransaction> deposits = seedmoneyTransactionRepository
                .findByUserIdAndTransactionTypeAndCreatedAtAfter(userId, "DEPOSIT", threeMonthsAgo);
        List<SeedmoneyTransaction> withdrawals = seedmoneyTransactionRepository
                .findByUserIdAndTransactionTypeAndCreatedAtAfter(userId, "TRANSFER", threeMonthsAgo);

        long totalDeposit = deposits.stream().mapToLong(t -> Math.abs(t.getAmount().longValue())).sum();
        long totalWithdrawal = withdrawals.stream().mapToLong(t -> Math.abs(t.getAmount().longValue())).sum();

        if (totalDeposit == 0) {
            return totalWithdrawal == 0 ? 300 : 30; // 입금 0인데 출금만 있으면 적자
        }

        double spendingRatio = (double) totalWithdrawal / totalDeposit;
        if (spendingRatio <= 0.30) return 300;
        if (spendingRatio <= 0.50) return 240;
        if (spendingRatio <= 0.70) return 180;
        if (spendingRatio <= 0.85) return 120;
        if (spendingRatio <= 1.00) return 60;
        return 30;
    }

    // ──────────────────────────────────────────────────
    // 3. Length of Credit History (150점) — 서비스 이용 기간
    //    시드머니 계좌 개설일 기준 (User에 createdAt 없으므로)
    // ──────────────────────────────────────────────────
    private int calcCreditLength(Long userId) {
        return seedmoneyAccountRepository.findByUserId(userId)
                .map(account -> {
                    long days = ChronoUnit.DAYS.between(account.getUpdatedAt(), LocalDateTime.now());
                    // updatedAt을 개설일 대용으로 사용
                    if (days >= 180) return 150;
                    if (days >= 90) return 120;
                    if (days >= 30) return 90;
                    if (days >= 14) return 60;
                    return 30;
                })
                .orElse(30); // 계좌 없으면 최소 점수
    }

    // ──────────────────────────────────────────────────
    // 4. Credit Mix (100점) — 보유 금융 상품 다양성
    //    시드머니 계좌 + PASS 구독 수
    // ──────────────────────────────────────────────────
    private int calcCreditMix(Long userId) {
        int score = 0;

        // 시드머니 계좌 보유
        boolean hasSeedmoney = seedmoneyAccountRepository.findByUserId(userId).isPresent();
        if (hasSeedmoney) score += 40;

        // PASS 구독 수
        List<PassSubscription> activeSubs =
                passSubscriptionRepository.findByUserIdAndIsActiveTrue(userId);
        int passCount = activeSubs.size();

        if (passCount >= 3) return Math.min(score + 60, 100);
        if (passCount >= 2) return Math.min(score + 40, 100);
        if (passCount >= 1) return Math.min(score + 20, 100);

        return Math.min(score, 100);
    }

    // ──────────────────────────────────────────────────
    // 5. New Credit (100점) — 최근 30일 PASS 신규 구독 수
    //    적을수록 안정적 → 높은 점수
    // ──────────────────────────────────────────────────
    private int calcNewCredit(Long userId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        List<PassSubscription> allSubs =
                passSubscriptionRepository.findByUserIdAndIsActiveTrue(userId);

        long recentNewSubs = allSubs.stream()
                .filter(sub -> sub.getSubscribedAt() != null && sub.getSubscribedAt().isAfter(thirtyDaysAgo))
                .count();

        if (recentNewSubs <= 1) return 100;
        if (recentNewSubs == 2) return 80;
        if (recentNewSubs == 3) return 60;
        return 40;
    }
}
