package io.ssafy.p.j14c103.homerun.domain.character.schedule;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.concurrent.ThreadLocalRandom;

public class SideJobIncomePolicy {

    private static final int MIN_KNOWLEDGE = 0;
    private static final int MAX_KNOWLEDGE = 100;
    private static final int LOW_KNOWLEDGE_MAX = 30;
    private static final int MID_KNOWLEDGE_MAX = 60;
    private static final int ROUNDING_UNIT = 10_000;

    private final IncomeRandomizer incomeRandomizer;

    public SideJobIncomePolicy() {
        this((minimumIncome, maximumIncome) ->
            ThreadLocalRandom.current().nextInt(minimumIncome, maximumIncome + 1)
        );
    }

    SideJobIncomePolicy(final IncomeRandomizer incomeRandomizer) {
        if (incomeRandomizer == null) {
            throwSchedulePolicyInvalid();
        }

        this.incomeRandomizer = incomeRandomizer;
    }

    public IncomePreview resolvePreview(final int knowledge) {
        final IncomeRange incomeRange = resolveRange(knowledge);

        if (isRandomTier(knowledge)) {
            return IncomePreview.range(
                incomeRange.minimumIncome(),
                incomeRange.maximumIncome()
            );
        }

        return IncomePreview.fixed(calculateDeterministicIncome(knowledge));
    }

    public int calculateIncome(final int knowledge) {
        final IncomeRange incomeRange = resolveRange(knowledge);

        if (isRandomTier(knowledge)) {
            return incomeRandomizer.nextInt(
                incomeRange.minimumIncome(),
                incomeRange.maximumIncome()
            );
        }

        return calculateDeterministicIncome(knowledge);
    }

    public IncomeRange resolveRange(final int knowledge) {
        validateKnowledge(knowledge);

        if (knowledge <= LOW_KNOWLEDGE_MAX) {
            return new IncomeRange("배달·단순 알바", 200_000, 350_000);
        }

        if (knowledge <= MID_KNOWLEDGE_MAX) {
            return new IncomeRange("과외·프리랜서", 300_000, 500_000);
        }

        return new IncomeRange("컨설팅·강의", 600_000, 1_000_000);
    }

    private int calculateDeterministicIncome(final int knowledge) {
        if (knowledge <= MID_KNOWLEDGE_MAX) {
            return calculateRoundedInterpolatedIncome(
                knowledge,
                LOW_KNOWLEDGE_MAX + 1,
                MID_KNOWLEDGE_MAX,
                300_000,
                500_000
            );
        }

        return calculateRoundedInterpolatedIncome(
            knowledge,
            MID_KNOWLEDGE_MAX + 1,
            MAX_KNOWLEDGE,
            600_000,
            1_000_000
        );
    }

    private int calculateRoundedInterpolatedIncome(
        final int knowledge,
        final int startKnowledge,
        final int endKnowledge,
        final int startIncome,
        final int endIncome
    ) {
        final int rawIncome = interpolate(
            knowledge,
            startKnowledge,
            endKnowledge,
            startIncome,
            endIncome
        );

        return clampToRange(roundToNearestTenThousand(rawIncome), startIncome, endIncome);
    }

    private int interpolate(
        final int value,
        final int startValue,
        final int endValue,
        final int startAmount,
        final int endAmount
    ) {
        final long numerator = (long) (value - startValue) * (endAmount - startAmount);
        final long denominator = endValue - startValue;

        return startAmount + (int) (numerator / denominator);
    }

    private int roundToNearestTenThousand(final int amount) {
        return ((amount + (ROUNDING_UNIT / 2)) / ROUNDING_UNIT) * ROUNDING_UNIT;
    }

    private int clampToRange(final int amount, final int minimumIncome, final int maximumIncome) {
        if (amount < minimumIncome) {
            return minimumIncome;
        }

        if (amount > maximumIncome) {
            return maximumIncome;
        }

        return amount;
    }

    private boolean isRandomTier(final int knowledge) {
        return knowledge <= LOW_KNOWLEDGE_MAX;
    }

    private void validateKnowledge(final int knowledge) {
        if (knowledge < MIN_KNOWLEDGE || knowledge > MAX_KNOWLEDGE) {
            throw new HomerunException(ErrorCode.CHARACTER_STAT_INVALID);
        }
    }

    @FunctionalInterface
    interface IncomeRandomizer {

        int nextInt(int minimumIncome, int maximumIncome);
    }

    public record IncomeRange(
        String sideJobKind,
        int minimumIncome,
        int maximumIncome
    ) {

        public IncomeRange {
            if (sideJobKind == null || sideJobKind.isBlank()) {
                throwSchedulePolicyInvalid();
            }
            if (minimumIncome < 0) {
                throwSchedulePolicyInvalid();
            }
            if (maximumIncome < 0) {
                throwSchedulePolicyInvalid();
            }
            if (minimumIncome > maximumIncome) {
                throwSchedulePolicyInvalid();
            }
        }

        public boolean contains(final int income) {
            return income >= minimumIncome && income <= maximumIncome;
        }
    }

    public record IncomePreview(
        int minimumIncome,
        int maximumIncome
    ) {

        public IncomePreview {
            if (minimumIncome < 0) {
                throwSchedulePolicyInvalid();
            }
            if (maximumIncome < 0) {
                throwSchedulePolicyInvalid();
            }
            if (minimumIncome > maximumIncome) {
                throwSchedulePolicyInvalid();
            }
        }

        public static IncomePreview fixed(final int exactIncome) {
            return new IncomePreview(exactIncome, exactIncome);
        }

        public static IncomePreview range(final int minimumIncome, final int maximumIncome) {
            return new IncomePreview(minimumIncome, maximumIncome);
        }

        public boolean isRange() {
            return minimumIncome != maximumIncome;
        }
    }

    private static void throwSchedulePolicyInvalid() {
        throw new HomerunException(ErrorCode.CHARACTER_SCHEDULE_POLICY_INVALID);
    }
}
