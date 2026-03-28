package io.ssafy.p.j14c103.homerun.api.controller.game.turn.request;

import io.ssafy.p.j14c103.homerun.api.service.game.turn.request.SubmitTurnSlotsServiceRequest;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.ActionType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SubmitTurnSlotsRequest {

    @NotNull(message = "{validation.game.turn.submit.slots.notNull}")
    @Size(min = 3, max = 3, message = "{validation.game.turn.submit.slots.size}")
    @Valid
    private List<@NotNull(message = "{validation.game.turn.submit.slot.notNull}") TurnSlotRequest> slots;

    @Builder(access = AccessLevel.PRIVATE)
    private SubmitTurnSlotsRequest(final List<TurnSlotRequest> slots) {
        this.slots = slots;
    }

    public static SubmitTurnSlotsRequest of(final List<TurnSlotRequest> slots) {
        return SubmitTurnSlotsRequest.builder()
            .slots(slots)
            .build();
    }

    public SubmitTurnSlotsServiceRequest toServiceRequest() {
        if (slots == null) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return SubmitTurnSlotsServiceRequest.of(
            slots.stream()
                .map(TurnSlotRequest::toServiceRequest)
                .toList()
        );
    }

    @Getter
    @NoArgsConstructor
    public static class TurnSlotRequest {

        @NotNull(message = "{validation.game.turn.submit.slotIndex.notNull}")
        @Min(value = 0, message = "{validation.game.turn.submit.slotIndex.min}")
        @Max(value = 2, message = "{validation.game.turn.submit.slotIndex.max}")
        private Integer slotIndex;

        @NotBlank(message = "{validation.game.turn.submit.actionType.notBlank}")
        private String actionType;

        @Builder(access = AccessLevel.PRIVATE)
        private TurnSlotRequest(
            final Integer slotIndex,
            final String actionType
        ) {
            this.slotIndex = slotIndex;
            this.actionType = actionType;
        }

        public static TurnSlotRequest of(
            final Integer slotIndex,
            final String actionType
        ) {
            return TurnSlotRequest.builder()
                .slotIndex(slotIndex)
                .actionType(actionType)
                .build();
        }

        private SubmitTurnSlotsServiceRequest.TurnSlotRequest toServiceRequest() {
            return SubmitTurnSlotsServiceRequest.TurnSlotRequest.of(
                slotIndex,
                toActionType(actionType)
            );
        }

        private ActionType toActionType(final String actionType) {
            try {
                return ActionType.valueOf(actionType);
            } catch (IllegalArgumentException exception) {
                throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }
    }
}
