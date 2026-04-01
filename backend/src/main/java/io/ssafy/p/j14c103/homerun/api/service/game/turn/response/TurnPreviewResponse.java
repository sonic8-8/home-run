package io.ssafy.p.j14c103.homerun.api.service.game.turn.response;

import io.ssafy.p.j14c103.homerun.api.service.game.port.TurnPreviewCalculator.PreviewSlot;
import io.ssafy.p.j14c103.homerun.api.service.game.port.TurnPreviewCalculator.TurnPreviewResult;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class TurnPreviewResponse {

    private final List<PreviewSlotResponse> slots;
    private final Long previewCashChange;
    private final Long previewCashMinChange;
    private final Long previewCashMaxChange;
    private final PreviewStatChangesResponse previewStatChanges;

    @Builder(access = AccessLevel.PRIVATE)
    private TurnPreviewResponse(
        final List<PreviewSlotResponse> slots,
        final Long previewCashChange,
        final Long previewCashMinChange,
        final Long previewCashMaxChange,
        final PreviewStatChangesResponse previewStatChanges
    ) {
        if (
            slots == null
                || previewCashChange == null
                || previewCashMinChange == null
                || previewCashMaxChange == null
                || previewStatChanges == null
        ) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
        this.slots = List.copyOf(slots);
        this.previewCashChange = previewCashChange;
        this.previewCashMinChange = previewCashMinChange;
        this.previewCashMaxChange = previewCashMaxChange;
        this.previewStatChanges = previewStatChanges;
    }

    public static TurnPreviewResponse from(final TurnPreviewResult previewResult) {
        return TurnPreviewResponse.builder()
            .slots(previewResult.getSlots().stream()
                .map(PreviewSlotResponse::from)
                .toList())
            .previewCashChange(previewResult.getPreviewCashChange().getAmount().longValueExact())
            .previewCashMinChange(previewResult.getPreviewCashMinChange().getAmount().longValueExact())
            .previewCashMaxChange(previewResult.getPreviewCashMaxChange().getAmount().longValueExact())
            .previewStatChanges(PreviewStatChangesResponse.from(previewResult))
            .build();
    }

    public static TurnPreviewResponse of(
        final List<PreviewSlotResponse> slots,
        final Long previewCashChange,
        final PreviewStatChangesResponse previewStatChanges
    ) {
        return TurnPreviewResponse.of(
            slots,
            previewCashChange,
            previewCashChange,
            previewCashChange,
            previewStatChanges
        );
    }

    public static TurnPreviewResponse of(
        final List<PreviewSlotResponse> slots,
        final Long previewCashChange,
        final Long previewCashMinChange,
        final Long previewCashMaxChange,
        final PreviewStatChangesResponse previewStatChanges
    ) {
        return TurnPreviewResponse.builder()
            .slots(slots)
            .previewCashChange(previewCashChange)
            .previewCashMinChange(previewCashMinChange)
            .previewCashMaxChange(previewCashMaxChange)
            .previewStatChanges(previewStatChanges)
            .build();
    }

    @Getter
    public static class PreviewSlotResponse {

        private final Integer slotIndex;
        private final String actionType;
        private final boolean forcedAction;

        @Builder(access = AccessLevel.PRIVATE)
        private PreviewSlotResponse(
            final Integer slotIndex,
            final String actionType,
            final boolean forcedAction
        ) {
            if (slotIndex == null || actionType == null || actionType.isBlank()) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            this.slotIndex = slotIndex;
            this.actionType = actionType;
            this.forcedAction = forcedAction;
        }

        public static PreviewSlotResponse from(final PreviewSlot previewSlot) {
            return PreviewSlotResponse.builder()
                .slotIndex(previewSlot.getSlotIndex())
                .actionType(previewSlot.getActionType().name())
                .forcedAction(previewSlot.isForcedAction())
                .build();
        }

        public static PreviewSlotResponse of(
            final Integer slotIndex,
            final String actionType,
            final boolean forcedAction
        ) {
            return PreviewSlotResponse.builder()
                .slotIndex(slotIndex)
                .actionType(actionType)
                .forcedAction(forcedAction)
                .build();
        }
    }

    @Getter
    public static class PreviewStatChangesResponse {

        private final Integer health;
        private final Integer fatigue;
        private final Integer stress;
        private final Integer happiness;
        private final Integer knowledge;

        @Builder(access = AccessLevel.PRIVATE)
        private PreviewStatChangesResponse(
            final Integer health,
            final Integer fatigue,
            final Integer stress,
            final Integer happiness,
            final Integer knowledge
        ) {
            this.health = requireValue(health);
            this.fatigue = requireValue(fatigue);
            this.stress = requireValue(stress);
            this.happiness = requireValue(happiness);
            this.knowledge = requireValue(knowledge);
        }

        public static PreviewStatChangesResponse from(final TurnPreviewResult previewResult) {
            return PreviewStatChangesResponse.builder()
                .health(previewResult.getPreviewStatChanges().getOrDefault("health", 0))
                .fatigue(previewResult.getPreviewStatChanges().getOrDefault("fatigue", 0))
                .stress(previewResult.getPreviewStatChanges().getOrDefault("stress", 0))
                .happiness(previewResult.getPreviewStatChanges().getOrDefault("happiness", 0))
                .knowledge(previewResult.getPreviewStatChanges().getOrDefault("knowledge", 0))
                .build();
        }

        public static PreviewStatChangesResponse of(
            final Integer health,
            final Integer fatigue,
            final Integer stress,
            final Integer happiness,
            final Integer knowledge
        ) {
            return PreviewStatChangesResponse.builder()
                .health(health)
                .fatigue(fatigue)
                .stress(stress)
                .happiness(happiness)
                .knowledge(knowledge)
                .build();
        }

        private Integer requireValue(final Integer value) {
            if (value == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            return value;
        }
    }
}
