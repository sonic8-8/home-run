package io.ssafy.p.j14c103.homerun.domain.gamesession.turn;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class SideJobIncomePolicy {

    private static final int MIN_KNOWLEDGE = 0;
    private static final int MAX_KNOWLEDGE = 100;
    private static final int LOW_KNOWLEDGE_MAX = 30;
    private static final int MID_KNOWLEDGE_MAX = 60;

    private static final int INCOME_STEP = 10_000;
    private static final int LOW_MIN_INCOME = 200_000;
    private static final int LOW_MAX_INCOME = 350_000;
    private static final int MID_MIN_INCOME = 300_000;
    private static final int MID_MAX_INCOME = 500_000;
    private static final int HIGH_MIN_INCOME = 600_000;
    private static final int HIGH_MAX_INCOME = 1_000_000;

    public SideJobIncomePreview previewIncome(final Integer knowledge) {
        validateKnowledge(knowledge);

        if (knowledge <= LOW_KNOWLEDGE_MAX) {
            return SideJobIncomePreview.range(LOW_MIN_INCOME, LOW_MAX_INCOME);
        }

        if (knowledge <= MID_KNOWLEDGE_MAX) {
            return SideJobIncomePreview.fixed(
                resolveDeterministicIncome(
                    knowledge,
                    LOW_KNOWLEDGE_MAX + 1,
                    MID_KNOWLEDGE_MAX,
                    MID_MIN_INCOME,
                    MID_MAX_INCOME
                )
            );
        }

        return SideJobIncomePreview.fixed(
            resolveDeterministicIncome(
                knowledge,
                MID_KNOWLEDGE_MAX + 1,
                MAX_KNOWLEDGE,
                HIGH_MIN_INCOME,
                HIGH_MAX_INCOME
            )
        );
    }

    private void validateKnowledge(final Integer knowledge) {
        if (knowledge == null) {
            throw new HomerunException(ErrorCode.SCHEDULE_KNOWLEDGE_INVALID);
        }

        if (knowledge < MIN_KNOWLEDGE || knowledge > MAX_KNOWLEDGE) {
            throw new HomerunException(ErrorCode.SCHEDULE_KNOWLEDGE_INVALID);
        }
    }

    public int calculateActualIncome(final Integer knowledge) {
        validateKnowledge(knowledge);

        if (knowledge <= LOW_KNOWLEDGE_MAX) {
            final int stepCount = ((LOW_MAX_INCOME - LOW_MIN_INCOME) / INCOME_STEP) + 1;
            return LOW_MIN_INCOME + (ThreadLocalRandom.current().nextInt(stepCount) * INCOME_STEP);
        }

        if (knowledge <= MID_KNOWLEDGE_MAX) {
            return resolveDeterministicIncome(
                knowledge,
                LOW_KNOWLEDGE_MAX + 1,
                MID_KNOWLEDGE_MAX,
                MID_MIN_INCOME,
                MID_MAX_INCOME
            );
        }

        return resolveDeterministicIncome(
            knowledge,
            MID_KNOWLEDGE_MAX + 1,
            MAX_KNOWLEDGE,
            HIGH_MIN_INCOME,
            HIGH_MAX_INCOME
        );
    }

    private int resolveDeterministicIncome(
        final int knowledge,
        final int minKnowledge,
        final int maxKnowledge,
        final int minIncome,
        final int maxIncome
    ) {
        if (knowledge <= minKnowledge) {
            return minIncome;
        }

        if (knowledge >= maxKnowledge) {
            return maxIncome;
        }

        final long scaledDelta = (long) (knowledge - minKnowledge) * (maxIncome - minIncome);
        final long rawIncome = minIncome + (scaledDelta / (maxKnowledge - minKnowledge));
        final int roundedIncome = (int) (((rawIncome + (INCOME_STEP / 2L)) / INCOME_STEP)
            * INCOME_STEP);
        return clampIncome(roundedIncome, minIncome, maxIncome);
    }

    private int clampIncome(
        final int income,
        final int minIncome,
        final int maxIncome
    ) {
        if (income < minIncome) {
            return minIncome;
        }

        if (income > maxIncome) {
            return maxIncome;
        }

        return income;
    }

    public record SideJobIncomePreview(
        int minAmount,
        int maxAmount,
        boolean rangePreview
    ) {

        public SideJobIncomePreview {
            validateAmount(minAmount);
            validateAmount(maxAmount);
            validateRange(minAmount, maxAmount, rangePreview);
        }

        public static SideJobIncomePreview fixed(final int amount) {
            return new SideJobIncomePreview(amount, amount, false);
        }

        public static SideJobIncomePreview range(final int minAmount, final int maxAmount) {
            return new SideJobIncomePreview(minAmount, maxAmount, true);
        }

        private static void validateAmount(final int amount) {
            if (amount < 0) {
                throw new HomerunException(ErrorCode.SCHEDULE_ACTION_CATALOG_INVALID);
            }
        }

        private static void validateRange(
            final int minAmount,
            final int maxAmount,
            final boolean rangePreview
        ) {
            if (minAmount > maxAmount) {
                throw new HomerunException(ErrorCode.SCHEDULE_ACTION_CATALOG_INVALID);
            }

            if (!rangePreview && minAmount != maxAmount) {
                throw new HomerunException(ErrorCode.SCHEDULE_ACTION_CATALOG_INVALID);
            }
        }
    }
}
