package io.ssafy.p.j14c103.homerun.api.service.character.schedule.request;

import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionType;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.TurnSlotPreviewPolicy.RequestedSlot;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TurnSlotPreviewRequest {

    private GameStat gameStat;
    private List<TurnSlotRequest> slots;

    @Builder(access = AccessLevel.PRIVATE)
    private TurnSlotPreviewRequest(
        final GameStat gameStat,
        final List<TurnSlotRequest> slots
    ) {
        validateRequest(gameStat, slots);

        this.gameStat = gameStat;
        this.slots = List.copyOf(slots);
    }

    public static TurnSlotPreviewRequest of(
        final GameStat gameStat,
        final List<TurnSlotRequest> slots
    ) {
        return TurnSlotPreviewRequest.builder()
            .gameStat(gameStat)
            .slots(slots)
            .build();
    }

    public List<RequestedSlot> toRequestedSlots() {
        return slots.stream()
            .map(slot -> RequestedSlot.of(slot.getSlotIndex(), slot.getActionType()))
            .toList();
    }

    private void validateRequest(
        final GameStat gameStat,
        final List<TurnSlotRequest> slots
    ) {
        if (gameStat == null || slots == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
        if (slots.stream().anyMatch(Objects::isNull)) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class TurnSlotRequest {

        private int slotIndex;
        private ActionType actionType;

        @Builder(access = AccessLevel.PRIVATE)
        private TurnSlotRequest(
            final int slotIndex,
            final ActionType actionType
        ) {
            validateRequest(actionType);

            this.slotIndex = slotIndex;
            this.actionType = actionType;
        }

        public static TurnSlotRequest of(final int slotIndex, final ActionType actionType) {
            return TurnSlotRequest.builder()
                .slotIndex(slotIndex)
                .actionType(actionType)
                .build();
        }

        private void validateRequest(final ActionType actionType) {
            if (actionType == null) {
                throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
            }
        }
    }
}
