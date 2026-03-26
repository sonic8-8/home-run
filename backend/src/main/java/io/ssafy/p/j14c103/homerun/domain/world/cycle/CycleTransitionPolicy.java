package io.ssafy.p.j14c103.homerun.domain.world.cycle;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.springframework.stereotype.Component;

@Component
public class CycleTransitionPolicy {

    private static final int MIN_ROLL = 1;
    private static final int MAX_ROLL = 100;

    private static final int BOOM_STAY_MAX = 55;
    private static final int BOOM_TO_CRISIS_MAX = 80;

    private static final int CRISIS_TO_BOOM_MAX = 15;
    private static final int CRISIS_STAY_MAX = 40;

    private static final int RECOVERY_TO_BOOM_MAX = 65;
    private static final int RECOVERY_TO_CRISIS_MAX = 75;

    private static final int FINANCIAL_CRISIS_WEIGHT = 18;
    private static final int CURRENCY_CRISIS_WEIGHT = 15;
    private static final int TECH_BUBBLE_WEIGHT = 15;
    private static final int OIL_SHOCK_WEIGHT = 14;
    private static final int STAGFLATION_WEIGHT = 13;
    private static final int PANDEMIC_WEIGHT = 12;
    private static final int GREAT_DEPRESSION_WEIGHT = 3;
    private static final int CRISIS_TOTAL_WEIGHT = FINANCIAL_CRISIS_WEIGHT
        + CURRENCY_CRISIS_WEIGHT
        + TECH_BUBBLE_WEIGHT
        + OIL_SHOCK_WEIGHT
        + STAGFLATION_WEIGHT
        + PANDEMIC_WEIGHT
        + GREAT_DEPRESSION_WEIGHT;
    private static final int RECOVERY_RATE_HIKE_MAX = 50;

    public CyclePhase nextPhase(
        CyclePhase currentPhase,
        int roll // 사이클 전환 판정에 쓰는 랜덤 값 (주사위를 한 번 굴려 나온 값)
    ) {
        validateRoll(roll);
        validateCurrentPhase(currentPhase);

        if (currentPhase == CyclePhase.BOOM) {
            return nextPhaseFromBoom(roll);
        }
        if (currentPhase == CyclePhase.CRISIS) {
            return nextPhaseFromCrisis(roll);
        }
        return nextPhaseFromRecovery(roll);
    }

    public String descriptionOf(CyclePhase phase) {
        validateCurrentPhase(phase);

        if (phase == CyclePhase.BOOM) {
            return "경기 호황기";
        }
        if (phase == CyclePhase.CRISIS) {
            return "경기 위기";
        }
        return "경기 회복기";
    }

    public CycleState nextState(
        final CycleState currentState,
        final CycleDecisionRolls rolls
    ) {
        validateCurrentState(currentState);
        validateRolls(rolls);

        if (currentState.getRemainingTurns() > 1) {
            return currentState.decrementRemainingTurns();
        }

        final CyclePhase nextPhase = nextPhase(currentState.getPhase(), rolls.getPhaseRoll());
        final CycleType nextType = nextType(nextPhase, rolls.getSubtypeRoll());
        final int nextDuration = nextDuration(nextType, rolls.getDurationRoll());

        return CycleState.of(nextPhase, nextType, nextDuration);
    }

    private void validateRoll(int roll) {
        if (roll < MIN_ROLL || roll > MAX_ROLL) {
            throw new HomerunException(ErrorCode.WORLD_CYCLE_INPUT_INVALID);
        }
    }

    private void validateCurrentPhase(CyclePhase currentPhase) {
        if (currentPhase == null) {
            throw new HomerunException(ErrorCode.WORLD_CYCLE_INPUT_INVALID);
        }
    }

    private void validateCurrentState(final CycleState currentState) {
        if (currentState == null) {
            throw new HomerunException(ErrorCode.WORLD_CYCLE_INPUT_INVALID);
        }
    }

    private void validateRolls(final CycleDecisionRolls rolls) {
        if (rolls == null) {
            throw new HomerunException(ErrorCode.WORLD_CYCLE_INPUT_INVALID);
        }
    }

    private CyclePhase nextPhaseFromBoom(int roll) {
        if (roll <= BOOM_STAY_MAX) {
            return CyclePhase.BOOM;
        }
        if (roll <= BOOM_TO_CRISIS_MAX) {
            return CyclePhase.CRISIS;
        }
        return CyclePhase.RECOVERY;
    }

    private CyclePhase nextPhaseFromCrisis(int roll) {
        if (roll <= CRISIS_TO_BOOM_MAX) {
            return CyclePhase.BOOM;
        }
        if (roll <= CRISIS_STAY_MAX) {
            return CyclePhase.CRISIS;
        }
        return CyclePhase.RECOVERY;
    }

    private CyclePhase nextPhaseFromRecovery(int roll) {
        if (roll <= RECOVERY_TO_BOOM_MAX) {
            return CyclePhase.BOOM;
        }
        if (roll <= RECOVERY_TO_CRISIS_MAX) {
            return CyclePhase.CRISIS;
        }
        return CyclePhase.RECOVERY;
    }

    private CycleType nextType(final CyclePhase nextPhase, final int subtypeRoll) {
        validateCurrentPhase(nextPhase);
        validateRoll(subtypeRoll);

        if (nextPhase == CyclePhase.BOOM) {
            return CycleType.CYCLE_BOOM;
        }
        if (nextPhase == CyclePhase.CRISIS) {
            return nextCrisisType(subtypeRoll);
        }
        return nextRecoveryType(subtypeRoll);
    }

    private CycleType nextCrisisType(final int roll) {
        final int weightedRoll = scaleRoll(roll, CRISIS_TOTAL_WEIGHT);

        if (weightedRoll <= FINANCIAL_CRISIS_WEIGHT) {
            return CycleType.CYCLE_FINANCIAL_CRISIS;
        }
        if (weightedRoll <= FINANCIAL_CRISIS_WEIGHT + CURRENCY_CRISIS_WEIGHT) {
            return CycleType.CYCLE_CURRENCY_CRISIS;
        }
        if (weightedRoll <= FINANCIAL_CRISIS_WEIGHT + CURRENCY_CRISIS_WEIGHT + TECH_BUBBLE_WEIGHT) {
            return CycleType.CYCLE_TECH_BUBBLE;
        }
        if (weightedRoll <= FINANCIAL_CRISIS_WEIGHT + CURRENCY_CRISIS_WEIGHT + TECH_BUBBLE_WEIGHT
            + OIL_SHOCK_WEIGHT) {
            return CycleType.CYCLE_OIL_SHOCK;
        }
        if (weightedRoll <= FINANCIAL_CRISIS_WEIGHT + CURRENCY_CRISIS_WEIGHT + TECH_BUBBLE_WEIGHT
            + OIL_SHOCK_WEIGHT + STAGFLATION_WEIGHT) {
            return CycleType.CYCLE_STAGFLATION;
        }
        if (weightedRoll <= FINANCIAL_CRISIS_WEIGHT + CURRENCY_CRISIS_WEIGHT + TECH_BUBBLE_WEIGHT
            + OIL_SHOCK_WEIGHT + STAGFLATION_WEIGHT + PANDEMIC_WEIGHT) {
            return CycleType.CYCLE_PANDEMIC;
        }
        return CycleType.CYCLE_GREAT_DEPRESSION;
    }

    private CycleType nextRecoveryType(final int roll) {
        if (roll <= RECOVERY_RATE_HIKE_MAX) {
            return CycleType.CYCLE_RATE_HIKE;
        }
        return CycleType.CYCLE_GEOPOLITICAL;
    }

    private int nextDuration(final CycleType nextType, final int durationRoll) {
        validateType(nextType);
        validateRoll(durationRoll);

        final int minDuration = nextType.getMinDurationTurns();
        final int maxDuration = nextType.getMaxDurationTurns();
        if (minDuration == maxDuration) {
            return minDuration;
        }

        final int durationRange = maxDuration - minDuration + 1;
        final int scaledOffset = scaleRoll(durationRoll, durationRange) - 1;
        return minDuration + scaledOffset;
    }

    private void validateType(final CycleType cycleType) {
        if (cycleType == null) {
            throw new HomerunException(ErrorCode.WORLD_CYCLE_INPUT_INVALID);
        }
    }

    private int scaleRoll(final int roll, final int totalBuckets) {
        return ((roll - 1) * totalBuckets) / MAX_ROLL + 1;
    }
}
