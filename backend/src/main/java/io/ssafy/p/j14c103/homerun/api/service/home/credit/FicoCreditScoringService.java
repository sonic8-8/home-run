package io.ssafy.p.j14c103.homerun.api.service.home.credit;

import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccount;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.card.CardTransaction;
import io.ssafy.p.j14c103.homerun.domain.card.CardTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.card.OwnedCard;
import io.ssafy.p.j14c103.homerun.domain.card.OwnedCardRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.financial.FinancialProductType;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialProduct;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialProductRepository;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscription;
import io.ssafy.p.j14c103.homerun.domain.pass.PassSubscriptionRepository;
import io.ssafy.p.j14c103.homerun.domain.pass.UserPassTransaction;
import io.ssafy.p.j14c103.homerun.domain.pass.UserPassTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransaction;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransactionRepository;
import io.ssafy.p.j14c103.homerun.domain.user.HomeCreditScoreSnapshotType;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetCardSpendRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetDepositRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetLoanRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetOtherIncomeRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetProfile;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetProfileRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserHomeCreditScoreSnapshot;
import io.ssafy.p.j14c103.homerun.domain.user.UserHomeCreditScoreSnapshotRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 홈 전용 내부 CSS 계산기.
 * 현재 월 점수는 월 1일 기준으로 한 번 생성된 스냅샷을 그대로 사용한다.
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
    private static final int PROFILE_PAYMENT_MAX = 250;

    private static final int MIN_OWED = 30;
    private static final int MAX_OWED = 300;
    private static final int NO_HISTORY_OWED = 200;

    private static final int MIN_LENGTH = 15;
    private static final int MAX_LENGTH = 150;
    private static final int NEUTRAL_LENGTH = 83;

    private static final int MIN_MIX = 20;
    private static final int MAX_MIX = 100;
    private static final int CREDIT_MIX_TYPE_COUNT = 5;

    private static final int MIN_NEW_CREDIT = 20;
    private static final int MAX_NEW_CREDIT = 100;

    private final UserAccountRepository userAccountRepository;
    private final PassSubscriptionRepository passSubscriptionRepository;
    private final UserPassTransactionRepository passTransactionRepository;
    private final SeedmoneyTransactionRepository seedmoneyTransactionRepository;
    private final OwnedCardRepository ownedCardRepository;
    private final CardTransactionRepository cardTransactionRepository;
    private final UserFinancialProductRepository userFinancialProductRepository;
    private final UserAssetProfileRepository userAssetProfileRepository;
    private final UserAssetDepositRepository userAssetDepositRepository;
    private final UserAssetLoanRepository userAssetLoanRepository;
    private final UserAssetOtherIncomeRepository userAssetOtherIncomeRepository;
    private final UserAssetCardSpendRepository userAssetCardSpendRepository;
    private final UserHomeCreditScoreSnapshotRepository userHomeCreditScoreSnapshotRepository;
    private final Clock appClock;

    @Override
    @Transactional
    public CreditScore calculate(final Long userId) {
        return toCreditScore(resolveCurrentMonthSnapshot(userId, false, false));
    }

    @Transactional
    public CreditScore initializeOnboardingSnapshot(
            final Long userId,
            final boolean overwriteExisting
    ) {
        return toCreditScore(resolveCurrentMonthSnapshot(userId, overwriteExisting, true));
    }

    private UserHomeCreditScoreSnapshot resolveCurrentMonthSnapshot(
            final Long userId,
            final boolean overwriteExisting,
            final boolean onboardingInitialization
    ) {
        final LocalDate scoreMonthStart = currentMonthStart();
        final Optional<UserHomeCreditScoreSnapshot> existingSnapshot = userHomeCreditScoreSnapshotRepository
                .findByUserIdAndScoreMonthStart(userId, scoreMonthStart);

        if (existingSnapshot.isPresent() && !overwriteExisting) {
            return existingSnapshot.get();
        }

        final boolean hasPreviousSnapshot = userHomeCreditScoreSnapshotRepository.existsByUserId(userId);
        final CreditScore score;
        final HomeCreditScoreSnapshotType snapshotType;

        if (onboardingInitialization) {
            score = calculateLive(userId, LocalDateTime.now(appClock));
            snapshotType = HomeCreditScoreSnapshotType.INITIAL;
        } else if (hasPreviousSnapshot) {
            score = calculateLive(userId, scoreMonthStart.atStartOfDay());
            snapshotType = HomeCreditScoreSnapshotType.MONTHLY;
        } else {
            score = calculateLive(userId, LocalDateTime.now(appClock));
            snapshotType = HomeCreditScoreSnapshotType.INITIAL;
        }

        return upsertSnapshot(existingSnapshot.orElse(null), userId, scoreMonthStart, snapshotType, score);
    }

    private UserHomeCreditScoreSnapshot upsertSnapshot(
            final UserHomeCreditScoreSnapshot existingSnapshot,
            final Long userId,
            final LocalDate scoreMonthStart,
            final HomeCreditScoreSnapshotType snapshotType,
            final CreditScore score
    ) {
        final LocalDateTime createdAt = LocalDateTime.now(appClock);
        if (existingSnapshot != null) {
            existingSnapshot.update(
                    snapshotType,
                    score.getPaymentHistory(),
                    score.getAmountsOwed(),
                    score.getCreditLength(),
                    score.getCreditMix(),
                    score.getNewCredit(),
                    createdAt
            );
            return existingSnapshot;
        }

        return userHomeCreditScoreSnapshotRepository.save(UserHomeCreditScoreSnapshot.create(
                userId,
                scoreMonthStart,
                snapshotType,
                score.getPaymentHistory(),
                score.getAmountsOwed(),
                score.getCreditLength(),
                score.getCreditMix(),
                score.getNewCredit(),
                createdAt
        ));
    }

    private CreditScore calculateLive(
            final Long userId,
        final LocalDateTime referenceDateTime
    ) {
        final UserAssetProfile assetProfile = userAssetProfileRepository.findById(userId).orElse(null);
        final long totalDepositAmount = totalDepositAmount(userId);
        final long totalLoanAmount = totalLoanAmount(userId);
        final long totalOtherIncomeAmount = totalOtherIncomeAmount(userId);
        final long totalCardSpendAmount = totalCardSpendAmount(userId);
        final boolean hasCardSignal = hasCardSignal(userId, referenceDateTime);
        final boolean hasCreditHistory = hasCreditHistory(userId, referenceDateTime);

        final int paymentHistory = calcPaymentHistory(
                userId,
                referenceDateTime,
                hasCreditHistory,
                assetProfile,
                totalDepositAmount,
                totalOtherIncomeAmount,
                totalCardSpendAmount
        );
        final int amountsOwed = assetProfile != null
                ? calcAmountsOwedFromAssetProfile(
                userId,
                assetProfile,
                totalDepositAmount,
                totalLoanAmount,
                totalOtherIncomeAmount,
                totalCardSpendAmount
        )
                : calcAmountsOwedFromTransactions(userId, referenceDateTime, hasCreditHistory);
        final int creditLength = calcCreditLength(userId, referenceDateTime);
        final int creditMix = calcCreditMix(
                userId,
                referenceDateTime,
                totalDepositAmount,
                totalLoanAmount,
                hasCardSignal
        );
        final int newCredit = calcNewCredit(userId, referenceDateTime);

        final CreditScore result = CreditScore.of(
                paymentHistory,
                amountsOwed,
                creditLength,
                creditMix,
                newCredit
        );
        log.info(
                "홈 CSS 스냅샷 계산 [userId={}, reference={}, score={}, grade={}]",
                userId,
                referenceDateTime,
                result.getScore(),
                result.getGrade()
        );
        return result;
    }

    private int calcPaymentHistory(
            final Long userId,
            final LocalDateTime referenceDateTime,
            final boolean hasCreditHistory,
            final UserAssetProfile assetProfile,
            final long totalDepositAmount,
            final long totalOtherIncomeAmount,
            final long totalCardSpendAmount
    ) {
        if (!hasCreditHistory) {
            if (assetProfile == null) {
                return NO_HISTORY_PAYMENT;
            }
            return calcPaymentHistoryFromAssetProfile(
                    userId,
                    assetProfile,
                    totalDepositAmount,
                    totalOtherIncomeAmount,
                    totalCardSpendAmount
            );
        }

        int totalScore = 0;
        int factors = 0;

        final List<PassSubscription> relevantSubscriptions = relevantPassSubscriptions(userId, referenceDateTime);
        if (!relevantSubscriptions.isEmpty()) {
            totalScore += proportional(
                    calcPassFulfillmentRate(relevantSubscriptions, referenceDateTime),
                    MIN_PAYMENT,
                    MAX_PAYMENT
            );
            factors++;
        }

        final int cardScore = calcCardActivity(userId, referenceDateTime);
        if (cardScore >= 0) {
            totalScore += cardScore;
            factors++;
        }

        if (factors == 0) {
            return assetProfile == null
                    ? NO_HISTORY_PAYMENT
                    : calcPaymentHistoryFromAssetProfile(
                    userId,
                    assetProfile,
                    totalDepositAmount,
                    totalOtherIncomeAmount,
                    totalCardSpendAmount
            );
        }
        return clamp(totalScore / factors, MIN_PAYMENT, MAX_PAYMENT);
    }

    private int calcPaymentHistoryFromAssetProfile(
            final Long userId,
            final UserAssetProfile profile,
            final long totalDepositAmount,
            final long totalOtherIncomeAmount,
            final long totalCardSpendAmount
    ) {
        final long totalMonthlyIncomeAmount = profile.getMonthlySalaryAmount() + totalOtherIncomeAmount;
        final long totalMonthlyExpenseAmount = profile.getMonthlyFixedExpenseAmount() + totalCardSpendAmount;
        final long reserveAmount = resolveMainBalance(userId, profile)
                + resolveSeedmoneyBalance(userId)
                + totalDepositAmount;
        final double jobStability = stabilityWeight(profile.getJobType());
        final double reserveCushion = normalizeSavingsCushion(reserveAmount, totalMonthlyExpenseAmount);
        final double cashflowHealth = normalizeCashflowHealth(
                totalMonthlyIncomeAmount,
                totalMonthlyExpenseAmount
        );
        final double paymentProxy = clampRatio(
                (jobStability * 0.4)
                        + (reserveCushion * 0.4)
                        + (cashflowHealth * 0.2)
        );

        return proportional(paymentProxy, NO_HISTORY_PAYMENT, PROFILE_PAYMENT_MAX);
    }

    private double calcPassFulfillmentRate(
            final List<PassSubscription> subscriptions,
            final LocalDateTime referenceDateTime
    ) {
        final LocalDateTime inclusiveReference = toInclusiveReference(referenceDateTime);
        final LocalDateTime windowStart = inclusiveReference.minusDays(BASE_PERIOD_DAYS);

        long eligibleSaveDays = 0L;
        long completedSaveDays = 0L;

        for (final PassSubscription subscription : subscriptions) {
            final LocalDateTime subscribedAt = subscription.getSubscribedAt();
            if (subscribedAt == null) {
                continue;
            }

            final LocalDateTime activeStart = subscribedAt.isAfter(windowStart) ? subscribedAt : windowStart;
            final LocalDateTime canceledAt = subscription.getCanceledAt();
            final LocalDateTime activeEnd = canceledAt == null || canceledAt.isAfter(inclusiveReference)
                    ? inclusiveReference
                    : canceledAt;

            if (activeEnd.isBefore(activeStart)) {
                continue;
            }

            eligibleSaveDays += ChronoUnit.DAYS.between(activeStart.toLocalDate(), activeEnd.toLocalDate()) + 1;
            completedSaveDays += passTransactionRepository.findBySubscriptionIdAndTransactionDateBetween(
                            subscription.getId(),
                            activeStart.toString(),
                            activeEnd.toString()
                    ).stream()
                    .map(UserPassTransaction::getTransactionDate)
                    .map(this::parseTransactionDate)
                    .flatMap(Optional::stream)
                    .map(LocalDateTime::toLocalDate)
                    .distinct()
                    .count();
        }

        if (eligibleSaveDays <= 0) {
            return 1.0;
        }
        return Math.min((double) completedSaveDays / eligibleSaveDays, 1.0);
    }

    private int calcCardActivity(
            final Long userId,
            final LocalDateTime referenceDateTime
    ) {
        if (getCardCount(userId, referenceDateTime) == 0) {
            return -1;
        }

        final LocalDateTime inclusiveReference = toInclusiveReference(referenceDateTime);
        final LocalDate startDate = inclusiveReference.minusDays(BASE_PERIOD_DAYS).toLocalDate();
        final LocalDate endDate = inclusiveReference.toLocalDate();
        final List<CardTransaction> transactions = cardTransactionRepository
                .findAllByUserIdAndPaymentDateBetweenOrderByPaymentDateDescCreatedAtDesc(userId, startDate, endDate);

        return transactions.isEmpty() ? proportional(0.5, MIN_PAYMENT, MAX_PAYMENT) : MAX_PAYMENT;
    }

    private int calcAmountsOwedFromTransactions(
            final Long userId,
            final LocalDateTime referenceDateTime,
            final boolean hasCreditHistory
    ) {
        if (!hasCreditHistory) {
            return NO_HISTORY_OWED;
        }

        final LocalDateTime inclusiveReference = toInclusiveReference(referenceDateTime);
        final LocalDateTime oneYearAgo = inclusiveReference.minusDays(BASE_PERIOD_DAYS);
        final long totalIncome = transactionAmount(userId, "DEPOSIT", oneYearAgo, inclusiveReference)
                + transactionAmount(userId, "SAVE", oneYearAgo, inclusiveReference);
        final long totalOutflow = transactionAmount(userId, "TRANSFER", oneYearAgo, inclusiveReference);

        if (totalIncome == 0) {
            return totalOutflow == 0 ? NO_HISTORY_OWED : MIN_OWED;
        }

        final double spendingRatio = Math.min((double) totalOutflow / totalIncome, 1.5);
        final double scoreRatio = 1.0 - Math.min(spendingRatio, 1.0);
        return proportional(scoreRatio, MIN_OWED, MAX_OWED);
    }

    private int calcAmountsOwedFromAssetProfile(
            final Long userId,
            final UserAssetProfile profile,
            final long totalDepositAmount,
            final long totalLoanAmount,
            final long totalOtherIncomeAmount,
            final long totalCardSpendAmount
    ) {
        final long totalMonthlyIncomeAmount = profile.getMonthlySalaryAmount() + totalOtherIncomeAmount;
        final long totalMonthlyExpenseAmount = profile.getMonthlyFixedExpenseAmount() + totalCardSpendAmount;
        final long totalAssetAmount = resolveMainBalance(userId, profile)
                + resolveSeedmoneyBalance(userId)
                + totalDepositAmount;
        final double debtToIncome = normalizeDebtBurden(totalLoanAmount, totalMonthlyIncomeAmount);
        final double debtToAsset = normalizeAssetDebtBurden(totalLoanAmount, totalAssetAmount);
        final double cashflowBurden = normalizeCashflowBurden(
                totalMonthlyIncomeAmount,
                totalMonthlyExpenseAmount
        );
        final double combinedBurden = clampRatio(
                (debtToIncome * 0.5)
                        + (debtToAsset * 0.3)
                        + (cashflowBurden * 0.2)
        );

        return proportional(1.0 - combinedBurden, MIN_OWED, MAX_OWED);
    }

    private int calcCreditLength(
            final Long userId,
            final LocalDateTime referenceDateTime
    ) {
        final LocalDateTime inclusiveReference = toInclusiveReference(referenceDateTime);

        final Optional<LocalDateTime> earliestActivity = earliestAccountOpenedAt(userId, inclusiveReference)
                .or(() -> earliestPassSubscribedAt(userId, inclusiveReference))
                .or(() -> earliestCardOpenedAt(userId, inclusiveReference))
                .or(() -> earliestFinancialProductOpenedAt(userId, inclusiveReference));

        if (earliestActivity.isEmpty()) {
            return NEUTRAL_LENGTH;
        }

        final long days = Math.max(0L, ChronoUnit.DAYS.between(earliestActivity.get(), inclusiveReference));
        final double ratio = Math.min((double) days / LENGTH_MAX_DAYS, 1.0);
        return proportional(ratio, MIN_LENGTH, MAX_LENGTH);
    }

    private int calcCreditMix(
            final Long userId,
            final LocalDateTime referenceDateTime,
            final long totalDepositAmount,
            final long totalLoanAmount,
            final boolean hasCardSignal
    ) {
        final LocalDateTime inclusiveReference = toInclusiveReference(referenceDateTime);
        int productTypes = 0;

        final boolean hasCashAccount = userAccountRepository.findByUserIdAndActiveYnTrue(userId).stream()
                .anyMatch(account -> !account.getOpenedAt().isAfter(inclusiveReference));
        if (hasCashAccount) {
            productTypes++;
        }

        final boolean hasPass = passSubscriptionRepository.findAllByUserIdOrderBySubscribedAtAsc(userId).stream()
                .anyMatch(subscription -> isPassActiveAt(subscription, inclusiveReference));
        if (hasPass) {
            productTypes++;
        }

        if (hasCardSignal) {
            productTypes++;
        }

        final boolean hasSavingProducts = totalDepositAmount > 0
                || userFinancialProductRepository.findByUserIdAndActiveYnTrue(userId).stream()
                .anyMatch(product -> !product.getOpenedAt().isAfter(inclusiveReference)
                        && (product.getProductType() == FinancialProductType.SAVING_DEPOSIT
                        || product.getProductType() == FinancialProductType.INVESTMENT));
        if (hasSavingProducts) {
            productTypes++;
        }

        final boolean hasLoanProducts = totalLoanAmount > 0
                || userFinancialProductRepository.findByUserIdAndActiveYnTrue(userId).stream()
                .anyMatch(product -> !product.getOpenedAt().isAfter(inclusiveReference)
                        && product.getProductType() == FinancialProductType.LOAN);
        if (hasLoanProducts) {
            productTypes++;
        }

        return proportional((double) productTypes / CREDIT_MIX_TYPE_COUNT, MIN_MIX, MAX_MIX);
    }

    private int calcNewCredit(
            final Long userId,
            final LocalDateTime referenceDateTime
    ) {
        final LocalDateTime inclusiveReference = toInclusiveReference(referenceDateTime);
        final LocalDateTime sixMonthsAgo = inclusiveReference.minusDays(NEW_CREDIT_PERIOD_DAYS);

        final long recentNewPassCount = passSubscriptionRepository.findAllByUserIdOrderBySubscribedAtAsc(userId).stream()
                .map(PassSubscription::getSubscribedAt)
                .filter(subscribedAt -> isWithinRange(subscribedAt, sixMonthsAgo, inclusiveReference))
                .count();

        final long recentNewCardCount = ownedCardRepository.findAllByUserIdAndActiveYnTrueOrderByOpenedAtDesc(userId).stream()
                .map(OwnedCard::getOpenedAt)
                .filter(openedAt -> isWithinRange(openedAt, sixMonthsAgo, inclusiveReference))
                .count();

        final long recentNewFinancialProductCount = userFinancialProductRepository.findByUserIdAndActiveYnTrue(userId).stream()
                .map(UserFinancialProduct::getOpenedAt)
                .filter(openedAt -> isWithinRange(openedAt, sixMonthsAgo, inclusiveReference))
                .count();

        final long recentNewCreditCount = recentNewPassCount + recentNewCardCount + recentNewFinancialProductCount;
        if (recentNewCreditCount <= 1) {
            return MAX_NEW_CREDIT;
        }

        final double penalty = (recentNewCreditCount - 1) * 0.12;
        final double ratio = Math.max(1.0 - penalty, 0.2);
        return proportional(ratio, MIN_NEW_CREDIT, MAX_NEW_CREDIT);
    }

    private LocalDate currentMonthStart() {
        return LocalDate.now(appClock).withDayOfMonth(1);
    }

    private CreditScore toCreditScore(final UserHomeCreditScoreSnapshot snapshot) {
        return CreditScore.of(
                snapshot.getPaymentHistory(),
                snapshot.getAmountsOwed(),
                snapshot.getCreditLength(),
                snapshot.getCreditMix(),
                snapshot.getNewCredit()
        );
    }

    private LocalDateTime toInclusiveReference(final LocalDateTime referenceDateTime) {
        if (referenceDateTime.toLocalTime().equals(LocalTime.MIN)) {
            return referenceDateTime.minusNanos(1);
        }
        return referenceDateTime;
    }

    private boolean hasCreditHistory(
            final Long userId,
            final LocalDateTime referenceDateTime
    ) {
        final LocalDateTime inclusiveReference = toInclusiveReference(referenceDateTime);
        final boolean hasPass = passSubscriptionRepository.findAllByUserIdOrderBySubscribedAtAsc(userId).stream()
                .anyMatch(subscription -> subscription.getSubscribedAt() != null
                        && !subscription.getSubscribedAt().isAfter(inclusiveReference));
        if (hasPass) {
            return true;
        }
        return getCardCount(userId, referenceDateTime) > 0;
    }

    private List<PassSubscription> relevantPassSubscriptions(
            final Long userId,
            final LocalDateTime referenceDateTime
    ) {
        final LocalDateTime inclusiveReference = toInclusiveReference(referenceDateTime);
        final LocalDateTime windowStart = inclusiveReference.minusDays(BASE_PERIOD_DAYS);
        return passSubscriptionRepository.findAllByUserIdOrderBySubscribedAtAsc(userId).stream()
                .filter(subscription -> overlapsWindow(subscription, windowStart, inclusiveReference))
                .toList();
    }

    private boolean overlapsWindow(
            final PassSubscription subscription,
            final LocalDateTime windowStart,
            final LocalDateTime inclusiveReference
    ) {
        final LocalDateTime subscribedAt = subscription.getSubscribedAt();
        if (subscribedAt == null || subscribedAt.isAfter(inclusiveReference)) {
            return false;
        }
        final LocalDateTime canceledAt = subscription.getCanceledAt();
        return canceledAt == null || canceledAt.isAfter(windowStart);
    }

    private boolean isPassActiveAt(
            final PassSubscription subscription,
            final LocalDateTime inclusiveReference
    ) {
        final LocalDateTime subscribedAt = subscription.getSubscribedAt();
        if (subscribedAt == null || subscribedAt.isAfter(inclusiveReference)) {
            return false;
        }
        final LocalDateTime canceledAt = subscription.getCanceledAt();
        return canceledAt == null || canceledAt.isAfter(inclusiveReference);
    }

    private Optional<LocalDateTime> earliestAccountOpenedAt(
            final Long userId,
            final LocalDateTime inclusiveReference
    ) {
        return userAccountRepository.findByUserIdAndActiveYnTrue(userId).stream()
                .map(UserAccount::getOpenedAt)
                .filter(openedAt -> !openedAt.isAfter(inclusiveReference))
                .min(Comparator.naturalOrder());
    }

    private Optional<LocalDateTime> earliestPassSubscribedAt(
            final Long userId,
            final LocalDateTime inclusiveReference
    ) {
        return passSubscriptionRepository.findAllByUserIdOrderBySubscribedAtAsc(userId).stream()
                .map(PassSubscription::getSubscribedAt)
                .filter(subscribedAt -> subscribedAt != null && !subscribedAt.isAfter(inclusiveReference))
                .min(Comparator.naturalOrder());
    }

    private Optional<LocalDateTime> earliestCardOpenedAt(
            final Long userId,
            final LocalDateTime inclusiveReference
    ) {
        return ownedCardRepository.findAllByUserIdAndActiveYnTrueOrderByOpenedAtDesc(userId).stream()
                .map(OwnedCard::getOpenedAt)
                .filter(openedAt -> !openedAt.isAfter(inclusiveReference))
                .min(Comparator.naturalOrder());
    }

    private Optional<LocalDateTime> earliestFinancialProductOpenedAt(
            final Long userId,
            final LocalDateTime inclusiveReference
    ) {
        return userFinancialProductRepository.findByUserIdAndActiveYnTrue(userId).stream()
                .map(UserFinancialProduct::getOpenedAt)
                .filter(openedAt -> !openedAt.isAfter(inclusiveReference))
                .min(Comparator.naturalOrder());
    }

    private Optional<LocalDateTime> parseTransactionDate(final String transactionDate) {
        try {
            return Optional.of(LocalDateTime.parse(transactionDate));
        } catch (final RuntimeException exception) {
            return Optional.empty();
        }
    }

    private long transactionAmount(
            final Long userId,
            final String transactionType,
            final LocalDateTime start,
            final LocalDateTime end
    ) {
        return seedmoneyTransactionRepository.findByUserIdAndTransactionTypeAndCreatedAtBetween(
                        userId,
                        transactionType,
                        start,
                        end
                ).stream()
                .mapToLong(transaction -> Math.abs(transaction.getAmount().longValue()))
                .sum();
    }

    private int getCardCount(
            final Long userId,
            final LocalDateTime referenceDateTime
    ) {
        final LocalDateTime inclusiveReference = toInclusiveReference(referenceDateTime);
        return (int) ownedCardRepository.findAllByUserIdAndActiveYnTrueOrderByOpenedAtDesc(userId).stream()
                .filter(card -> !card.getOpenedAt().isAfter(inclusiveReference))
                .count();
    }

    private boolean hasCardSignal(
            final Long userId,
            final LocalDateTime referenceDateTime
    ) {
        return hasCardSpendInput(userId) || getCardCount(userId, referenceDateTime) > 0;
    }

    private long resolveMainBalance(
            final Long userId,
            final UserAssetProfile profile
    ) {
        return userAccountRepository.findByUserIdAndAccountType(userId, AccountType.MAIN)
                .map(UserAccount::getBalanceSnapshot)
                .orElse(profile.getMainAccountBalanceAmount());
    }

    private long resolveSeedmoneyBalance(final Long userId) {
        return userAccountRepository.findByUserIdAndAccountType(userId, AccountType.SEEDMONEY)
                .map(UserAccount::getBalanceSnapshot)
                .orElse(0L);
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
            final long reserveAmount,
            final long monthlyExpenseAmount
    ) {
        if (reserveAmount <= 0) {
            return 0.0;
        }
        if (monthlyExpenseAmount <= 0) {
            return 1.0;
        }
        return clampRatio((double) reserveAmount / (monthlyExpenseAmount * 6.0));
    }

    private double normalizeCashflowHealth(
            final long monthlyIncomeAmount,
            final long monthlyExpenseAmount
    ) {
        if (monthlyIncomeAmount <= 0) {
            return 0.0;
        }
        return clampRatio((double) (monthlyIncomeAmount - monthlyExpenseAmount) / monthlyIncomeAmount);
    }

    private double normalizeCashflowBurden(
            final long monthlyIncomeAmount,
            final long monthlyExpenseAmount
    ) {
        if (monthlyIncomeAmount <= 0) {
            return monthlyExpenseAmount > 0 ? 1.0 : 0.0;
        }
        return clampRatio((double) monthlyExpenseAmount / monthlyIncomeAmount);
    }

    private double normalizeDebtBurden(
            final long loanAmount,
            final long monthlyIncomeAmount
    ) {
        if (loanAmount <= 0) {
            return 0.0;
        }
        if (monthlyIncomeAmount <= 0) {
            return 1.0;
        }
        return clampRatio((double) loanAmount / (monthlyIncomeAmount * 12.0));
    }

    private double normalizeAssetDebtBurden(
            final long loanAmount,
            final long totalAssetAmount
    ) {
        if (loanAmount <= 0) {
            return 0.0;
        }
        if (totalAssetAmount <= 0) {
            return 1.0;
        }
        return clampRatio((double) loanAmount / totalAssetAmount);
    }

    private boolean isWithinRange(
            final LocalDateTime value,
            final LocalDateTime start,
            final LocalDateTime inclusiveEnd
    ) {
        return value != null && !value.isBefore(start) && !value.isAfter(inclusiveEnd);
    }

    private long totalDepositAmount(final Long userId) {
        return userAssetDepositRepository.findAllByUserIdOrderByIdAsc(userId).stream()
                .mapToLong(item -> item.getAmount().longValue())
                .sum();
    }

    private long totalLoanAmount(final Long userId) {
        return userAssetLoanRepository.findAllByUserIdOrderByIdAsc(userId).stream()
                .mapToLong(item -> item.getAmount().longValue())
                .sum();
    }

    private long totalOtherIncomeAmount(final Long userId) {
        return userAssetOtherIncomeRepository.findAllByUserIdOrderByIdAsc(userId).stream()
                .mapToLong(item -> item.getAmount().longValue())
                .sum();
    }

    private long totalCardSpendAmount(final Long userId) {
        return userAssetCardSpendRepository.findAllByUserIdOrderByIdAsc(userId).stream()
                .mapToLong(item -> item.getAmount().longValue())
                .sum();
    }

    private boolean hasCardSpendInput(final Long userId) {
        return !userAssetCardSpendRepository.findAllByUserIdOrderByIdAsc(userId).isEmpty();
    }

    private int proportional(
            final double ratio,
            final int min,
            final int max
    ) {
        return clamp((int) Math.round(max * clampRatio(ratio)), min, max);
    }

    private int clamp(
            final int value,
            final int min,
            final int max
    ) {
        return Math.max(min, Math.min(value, max));
    }

    private double clampRatio(final double value) {
        return Math.max(0.0, Math.min(value, 1.0));
    }
}
