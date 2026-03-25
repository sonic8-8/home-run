package io.ssafy.p.j14c103.homerun.domain.world.cycle;

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

    private void validateRoll(int roll) {
        if (roll < MIN_ROLL || roll > MAX_ROLL) {
            throw new IllegalArgumentException("roll은 1 이상 100 이하이어야 합니다.");
        }
    }

    private void validateCurrentPhase(CyclePhase currentPhase) {
        if (currentPhase == null) {
            throw new IllegalArgumentException("currentPhase는 null일 수 없습니다.");
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
}
