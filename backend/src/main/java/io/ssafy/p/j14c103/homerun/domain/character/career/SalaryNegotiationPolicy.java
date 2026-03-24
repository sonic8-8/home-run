package io.ssafy.p.j14c103.homerun.domain.character.career;

import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SalaryNegotiationPolicy {

    private static final int TURNS_PER_YEAR = 12;
    private static final int NEGOTIATION_START_TURN = 1;
    private static final int MIN_STAT = 0;
    private static final int MAX_STAT = 100;
    private static final String SUCCESS_MESSAGE = "연봉 협상에 성공했습니다!";

    private static final RaiseRateRange SMALL_BIZ_BASE_RAISE_RATE = RaiseRateRange.of(2, 5);
    private static final RaiseRateRange MID_BIZ_BASE_RAISE_RATE = RaiseRateRange.of(3, 6);
    private static final RaiseRateRange LARGE_BIZ_BASE_RAISE_RATE = RaiseRateRange.of(2, 4);
    // MVP1 docs omit STARTUP/FREELANCER base ranges, so these follow the growth-oriented track.
    private static final RaiseRateRange STARTUP_BASE_RAISE_RATE = RaiseRateRange.of(4, 7);
    private static final RaiseRateRange FREELANCER_BASE_RAISE_RATE = RaiseRateRange.of(3, 6);

    private static final RaiseRateRange NO_BONUS_RATE = RaiseRateRange.of(0, 0);
    private static final RaiseRateRange MID_KNOWLEDGE_BONUS_RATE = RaiseRateRange.of(1, 2);
    private static final RaiseRateRange HIGH_KNOWLEDGE_BONUS_RATE = RaiseRateRange.of(3, 5);
    private static final RaiseRateRange LOW_HEALTH_PENALTY_RATE = RaiseRateRange.of(1, 2);

    private final NegotiationPreparationPolicy negotiationPreparationPolicy =
        new NegotiationPreparationPolicy();

    public NegotiationResult negotiate(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final int currentTurn
    ) {
        validateRequest(gameCareer, gameStat, currentTurn);

        final JobType jobType = requireJobType(gameCareer.getJobType());
        final int previousSalary = requireSalary(gameCareer.getSalary());
        final int lastNegotiatedTurn = requireLastNegotiatedTurn(
            gameCareer.getLastNegotiatedTurn()
        );
        final int negotiationPreparationScore = requireNegotiationPreparationScore(
            gameCareer.getNegotiationPreparationScore()
        );
        final int knowledge = requireStat(gameStat.getKnowledge());
        final int health = requireStat(gameStat.getHealth());

        validateNegotiationWindow(lastNegotiatedTurn, currentTurn);

        final int raiseRate = resolveRaiseRate(
            jobType,
            negotiationPreparationScore,
            knowledge,
            health
        );
        final int newSalary = calculateNewSalary(previousSalary, raiseRate);

        return NegotiationResult.of(
            previousSalary,
            newSalary,
            raiseRate,
            currentTurn,
            SUCCESS_MESSAGE
        );
    }

    private void validateRequest(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final int currentTurn
    ) {
        if (gameCareer == null || gameStat == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
        if (currentTurn < NEGOTIATION_START_TURN) {
            throw new HomerunException(ErrorCode.CHARACTER_TURN_INVALID);
        }
    }

    private JobType requireJobType(final JobType jobType) {
        if (jobType == null) {
            throw new HomerunException(ErrorCode.CHARACTER_STATE_UNINITIALIZED);
        }

        return jobType;
    }

    private int requireSalary(final Integer salary) {
        return requireNonNegativeState(salary);
    }

    private int requireLastNegotiatedTurn(final Integer lastNegotiatedTurn) {
        return requireNonNegativeState(lastNegotiatedTurn);
    }

    private int requireNegotiationPreparationScore(final Integer negotiationPreparationScore) {
        return requireNonNegativeState(negotiationPreparationScore);
    }

    private int requireNonNegativeState(final Integer value) {
        if (value == null) {
            throw new HomerunException(ErrorCode.CHARACTER_STATE_UNINITIALIZED);
        }
        if (value < 0) {
            throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
        }

        return value;
    }

    private int requireStat(final Integer stat) {
        if (stat == null) {
            throw new HomerunException(ErrorCode.CHARACTER_STATE_UNINITIALIZED);
        }
        if (stat < MIN_STAT || stat > MAX_STAT) {
            throw new HomerunException(ErrorCode.CHARACTER_STAT_INVALID);
        }

        return stat;
    }

    private void validateNegotiationWindow(
        final int lastNegotiatedTurn,
        final int currentTurn
    ) {
        if (lastNegotiatedTurn == 0) {
            return;
        }
        if (currentTurn < lastNegotiatedTurn) {
            throw new HomerunException(ErrorCode.CHARACTER_TURN_INVALID);
        }
        if (currentTurn - lastNegotiatedTurn < TURNS_PER_YEAR) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }

    private int resolveRaiseRate(
        final JobType jobType,
        final int negotiationPreparationScore,
        final int knowledge,
        final int health
    ) {
        final int baseRaiseRate = resolveBaseRaiseRate(jobType).resolveActualRate();
        final int knowledgeBonusRate = resolveKnowledgeBonusRate(knowledge).resolveActualRate();
        final int preparationBonusRate = negotiationPreparationPolicy.resolveRaiseBonusRate(
            negotiationPreparationScore
        );
        final int healthPenaltyRate = resolveHealthPenaltyRate(health).resolveActualRate();

        return baseRaiseRate + knowledgeBonusRate + preparationBonusRate - healthPenaltyRate;
    }

    private RaiseRateRange resolveBaseRaiseRate(final JobType jobType) {
        if (jobType == JobType.SMALL_BIZ) {
            return SMALL_BIZ_BASE_RAISE_RATE;
        }
        if (jobType == JobType.MID_BIZ) {
            return MID_BIZ_BASE_RAISE_RATE;
        }
        if (jobType == JobType.LARGE_BIZ) {
            return LARGE_BIZ_BASE_RAISE_RATE;
        }
        if (jobType == JobType.STARTUP) {
            return STARTUP_BASE_RAISE_RATE;
        }
        if (jobType == JobType.FREELANCER) {
            return FREELANCER_BASE_RAISE_RATE;
        }

        throw new HomerunException(ErrorCode.CHARACTER_JOB_TYPE_UNSUPPORTED);
    }

    private RaiseRateRange resolveKnowledgeBonusRate(final int knowledge) {
        if (knowledge <= 30) {
            return NO_BONUS_RATE;
        }
        if (knowledge <= 60) {
            return MID_KNOWLEDGE_BONUS_RATE;
        }

        return HIGH_KNOWLEDGE_BONUS_RATE;
    }

    private RaiseRateRange resolveHealthPenaltyRate(final int health) {
        if (health >= 50) {
            return NO_BONUS_RATE;
        }

        return LOW_HEALTH_PENALTY_RATE;
    }

    private int calculateNewSalary(final int previousSalary, final int raiseRate) {
        return BigDecimal.valueOf(previousSalary)
            .multiply(BigDecimal.valueOf(100L + raiseRate))
            .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN)
            .intValueExact();
    }

    public record NegotiationResult(
        int previousSalary,
        int newSalary,
        int raiseRate,
        int lastNegotiatedTurn,
        String message
    ) {

        public NegotiationResult {
            validateNonNegative(previousSalary);
            validateNonNegative(newSalary);
            validateNonNegative(raiseRate);
            validateNonNegative(lastNegotiatedTurn);
            if (message == null || message.isBlank()) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
        }

        public static NegotiationResult of(
            final int previousSalary,
            final int newSalary,
            final int raiseRate,
            final int lastNegotiatedTurn,
            final String message
        ) {
            return new NegotiationResult(
                previousSalary,
                newSalary,
                raiseRate,
                lastNegotiatedTurn,
                message
            );
        }
    }

    public record RaiseRateRange(
        int minimumRate,
        int maximumRate
    ) {

        public RaiseRateRange {
            validateNonNegative(minimumRate);
            validateNonNegative(maximumRate);
            if (minimumRate > maximumRate) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
        }

        public static RaiseRateRange of(final int minimumRate, final int maximumRate) {
            return new RaiseRateRange(minimumRate, maximumRate);
        }

        private int resolveActualRate() {
            return maximumRate;
        }
    }

    private static void validateNonNegative(final int value) {
        if (value < 0) {
            throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
        }
    }
}
