package io.ssafy.p.j14c103.homerun.domain.world.cycle;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.Getter;

@Getter
public class CycleState {

    private final CyclePhase phase;
    private final CycleType type;
    private final int remainingTurns;

    private CycleState(
        final CyclePhase phase,
        final CycleType type,
        final int remainingTurns
    ) {
        validatePhase(phase);
        validateType(type);
        validateMatchingPhase(phase, type);
        validateRemainingTurns(remainingTurns);

        this.phase = phase;
        this.type = type;
        this.remainingTurns = remainingTurns;
    }

    public static CycleState of(
        final CyclePhase phase,
        final CycleType type,
        final int remainingTurns
    ) {
        return new CycleState(phase, type, remainingTurns);
    }

    public CycleState decrementRemainingTurns() {
        return new CycleState(phase, type, remainingTurns - 1);
    }

    private void validatePhase(final CyclePhase phase) {
        if (phase == null) {
            throw new HomerunException(ErrorCode.WORLD_CYCLE_INPUT_INVALID);
        }
    }

    private void validateType(final CycleType type) {
        if (type == null) {
            throw new HomerunException(ErrorCode.WORLD_CYCLE_INPUT_INVALID);
        }
    }

    private void validateMatchingPhase(final CyclePhase phase, final CycleType type) {
        if (type.getPhase() != phase) {
            throw new HomerunException(ErrorCode.WORLD_CYCLE_INPUT_INVALID);
        }
    }

    private void validateRemainingTurns(final int remainingTurns) {
        if (remainingTurns < 1) {
            throw new HomerunException(ErrorCode.WORLD_CYCLE_INPUT_INVALID);
        }
    }
}
