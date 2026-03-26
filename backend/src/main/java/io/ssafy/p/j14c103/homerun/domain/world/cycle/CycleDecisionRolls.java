package io.ssafy.p.j14c103.homerun.domain.world.cycle;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.Getter;

@Getter
public class CycleDecisionRolls {

    private static final int MIN_ROLL = 1;
    private static final int MAX_ROLL = 100;

    private final int phaseRoll;
    private final int subtypeRoll;
    private final int durationRoll;

    private CycleDecisionRolls(
        final int phaseRoll,
        final int subtypeRoll,
        final int durationRoll
    ) {
        validateRoll("phaseRoll", phaseRoll);
        validateRoll("subtypeRoll", subtypeRoll);
        validateRoll("durationRoll", durationRoll);

        this.phaseRoll = phaseRoll;
        this.subtypeRoll = subtypeRoll;
        this.durationRoll = durationRoll;
    }

    public static CycleDecisionRolls of(
        final int phaseRoll,
        final int subtypeRoll,
        final int durationRoll
    ) {
        return new CycleDecisionRolls(phaseRoll, subtypeRoll, durationRoll);
    }

    private void validateRoll(final String fieldName, final int roll) {
        if (roll < MIN_ROLL || roll > MAX_ROLL) {
            throw new HomerunException(ErrorCode.WORLD_CYCLE_INPUT_INVALID);
        }
    }
}
