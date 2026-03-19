package io.ssafy.p.j14c103.homerun.api.service.home.credit;

import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyCreditCardClient;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscription;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscriptionRepository;
import io.ssafy.p.j14c103.homerun.domain.pass.UserPassTransaction;
import io.ssafy.p.j14c103.homerun.domain.pass.UserPassTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransaction;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransactionRepository;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContext;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * FICO 프레임워크 기반 자체 CSS (Credit Scoring System).
 * <p>
 * 5대 요소 비중 (FICO 공식):
 * - Payment History: 35% (350점) — 금융 상품 이행 이력
 * - Amounts Owed:    30% (300점) — 부채/자산 비율
 * - Length of History:15% (150점) — 서비스 이용 기간
 * - Credit Mix:      10% (100점) — 보유 금융 상품 다양성
 * - New Credit:      10% (100점) — 최근 신규 금융 활동
 * </p>
 * <p>
 * 2분기 로직:
 * - 분기 1: 금융 이력(카드/PASS)이 있는 사용자 → 실제 이행률 기반 산출
 * - 분기 2: 금융 이력 없는 사용자 → 기본 점수 부여 (증명 불가)
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
    private final SsafyCreditCardClient creditCardClient;
    private final UserAuthContextService userAuthContextService;

    @Override
    public CreditScore calculate(final Long userId) {
        final boolean hasCreditHistory = hasCreditHistory(userId);

        final int paymentHistory = calcPaymentHistory(userId, hasCreditHistory);
        final int amountsOwed = calcAmountsOwed(userId, hasCreditHistory);
        final int creditLength = calcCreditLength(userId);
        final int creditMix = calcCreditMix(userId);
        final int newCredit = calcNewCredit(userId);

        final CreditScore result = CreditScore.of(
                paymentHistory, amountsOwed, creditLength, creditMix, newCredit);
        log.info("CSS 점수 산출 [userId={}, 이력={}]: {} ({}등급 {})",
                userId, hasCreditHistory, result.getScore(), result.getGrade(), result.getGradeLabel());
        return result;
    }

    /**
     * 금융 이력 존재 여부 판단.
     * PASS 구독 또는 카드 보유 시 이력 있음.
     */
    private boolean hasCreditHistory(final Long userId) {
        final List<PassSubscription> activeSubs =
                passSubscriptionRepository.findByUserIdAndIsActiveTrue(userId);
        if (!activeSubs.isEmpty()) {
            return true;
        }

        return getCardCount(userId) > 0;
    }

    // ──────────────────────────────────────────────────
    // 1. Payment History (350점) — 금융 상품 이행 이력
    //    이력 있음: PASS 저축 이행률 + 카드 거래 이력
    //    이력 없음: 250점 (상환 증명 불가, 연체도 없음)
    // ──────────────────────────────────────────────────
    private int calcPaymentHistory(final Long userId, final boolean hasCreditHistory) {
        if (!hasCreditHistory) {
            return 250; // 이력 없음 → 기본 점수
        }

        int score = 0;
        int factors = 0;

        // PASS 저축 이행률
        final List<PassSubscription> activeSubscriptions =
                passSubscriptionRepository.findByUserIdAndIsActiveTrue(userId);
        if (!activeSubscriptions.isEmpty()) {
            final int passScore = calcPassFulfillment(activeSubscriptions);
            score += passScore;
            factors++;
        }

        // 카드 거래 이력 (보유 카드가 있으면 결제 활동 여부 확인)
        final int cardActivityScore = calcCardActivity(userId);
        if (cardActivityScore >= 0) {
            score += cardActivityScore;
            factors++;
        }

        if (factors == 0) {
            return 250;
        }

        return Math.min(score / factors, 350);
    }

    /**
     * PASS 저축 이행률 점수 (350점 기준).
     */
    private int calcPassFulfillment(final List<PassSubscription> activeSubscriptions) {
        final LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long actualSaves = 0;
        final long expectedSaves = activeSubscriptions.size() * 30L;

        for (final PassSubscription sub : activeSubscriptions) {
            final List<UserPassTransaction> txns = passTransactionRepository
                    .findBySubscriptionIdAndTransactionDateAfter(sub.getId(), thirtyDaysAgo.toString());
            actualSaves += txns.size();
        }

        if (expectedSaves == 0) {
            return 350;
        }

        final double fulfillmentRate = (double) actualSaves / expectedSaves;
        if (fulfillmentRate >= 0.90) return 350;
        if (fulfillmentRate >= 0.75) return 280;
        if (fulfillmentRate >= 0.60) return 210;
        if (fulfillmentRate >= 0.40) return 140;
        return 70;
    }

    /**
     * 카드 거래 활동 점수 (350점 기준).
     * 카드 미보유 시 -1 반환 (factors에서 제외).
     */
    private int calcCardActivity(final Long userId) {
        try {
            final UserAuthContext ctx = userAuthContextService.getContext(userId);
            if (!ctx.hasSsafyUserKey()) {
                return -1;
            }

            final List<Map<String, Object>> cards = creditCardClient.inquireSignUpCreditCardList(ctx.ssafyUserKey());
            if (cards.isEmpty()) {
                return -1;
            }

            // 카드 보유 + 최근 거래가 있으면 높은 점수
            boolean hasRecentTransaction = false;
            for (final Map<String, Object> card : cards) {
                final String cardNo = (String) card.get("cardNo");
                final String cvc = (String) card.get("cvc");
                if (cardNo == null || cvc == null) continue;

                final String endDate = LocalDateTime.now().toLocalDate().toString().replace("-", "");
                final String startDate = LocalDateTime.now().minusDays(30).toLocalDate().toString().replace("-", "");

                final List<Map<String, Object>> txns = creditCardClient.inquireCreditCardTransactionList(
                        ctx.ssafyUserKey(), cardNo, cvc, startDate, endDate);
                if (!txns.isEmpty()) {
                    hasRecentTransaction = true;
                    break;
                }
            }

            return hasRecentTransaction ? 350 : 210; // 카드 보유 + 거래 활동 유무
        } catch (final Exception e) {
            log.debug("카드 활동 조회 실패, Payment History에서 카드 제외", e);
            return -1;
        }
    }

    // ──────────────────────────────────────────────────
    // 2. Amounts Owed (300점) — 부채/소비 비율
    //    이력 있음: 시드머니 출금/입금 비율 (소비율 기반)
    //    이력 없음: 300점 (부채 없음 = 가용 신용 100%)
    // ──────────────────────────────────────────────────
    private int calcAmountsOwed(final Long userId, final boolean hasCreditHistory) {
        if (!hasCreditHistory) {
            return 300; // 이력 없음 → 만점 (부채 없음)
        }

        final LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);

        final List<SeedmoneyTransaction> deposits = seedmoneyTransactionRepository
                .findByUserIdAndTransactionTypeAndCreatedAtAfter(userId, "DEPOSIT", threeMonthsAgo);
        final List<SeedmoneyTransaction> withdrawals = seedmoneyTransactionRepository
                .findByUserIdAndTransactionTypeAndCreatedAtAfter(userId, "TRANSFER", threeMonthsAgo);

        final long totalDeposit = deposits.stream()
                .mapToLong(t -> Math.abs(t.getAmount().longValue())).sum();
        final long totalWithdrawal = withdrawals.stream()
                .mapToLong(t -> Math.abs(t.getAmount().longValue())).sum();

        if (totalDeposit == 0) {
            return totalWithdrawal == 0 ? 300 : 30;
        }

        final double spendingRatio = (double) totalWithdrawal / totalDeposit;
        if (spendingRatio <= 0.30) return 300;
        if (spendingRatio <= 0.50) return 240;
        if (spendingRatio <= 0.70) return 180;
        if (spendingRatio <= 0.85) return 120;
        if (spendingRatio <= 1.00) return 60;
        return 30;
    }

    // ──────────────────────────────────────────────────
    // 3. Length of Credit History (150점) — 서비스 이용 기간
    //    시드머니 계좌 개설일 기준 (공통)
    // ──────────────────────────────────────────────────
    private int calcCreditLength(final Long userId) {
        return seedmoneyAccountRepository.findByUserId(userId)
                .map(account -> {
                    final long days = ChronoUnit.DAYS.between(account.getUpdatedAt(), LocalDateTime.now());
                    if (days >= 180) return 150;
                    if (days >= 90) return 120;
                    if (days >= 30) return 90;
                    if (days >= 14) return 60;
                    return 30;
                })
                .orElse(30);
    }

    // ──────────────────────────────────────────────────
    // 4. Credit Mix (100점) — 보유 금융 상품 다양성
    //    시드머니 / PASS / 카드 보유 수
    //    이력 없음: 시드머니만 → 40점
    // ──────────────────────────────────────────────────
    private int calcCreditMix(final Long userId) {
        int productTypes = 0;

        // 시드머니 계좌
        if (seedmoneyAccountRepository.findByUserId(userId).isPresent()) {
            productTypes++;
        }

        // PASS 구독
        final List<PassSubscription> activeSubs =
                passSubscriptionRepository.findByUserIdAndIsActiveTrue(userId);
        if (!activeSubs.isEmpty()) {
            productTypes++;
        }

        // 카드 보유
        if (getCardCount(userId) > 0) {
            productTypes++;
        }

        return switch (productTypes) {
            case 3 -> 100;
            case 2 -> 70;
            case 1 -> 40;
            default -> 20;
        };
    }

    // ──────────────────────────────────────────────────
    // 5. New Credit (100점) — 최근 신규 금융 활동
    //    최근 30일 PASS 신규 구독 수 + 카드 등록 수
    //    적을수록 안정적 → 높은 점수
    //    이력 없음: 100점 (신청 없음 = 안정)
    // ──────────────────────────────────────────────────
    private int calcNewCredit(final Long userId) {
        final LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        // PASS 최근 구독
        final List<PassSubscription> allSubs =
                passSubscriptionRepository.findByUserIdAndIsActiveTrue(userId);
        final long recentNewSubs = allSubs.stream()
                .filter(sub -> sub.getSubscribedAt() != null && sub.getSubscribedAt().isAfter(thirtyDaysAgo))
                .count();

        if (recentNewSubs <= 1) return 100;
        if (recentNewSubs == 2) return 80;
        if (recentNewSubs == 3) return 60;
        return 40;
    }

    /**
     * SSAFY 금융망에서 카드 보유 수 조회. 실패 시 0 반환.
     */
    private int getCardCount(final Long userId) {
        try {
            final UserAuthContext ctx = userAuthContextService.getContext(userId);
            if (!ctx.hasSsafyUserKey()) {
                return 0;
            }
            return creditCardClient.inquireSignUpCreditCardList(ctx.ssafyUserKey()).size();
        } catch (final Exception e) {
            log.debug("카드 보유 조회 실패", e);
            return 0;
        }
    }
}
