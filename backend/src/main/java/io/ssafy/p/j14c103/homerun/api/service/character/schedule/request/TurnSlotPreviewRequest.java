package io.ssafy.p.j14c103.homerun.api.service.character.schedule.request;

import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionType;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.TurnSlotPreviewPolicy.RequestedSlot;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;

public record TurnSlotPreviewRequest(
    GameStat gameStat,
    List<TurnSlotRequest> slots
) {

    public TurnSlotPreviewRequest {
        if (gameStat == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
        if (slots == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
        if (slots.stream().anyMatch(java.util.Objects::isNull)) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
        slots = List.copyOf(slots);
    }

    public static TurnSlotPreviewRequest of(
        final GameStat gameStat,
        final List<TurnSlotRequest> slots
    ) {
        return new TurnSlotPreviewRequest(gameStat, slots);
    }

    public List<RequestedSlot> toRequestedSlots() {
        return slots.stream()
            .map(slot -> RequestedSlot.of(slot.slotIndex(), slot.actionType()))
            .toList();
    }

    public record TurnSlotRequest(
        int slotIndex,
        ActionType actionType
    ) {

        public TurnSlotRequest {
            if (actionType == null) {
                throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
            }
        }

        public static TurnSlotRequest of(final int slotIndex, final ActionType actionType) {
            return new TurnSlotRequest(slotIndex, actionType);
        }
    }
}
