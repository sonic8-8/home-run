package io.ssafy.p.j14c103.homerun.api.service.game.turn.request;

import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.ActionType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.TurnDraftSlot;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SubmitTurnSlotsServiceRequest {

    private List<TurnSlotRequest> slots;

    @Builder(access = AccessLevel.PRIVATE)
    private SubmitTurnSlotsServiceRequest(final List<TurnSlotRequest> slots) {
        validateSlots(slots);
        this.slots = List.copyOf(slots);
    }

    public static SubmitTurnSlotsServiceRequest of(final List<TurnSlotRequest> slots) {
        return SubmitTurnSlotsServiceRequest.builder()
            .slots(slots)
            .build();
    }

    public List<TurnDraftSlot> toTurnDraftSlots() {
        return slots.stream()
            .map(slot -> TurnDraftSlot.of(slot.getSlotIndex(), slot.getActionType()))
            .toList();
    }

    private static void validateSlots(final List<TurnSlotRequest> slots) {
        if (slots == null || slots.size() != 3) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }

        final boolean hasNullSlot = slots.stream().anyMatch(slot -> slot == null);
        if (hasNullSlot) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }

        final long distinctSlotCount = slots.stream()
            .map(TurnSlotRequest::getSlotIndex)
            .distinct()
            .count();

        if (distinctSlotCount != slots.size()) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    @Getter
    @NoArgsConstructor
    public static class TurnSlotRequest {

        private Integer slotIndex;
        private ActionType actionType;

        @Builder(access = AccessLevel.PRIVATE)
        private TurnSlotRequest(
            final Integer slotIndex,
            final ActionType actionType
        ) {
            validate(slotIndex, actionType);
            this.slotIndex = slotIndex;
            this.actionType = actionType;
        }

        public static TurnSlotRequest of(
            final Integer slotIndex,
            final ActionType actionType
        ) {
            return TurnSlotRequest.builder()
                .slotIndex(slotIndex)
                .actionType(actionType)
                .build();
        }

        private static void validate(
            final Integer slotIndex,
            final ActionType actionType
        ) {
            if (slotIndex == null || slotIndex < 0 || slotIndex > 2 || actionType == null) {
                throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }
    }
}
