package io.ssafy.p.j14c103.homerun.api.service.home.credit;

import io.ssafy.p.j14c103.homerun.api.service.financial.UserFinancialSummaryService;
import io.ssafy.p.j14c103.homerun.domain.account.AccountTransactionType;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccount;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransaction;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.card.CardTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.card.OwnedCardRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.FinancialProductType;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialProduct;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialProductRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialSummary;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscription;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscriptionRepository;
import io.ssafy.p.j14c103.homerun.domain.pass.UserPassTransaction;
import io.ssafy.p.j14c103.homerun.domain.pass.UserPassTransactionRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * FICO 프레임워크 기반 자체 CSS (Credit Scoring System).
 * <p>
 * 비례식 산출, 보수적 기준.
 * 모든 금융 활동 기록은 1년(365일) 기반.
 * </p>
 * <p>
 * 데이터 인식:
 * - 계좌 거래유형: DEPOSIT, WITHDRAW, PASS_SAVE_OUT, PASS_SAVE_IN, INTERNAL_TRANSFER
 * - 카드 거래: 로컬 card_transactions
 * - PASS 이행: UserPassTransaction
 * - 자산/부채: UserFinancialSummary
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FicoCreditScoringService implements CreditScoreProvider {

    private static final int BASE_PERIOD_DAYS = 365;
    private static final int NEW_CREDIT_PERIOD_DAYS = 180;
    private static final int LENGTH_MAX_DAYS = 365;

    private static final int MIN_PAYMENT = 35;
    private static final int MAX_PAYMENT = 350;
    private static final int NO_HISTORY_PAYMENT = 175;

    private static final int MIN_OWED = 30;
    private static final int MAX_OWED = 300;
    private static final int NO_HISTORY_OWED = 200;

    private static final int MIN_LENGTH = 15;
    private static final int MAX_LENGTH = 150;

    private static final int MIN_MIX = 20;
    private static final int MAX_MIX = 100;

    private static final int MIN_NEW_CREDIT = 20;
    private static final int MAX_NEW_CREDIT = 100;

    private final PassSubscriptionRepository passSubscriptionRepository;
    private final UserPassTransactionRepository passTransactionRepository;
    private final UserAccountRepository userAccountRepository;
    private final UserAccountTransactionRepository userAccountTransactionRepository;
    private final OwnedCardRepository ownedCardRepository;
    private final CardTransactionRepository cardTransactionRepository;
    private final UserFinancialProductRepository userFinancialProductRepository;
    private final UserFinancialSummaryService userFinancialSummaryService;

    @Override
    public CreditScore calculate(final Long userId) {
        final boolean hasCreditHistory = hasCreditHistory(userId);

        final int paymentHistory = calcPaymentHistory(userId, hasCreditHistory);
        final int amountsOwed = calcAmountsOwed(userId, hasCreditHistory);
        final int creditLength = calcCreditLength(userId);
        final int creditMix = calcCreditMix(userId);
        final int newCredit = calcNewCredit(userId);

        final CreditScore result = CreditScore.of(
                paymentHistory,
                amountsOwed,
                creditLength,
                creditMix,
                newCredit
        );
        log.info("CSS 점수 산출 [userId={}, 이력={}]: {} ({}등급 {})",
                userId, hasCreditHistory, result.getScore(), result.getGrade(), result.getGradeLabel());
        return result;
    }

    private boolean hasCreditHistory(final Long userId) {
        if (!userAccountRepository.findByUserIdAndActiveYnTrue(userId).isEmpty()) {
            return true;
        }
        if (!userAccountTransactionRepository.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
                userId,
                LocalDateTime.of(1970, 1, 1, 0, 0)
        ).isEmpty()) {
            return true;
        }
        if (!passSubscriptionRepository.findByUserIdAndIsActiveTrue(userId).isEmpty()) {
            return true;
        }
        if (!ownedCardRepository.findAllByUserIdAndActiveYnTrueOrderByOpenedAtDesc(userId).isEmpty()) {
            return true;
        }
        if (!cardTransactionRepository.findAllByUserIdOrderByPaymentDateDescCreatedAtDesc(userId).isEmpty()) {
            return true;
        }
        return !userFinancialProductRepository.findByUserIdAndActiveYnTrue(userId).isEmpty();
    }

    private int calcPaymentHistory(final Long userId, final boolean hasCreditHistory) {
        if (!hasCreditHistory) {
            return NO_HISTORY_PAYMENT;
        }

        int totalScore = 0;
        int factors = 0;

        final List<PassSubscription> activeSubscriptions =
                passSubscriptionRepository.findByUserIdAndIsActiveTrue(userId);
        if (!activeSubscriptions.isEmpty()) {
            final double fulfillmentRate = calcPassFulfillmentRate(activeSubscriptions);
            totalScore += proportional(fulfillmentRate, MIN_PAYMENT, MAX_PAYMENT);
            factors++;
        }

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

    private double calcPassFulfillmentRate(final List<PassSubscription> activeSubscriptions) {
        final LocalDateTime oneYearAgo = LocalDateTime.now().minusDays(BASE_PERIOD_DAYS);
        long actualSaves = 0;
        final long expectedSaves = activeSubscriptions.size() * (long) BASE_PERIOD_DAYS;

        for (final PassSubscription subscription : activeSubscriptions) {
            final List<UserPassTransaction> transactions = passTransactionRepository
                    .findBySubscriptionIdAndTransactionDateAfter(subscription.getId(), oneYearAgo.toString());
            actualSaves += transactions.size();
        }

        if (expectedSaves == 0) {
            return 1.0;
        }
        return Math.min((double) actualSaves / expectedSaves, 1.0);
    }

    private int calcCardActivity(final Long userId) {
        if (ownedCardRepository.findAllByUserIdAndActiveYnTrueOrderByOpenedAtDesc(userId).isEmpty()) {
            return -1;
        }

        final LocalDate startDate = LocalDate.now().minusDays(BASE_PERIOD_DAYS);
        final LocalDate endDate = LocalDate.now();
        final boolean hasTransaction = !cardTransactionRepository
                .findAllByUserIdAndPaymentDateBetweenOrderByPaymentDateDescCreatedAtDesc(userId, startDate, endDate)
                .isEmpty();

        if (hasTransaction) {
            return MAX_PAYMENT;
        }
        return (int) (MAX_PAYMENT * 0.5);
    }

    private int calcAmountsOwed(final Long userId, final boolean hasCreditHistory) {
        if (!hasCreditHistory) {
            return NO_HISTORY_OWED;
        }

        final List<Integer> scores = new ArrayList<>();

        final Integer cashFlowScore = calcCashFlowScore(userId);
        if (cashFlowScore != null) {
            scores.add(cashFlowScore);
        }

        final Integer debtScore = calcDebtRatioScore(userId);
        if (debtScore != null) {
            scores.add(debtScore);
        }

        if (scores.isEmpty()) {
            return NO_HISTORY_OWED;
        }

        return clamp(
                (int) Math.round(scores.stream().mapToInt(Integer::intValue).average().orElse(NO_HISTORY_OWED)),
                MIN_OWED,
                MAX_OWED
        );
    }

    private Integer calcCashFlowScore(final Long userId) {
        final LocalDateTime oneYearAgo = LocalDateTime.now().minusDays(BASE_PERIOD_DAYS);
        final List<UserAccountTransaction> transactions = userAccountTransactionRepository
                .findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, oneYearAgo);

        final long totalIncome = transactions.stream()
                .filter(transaction -> transaction.getTransactionType() == AccountTransactionType.DEPOSIT)
                .mapToLong(transaction -> transaction.getAmount().longValue())
                .sum();
        final long totalOutflow = transactions.stream()
                .filter(transaction -> transaction.getTransactionType() == AccountTransactionType.WITHDRAW)
                .mapToLong(transaction -> transaction.getAmount().longValue())
                .sum();

        if (totalIncome == 0) {
            if (totalOutflow == 0) {
                return null;
            }
            return MIN_OWED;
        }

        final double spendingRatio = Math.min((double) totalOutflow / totalIncome, 1.5);
        final double score = MAX_OWED * (1.0 - spendingRatio);
        return clamp((int) Math.round(score), MIN_OWED, MAX_OWED);
    }

    private Integer calcDebtRatioScore(final Long userId) {
        final UserFinancialSummary summary = userFinancialSummaryService.getSummary(userId);
        final long totalAssetAmount = summary.getTotalAssetAmount().longValue();
        final long totalDebtAmount = summary.getTotalDebtAmount().longValue();

        if (totalAssetAmount == 0 && totalDebtAmount == 0) {
            return null;
        }
        if (totalAssetAmount == 0) {
            return MIN_OWED;
        }

        final double debtRatio = Math.min((double) totalDebtAmount / totalAssetAmount, 1.5);
        final double score = MAX_OWED * (1.0 - debtRatio);
        return clamp((int) Math.round(score), MIN_OWED, MAX_OWED);
    }

    private int calcCreditLength(final Long userId) {
        final List<LocalDateTime> openedDates = new ArrayList<>();

        userAccountRepository.findByUserIdAndActiveYnTrue(userId).stream()
                .map(UserAccount::getOpenedAt)
                .forEach(openedDates::add);
        ownedCardRepository.findAllByUserIdAndActiveYnTrueOrderByOpenedAtDesc(userId).stream()
                .map(ownedCard -> ownedCard.getOpenedAt())
                .forEach(openedDates::add);
        userFinancialProductRepository.findByUserIdAndActiveYnTrue(userId).stream()
                .map(UserFinancialProduct::getOpenedAt)
                .forEach(openedDates::add);

        return openedDates.stream()
                .min(LocalDateTime::compareTo)
                .map(openedAt -> {
                    final long days = ChronoUnit.DAYS.between(openedAt, LocalDateTime.now());
                    final double ratio = Math.min((double) days / LENGTH_MAX_DAYS, 1.0);
                    return clamp((int) Math.round(MAX_LENGTH * ratio), MIN_LENGTH, MAX_LENGTH);
                })
                .orElse(MIN_LENGTH);
    }

    private int calcCreditMix(final Long userId) {
        int productTypes = 0;
        final List<UserFinancialProduct> products = userFinancialProductRepository.findByUserIdAndActiveYnTrue(userId);

        if (userAccountRepository.findByUserIdAndAccountType(userId, AccountType.MAIN).isPresent()) {
            productTypes++;
        }
        if (userAccountRepository.findByUserIdAndAccountType(userId, AccountType.SEEDMONEY).isPresent()) {
            productTypes++;
        }
        if (!passSubscriptionRepository.findByUserIdAndIsActiveTrue(userId).isEmpty()) {
            productTypes++;
        }
        if (!ownedCardRepository.findAllByUserIdAndActiveYnTrueOrderByOpenedAtDesc(userId).isEmpty()) {
            productTypes++;
        }
        if (products.stream().anyMatch(product -> product.getProductType() == FinancialProductType.SAVING_DEPOSIT)) {
            productTypes++;
        }
        if (products.stream().anyMatch(product -> product.getProductType() == FinancialProductType.INVESTMENT)) {
            productTypes++;
        }
        if (products.stream().anyMatch(product -> product.getProductType() == FinancialProductType.LOAN)) {
            productTypes++;
        }

        final double ratio = (double) productTypes / 7.0;
        return clamp((int) Math.round(MAX_MIX * ratio), MIN_MIX, MAX_MIX);
    }

    private int calcNewCredit(final Long userId) {
        final LocalDateTime sixMonthsAgo = LocalDateTime.now().minusDays(NEW_CREDIT_PERIOD_DAYS);

        final long recentPassCount = passSubscriptionRepository.findByUserIdAndIsActiveTrue(userId).stream()
                .filter(subscription -> subscription.getSubscribedAt() != null)
                .filter(subscription -> subscription.getSubscribedAt().isAfter(sixMonthsAgo))
                .count();
        final long recentOwnedCardCount = ownedCardRepository.findAllByUserIdAndActiveYnTrueOrderByOpenedAtDesc(userId)
                .stream()
                .filter(card -> card.getOpenedAt().isAfter(sixMonthsAgo))
                .count();
        final long recentLoanCount = userFinancialProductRepository.findByUserIdAndActiveYnTrue(userId).stream()
                .filter(product -> product.getProductType() == FinancialProductType.LOAN)
                .filter(product -> product.getOpenedAt().isAfter(sixMonthsAgo))
                .count();

        final long recentNewCredits = recentPassCount + recentOwnedCardCount + recentLoanCount;
        if (recentNewCredits <= 1) {
            return MAX_NEW_CREDIT;
        }

        final double penalty = (recentNewCredits - 1) * 0.12;
        final double ratio = Math.max(1.0 - penalty, 0.2);
        return clamp((int) Math.round(MAX_NEW_CREDIT * ratio), MIN_NEW_CREDIT, MAX_NEW_CREDIT);
    }

    private int proportional(final double ratio, final int min, final int max) {
        return clamp((int) Math.round(max * ratio), min, max);
    }

    private int clamp(final int value, final int min, final int max) {
        return Math.max(min, Math.min(value, max));
    }
}
