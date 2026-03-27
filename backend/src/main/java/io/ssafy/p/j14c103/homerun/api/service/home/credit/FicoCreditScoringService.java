package io.ssafy.p.j14c103.homerun.api.service.home.credit;

import io.ssafy.p.j14c103.homerun.client.ssafy.SsafyCreditCardClient;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscription;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscriptionRepository;
import io.ssafy.p.j14c103.homerun.domain.pass.UserPassTransaction;
import io.ssafy.p.j14c103.homerun.domain.pass.UserPassTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransaction;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetCardSpendRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetDepositRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetLoanRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetOtherIncomeRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetProfile;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetProfileRepository;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContext;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
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
 * 비례식 산출, 보수적 기준.
 * 모든 금융 활동 기록은 1년(365일) 기반.
 * </p>
 * <p>
 * 데이터 인식:
 * - 시드머니 거래유형: SAVE(PASS 저축), DEPOSIT(입금), TRANSFER(출금/송금)
 * - 카드 거래: SSAFY 금융망 inquireCreditCardTransactionList
 * - PASS 이행: UserPassTransaction (구독별 저축 거래)
 * </p>
 * <p>
 * 2분기 로직 (보수적):
 * - 이력 있음: 실제 금융 활동 기반 비례식 산출
 * - 이력 없음: 보수적 기본 점수 (증명 불가 = 낮게)
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FicoCreditScoringService implements CreditScoreProvider {

    /** 모든 금융 활동 기준 기간: 1년 */
    private static final int BASE_PERIOD_DAYS = 365;

    /** New Credit 기준 기간: 6개월 */
    private static final int NEW_CREDIT_PERIOD_DAYS = 180;

    /** Credit Length 만점 기준: 1년 */
    private static final int LENGTH_MAX_DAYS = 365;

    // Payment History
    private static final int MIN_PAYMENT = 35;
    private static final int MAX_PAYMENT = 350;
    private static final int NO_HISTORY_PAYMENT = 175; // 보수적: 만점의 50%

    // Amounts Owed
    private static final int MIN_OWED = 30;
    private static final int MAX_OWED = 300;
    private static final int NO_HISTORY_OWED = 200; // 보수적: 만점의 67%

    // Credit Length
    private static final int MIN_LENGTH = 15;
    private static final int MAX_LENGTH = 150;

    // Credit Mix
    private static final int MIN_MIX = 20;
    private static final int MAX_MIX = 100;

    // New Credit
    private static final int MIN_NEW_CREDIT = 20;
    private static final int MAX_NEW_CREDIT = 100;

    private final PassSubscriptionRepository passSubscriptionRepository;
    private final UserPassTransactionRepository passTransactionRepository;
    private final SeedmoneyAccountRepository seedmoneyAccountRepository;
    private final SeedmoneyTransactionRepository seedmoneyTransactionRepository;
    private final SsafyCreditCardClient creditCardClient;
    private final UserAuthContextService userAuthContextService;
    private final UserAssetProfileRepository userAssetProfileRepository;
    private final UserAssetDepositRepository userAssetDepositRepository;
    private final UserAssetLoanRepository userAssetLoanRepository;
    private final UserAssetOtherIncomeRepository userAssetOtherIncomeRepository;
    private final UserAssetCardSpendRepository userAssetCardSpendRepository;

    @Override
    public CreditScore calculate(final Long userId) {
        final boolean hasCreditHistory = hasCreditHistory(userId);
        final UserAssetProfile assetProfile = userAssetProfileRepository.findById(userId).orElse(null);

        if (!hasCreditHistory && assetProfile != null) {
            return calculateFromAssetProfile(
                    assetProfile,
                    totalDepositAmount(userId),
                    totalLoanAmount(userId),
                    totalOtherIncomeAmount(userId),
                    totalCardSpendAmount(userId),
                    hasCardSpend(userId)
            );
        }

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
     * 금융 이력 존재 여부.
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
    // 1. Payment History (350점)
    //    이력 있음: PASS 1년 이행률 + 카드 1년 거래 활동 (비례, 가중 평균)
    //    이력 없음: 175점 (보수적 — 증명 불가)
    // ──────────────────────────────────────────────────
    private int calcPaymentHistory(final Long userId, final boolean hasCreditHistory) {
        if (!hasCreditHistory) {
            return NO_HISTORY_PAYMENT;
        }

        int totalScore = 0;
        int factors = 0;

        // PASS 1년 이행률 → 비례 점수
        final List<PassSubscription> activeSubscriptions =
                passSubscriptionRepository.findByUserIdAndIsActiveTrue(userId);
        if (!activeSubscriptions.isEmpty()) {
            final double fulfillmentRate = calcPassFulfillmentRate(activeSubscriptions);
            totalScore += proportional(fulfillmentRate, MIN_PAYMENT, MAX_PAYMENT);
            factors++;
        }

        // 카드 1년 거래 활동
        final int cardScore = calcCardActivity(userId);
        if (cardScore >= 0) {
            totalScore += cardScore;
            factors++;
        }

        if (factors == 0) {
            return NO_HISTORY_PAYMENT;
        }

        return clamp(totalScore / factors, MIN_PAYMENT, MAX_PAYMENT);
    }

    /**
     * PASS 저축 이행률 (1년 기준, 0.0 ~ 1.0).
     * 인식 방법: UserPassTransaction 테이블에서 구독별 저축 거래 수를 카운트.
     */
    private double calcPassFulfillmentRate(final List<PassSubscription> activeSubscriptions) {
        final LocalDateTime oneYearAgo = LocalDateTime.now().minusDays(BASE_PERIOD_DAYS);
        long actualSaves = 0;
        final long expectedSaves = activeSubscriptions.size() * (long) BASE_PERIOD_DAYS;

        for (final PassSubscription sub : activeSubscriptions) {
            final List<UserPassTransaction> txns = passTransactionRepository
                    .findBySubscriptionIdAndTransactionDateAfter(sub.getId(), oneYearAgo.toString());
            actualSaves += txns.size();
        }

        if (expectedSaves == 0) {
            return 1.0;
        }

        return Math.min((double) actualSaves / expectedSaves, 1.0);
    }

    /**
     * 카드 거래 활동 점수 (1년 기준).
     * 인식 방법: SSAFY 금융망 inquireCreditCardTransactionList API.
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

            final String endDate = LocalDateTime.now().toLocalDate().toString().replace("-", "");
            final String startDate = LocalDateTime.now().minusDays(BASE_PERIOD_DAYS)
                    .toLocalDate().toString().replace("-", "");

            boolean hasTransaction = false;
            for (final Map<String, Object> card : cards) {
                final String cardNo = (String) card.get("cardNo");
                final String cvc = (String) card.get("cvc");
                if (cardNo == null || cvc == null) continue;

                final List<Map<String, Object>> txns = creditCardClient.inquireCreditCardTransactionList(
                        ctx.ssafyUserKey(), cardNo, cvc, startDate, endDate);
                if (!txns.isEmpty()) {
                    hasTransaction = true;
                    break;
                }
            }

            return hasTransaction ? MAX_PAYMENT : (int) (MAX_PAYMENT * 0.5);
        } catch (final Exception e) {
            log.debug("카드 활동 조회 실패, Payment History에서 카드 제외", e);
            return -1;
        }
    }

    // ──────────────────────────────────────────────────
    // 2. Amounts Owed (300점)
    //    소비율 = 1년간 출금(TRANSFER) 합계 / 입금(DEPOSIT+SAVE) 합계
    //    비례식: 300 × (1 - 소비율)
    //    이력 없음: 200점 (보수적 — 부채 없으나 건전성 증명 불가)
    //
    //    인식 방법:
    //    - 입금: transactionType = "DEPOSIT" (외부 입금) + "SAVE" (PASS 저축)
    //    - 출금: transactionType = "TRANSFER" (외부 송금)
    // ──────────────────────────────────────────────────
    private int calcAmountsOwed(final Long userId, final boolean hasCreditHistory) {
        if (!hasCreditHistory) {
            return NO_HISTORY_OWED;
        }

        final LocalDateTime oneYearAgo = LocalDateTime.now().minusDays(BASE_PERIOD_DAYS);

        final List<SeedmoneyTransaction> deposits = seedmoneyTransactionRepository
                .findByUserIdAndTransactionTypeAndCreatedAtAfter(userId, "DEPOSIT", oneYearAgo);
        final List<SeedmoneyTransaction> saves = seedmoneyTransactionRepository
                .findByUserIdAndTransactionTypeAndCreatedAtAfter(userId, "SAVE", oneYearAgo);
        final List<SeedmoneyTransaction> transfers = seedmoneyTransactionRepository
                .findByUserIdAndTransactionTypeAndCreatedAtAfter(userId, "TRANSFER", oneYearAgo);

        final long totalIncome = deposits.stream().mapToLong(t -> Math.abs(t.getAmount().longValue())).sum()
                + saves.stream().mapToLong(t -> Math.abs(t.getAmount().longValue())).sum();
        final long totalOutflow = transfers.stream().mapToLong(t -> Math.abs(t.getAmount().longValue())).sum();

        if (totalIncome == 0) {
            return totalOutflow == 0 ? NO_HISTORY_OWED : MIN_OWED;
        }

        final double spendingRatio = Math.min((double) totalOutflow / totalIncome, 1.5);
        final double score = MAX_OWED * (1.0 - spendingRatio);
        return clamp((int) Math.round(score), MIN_OWED, MAX_OWED);
    }

    // ──────────────────────────────────────────────────
    // 3. Credit Length (150점)
    //    비례식: 150 × min(일수 / 365, 1.0)
    //    만점 기준: 1년 (365일)
    // ──────────────────────────────────────────────────
    private int calcCreditLength(final Long userId) {
        return seedmoneyAccountRepository.findByUserId(userId)
                .map(account -> {
                    final long days = ChronoUnit.DAYS.between(account.getUpdatedAt(), LocalDateTime.now());
                    final double ratio = Math.min((double) days / LENGTH_MAX_DAYS, 1.0);
                    return clamp((int) Math.round(MAX_LENGTH * ratio), MIN_LENGTH, MAX_LENGTH);
                })
                .orElse(MIN_LENGTH);
    }

    // ──────────────────────────────────────────────────
    // 4. Credit Mix (100점)
    //    비례식: 100 × (유형수 / 3)
    // ──────────────────────────────────────────────────
    private int calcCreditMix(final Long userId) {
        int productTypes = 0;

        if (seedmoneyAccountRepository.findByUserId(userId).isPresent()) {
            productTypes++;
        }

        final List<PassSubscription> activeSubs =
                passSubscriptionRepository.findByUserIdAndIsActiveTrue(userId);
        if (!activeSubs.isEmpty()) {
            productTypes++;
        }

        if (getCardCount(userId) > 0) {
            productTypes++;
        }

        final double ratio = (double) productTypes / 3.0;
        return clamp((int) Math.round(MAX_MIX * ratio), MIN_MIX, MAX_MIX);
    }

    // ──────────────────────────────────────────────────
    // 5. New Credit (100점)
    //    기준 기간: 6개월 (180일)
    //    비례식: 100 × max(1 - (구독수 - 1) × 0.12, 0.2)
    //    0~1건 = 만점, 이후 건당 12% 감소
    // ──────────────────────────────────────────────────
    private int calcNewCredit(final Long userId) {
        final LocalDateTime sixMonthsAgo = LocalDateTime.now().minusDays(NEW_CREDIT_PERIOD_DAYS);

        final List<PassSubscription> allSubs =
                passSubscriptionRepository.findByUserIdAndIsActiveTrue(userId);
        final long recentNewSubs = allSubs.stream()
                .filter(sub -> sub.getSubscribedAt() != null && sub.getSubscribedAt().isAfter(sixMonthsAgo))
                .count();

        if (recentNewSubs <= 1) {
            return MAX_NEW_CREDIT;
        }

        final double penalty = (recentNewSubs - 1) * 0.12;
        final double ratio = Math.max(1.0 - penalty, 0.2);
        return clamp((int) Math.round(MAX_NEW_CREDIT * ratio), MIN_NEW_CREDIT, MAX_NEW_CREDIT);
    }

    private CreditScore calculateFromAssetProfile(
            final UserAssetProfile profile,
            final int totalDepositAmount,
            final int totalLoanAmount,
            final int totalOtherIncomeAmount,
            final int totalCardSpendAmount,
            final boolean hasCardSpend
    ) {
        final int totalMonthlyIncomeAmount = profile.getMonthlySalaryAmount() + totalOtherIncomeAmount;
        final int totalMonthlyExpenseAmount = profile.getMonthlyFixedExpenseAmount() + totalCardSpendAmount;
        final int reserveAmount = profile.getMainAccountBalanceAmount() + totalDepositAmount;
        final double jobStability = stabilityWeight(profile.getJobType());
        final double savingsCushion = normalizeSavingsCushion(
                reserveAmount,
                totalMonthlyExpenseAmount
        );
        final double cashflowBurden = normalizeCashflowBurden(
                totalMonthlyIncomeAmount,
                totalMonthlyExpenseAmount
        );
        final double debtBurden = normalizeDebtBurden(
                totalLoanAmount,
                totalMonthlyIncomeAmount
        );

        final int paymentHistory = proportional(
                (jobStability * 0.6) + (savingsCushion * 0.4),
                MIN_PAYMENT,
                MAX_PAYMENT
        );
        final int amountsOwed = proportional(
                1.0 - clampRatio((cashflowBurden * 0.7) + (debtBurden * 0.3)),
                MIN_OWED,
                MAX_OWED
        );
        final int creditLength = proportional(
                (jobStability * 0.5) + (savingsCushion * 0.5),
                MIN_LENGTH,
                MAX_LENGTH
        );
        final int productTypes = countProductTypes(totalDepositAmount, totalLoanAmount, hasCardSpend);
        final int creditMix = clamp((int) Math.round(MAX_MIX * (productTypes / 3.0)), MIN_MIX, MAX_MIX);
        final int newCredit = (totalLoanAmount > 0 || hasCardSpend) ? 85 : MAX_NEW_CREDIT;

        return CreditScore.of(paymentHistory, amountsOwed, creditLength, creditMix, newCredit);
    }

    // ── 유틸 ──

    private int proportional(final double ratio, final int min, final int max) {
        return clamp((int) Math.round(max * ratio), min, max);
    }

    private int clamp(final int value, final int min, final int max) {
        return Math.max(min, Math.min(value, max));
    }

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

    private double stabilityWeight(final JobType jobType) {
        if (jobType == null) {
            return 0.5;
        }
        return switch (jobType) {
            case LARGE_BIZ -> 0.95;
            case MID_BIZ -> 0.85;
            case SMALL_BIZ -> 0.75;
            case STARTUP -> 0.65;
            case FREELANCER -> 0.55;
        };
    }

    private double normalizeSavingsCushion(
            final int depositAmount,
            final int monthlyFixedExpenseAmount
    ) {
        if (depositAmount <= 0) {
            return 0.0;
        }
        if (monthlyFixedExpenseAmount <= 0) {
            return 1.0;
        }
        return clampRatio((double) depositAmount / (monthlyFixedExpenseAmount * 6.0));
    }

    private double normalizeCashflowBurden(
            final int monthlyIncomeAmount,
            final int monthlyFixedExpenseAmount
    ) {
        if (monthlyIncomeAmount <= 0) {
            return monthlyFixedExpenseAmount > 0 ? 1.0 : 0.0;
        }
        return clampRatio((double) monthlyFixedExpenseAmount / monthlyIncomeAmount);
    }

    private double normalizeDebtBurden(
            final int loanAmount,
            final int monthlyIncomeAmount
    ) {
        if (loanAmount <= 0) {
            return 0.0;
        }
        if (monthlyIncomeAmount <= 0) {
            return 1.0;
        }
        return clampRatio((double) loanAmount / (monthlyIncomeAmount * 12.0));
    }

    private double clampRatio(final double value) {
        return Math.max(0.0, Math.min(value, 1.0));
    }

    private int totalDepositAmount(final Long userId) {
        return userAssetDepositRepository.findAllByUserIdOrderByIdAsc(userId).stream()
                .mapToInt(item -> item.getAmount().intValue())
                .sum();
    }

    private int totalLoanAmount(final Long userId) {
        return userAssetLoanRepository.findAllByUserIdOrderByIdAsc(userId).stream()
                .mapToInt(item -> item.getAmount().intValue())
                .sum();
    }

    private int totalOtherIncomeAmount(final Long userId) {
        return userAssetOtherIncomeRepository.findAllByUserIdOrderByIdAsc(userId).stream()
                .mapToInt(item -> item.getAmount().intValue())
                .sum();
    }

    private int totalCardSpendAmount(final Long userId) {
        return userAssetCardSpendRepository.findAllByUserIdOrderByIdAsc(userId).stream()
                .mapToInt(item -> item.getAmount().intValue())
                .sum();
    }

    private boolean hasCardSpend(final Long userId) {
        return !userAssetCardSpendRepository.findAllByUserIdOrderByIdAsc(userId).isEmpty();
    }

    private int countProductTypes(
            final int totalDepositAmount,
            final int totalLoanAmount,
            final boolean hasCardSpend
    ) {
        int productTypes = 0;
        if (totalDepositAmount > 0) {
            productTypes++;
        }
        if (totalLoanAmount > 0) {
            productTypes++;
        }
        if (hasCardSpend) {
            productTypes++;
        }
        return productTypes;
    }
}
