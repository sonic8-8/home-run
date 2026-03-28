package io.ssafy.p.j14c103.homerun.domain.gamesession.turn;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.Getter;

@Getter
public class TurnDraftSlot {

    private static final int MIN_SLOT_INDEX = 0;
    private static final int MAX_SLOT_INDEX = 2;

    private final Integer slotIndex;
    private final ActionType actionType;

    private TurnDraftSlot(
        final Integer slotIndex,
        final ActionType actionType
    ) {
        validateSlotIndex(slotIndex);
        validateActionType(actionType);

        this.slotIndex = slotIndex;
        this.actionType = actionType;
    }

    public static TurnDraftSlot of(
        final Integer slotIndex,
        final ActionType actionType
    ) {
        return new TurnDraftSlot(slotIndex, actionType);
    }

    private static void validateSlotIndex(final Integer slotIndex) {
        if (slotIndex == null || slotIndex < MIN_SLOT_INDEX || slotIndex > MAX_SLOT_INDEX) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private static void validateActionType(final ActionType actionType) {
        if (actionType == null) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
