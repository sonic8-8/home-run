package io.ssafy.p.j14c103.homerun.api.service.character.schedule.response;

import io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionCategory;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionType;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.TurnSlotPreviewPolicy;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class TurnSlotPreviewResponse {

    private final List<TurnSlotResponse> slots;
    private final StatPreviewResponse statPreview;
    private final CashPreviewResponse cashPreview;

    @Builder(access = AccessLevel.PRIVATE)
    private TurnSlotPreviewResponse(
        final List<TurnSlotResponse> slots,
        final StatPreviewResponse statPreview,
        final CashPreviewResponse cashPreview
    ) {
        validateRequest(slots, statPreview, cashPreview);

        this.slots = List.copyOf(slots);
        this.statPreview = statPreview;
        this.cashPreview = cashPreview;
    }

    public static TurnSlotPreviewResponse from(
        final TurnSlotPreviewPolicy.PreviewResult previewResult
    ) {
        return TurnSlotPreviewResponse.builder()
            .slots(previewResult.slots().stream()
                .map(TurnSlotResponse::from)
                .toList())
            .statPreview(StatPreviewResponse.from(previewResult.statPreview()))
            .cashPreview(CashPreviewResponse.from(previewResult.cashPreview()))
            .build();
    }

    private void validateRequest(
        final List<TurnSlotResponse> slots,
        final StatPreviewResponse statPreview,
        final CashPreviewResponse cashPreview
    ) {
        if (slots == null || statPreview == null || cashPreview == null) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
    }

    @Getter
    public static class TurnSlotResponse {

        private final int slotIndex;
        private final ActionType actionType;
        private final ActionCategory actionCategory;
        private final boolean forcedAction;

        @Builder(access = AccessLevel.PRIVATE)
        private TurnSlotResponse(
            final int slotIndex,
            final ActionType actionType,
            final ActionCategory actionCategory,
            final boolean forcedAction
        ) {
            validateRequest(actionType, actionCategory);

            this.slotIndex = slotIndex;
            this.actionType = actionType;
            this.actionCategory = actionCategory;
            this.forcedAction = forcedAction;
        }

        private static TurnSlotResponse from(final TurnSlotPreviewPolicy.PreviewSlot previewSlot) {
            return TurnSlotResponse.builder()
                .slotIndex(previewSlot.slotIndex())
                .actionType(previewSlot.actionType())
                .actionCategory(previewSlot.actionCategory())
                .forcedAction(previewSlot.forcedAction())
                .build();
        }

        private void validateRequest(
            final ActionType actionType,
            final ActionCategory actionCategory
        ) {
            if (actionType == null || actionCategory == null) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
        }
    }

    @Getter
    public static class StatPreviewResponse {

        private final int healthDelta;
        private final int fatigueDelta;
        private final int stressDelta;
        private final int happinessDelta;
        private final int knowledgeDelta;

        @Builder(access = AccessLevel.PRIVATE)
        private StatPreviewResponse(
            final int healthDelta,
            final int fatigueDelta,
            final int stressDelta,
            final int happinessDelta,
            final int knowledgeDelta
        ) {
            this.healthDelta = healthDelta;
            this.fatigueDelta = fatigueDelta;
            this.stressDelta = stressDelta;
            this.happinessDelta = happinessDelta;
            this.knowledgeDelta = knowledgeDelta;
        }

        private static StatPreviewResponse from(
            final TurnSlotPreviewPolicy.StatPreview statPreview
        ) {
            return StatPreviewResponse.builder()
                .healthDelta(statPreview.healthDelta())
                .fatigueDelta(statPreview.fatigueDelta())
                .stressDelta(statPreview.stressDelta())
                .happinessDelta(statPreview.happinessDelta())
                .knowledgeDelta(statPreview.knowledgeDelta())
                .build();
        }
    }

    @Getter
    public static class CashPreviewResponse {

        private final int minimumCashDelta;
        private final int maximumCashDelta;
        private final boolean rangePreview;

        @Builder(access = AccessLevel.PRIVATE)
        private CashPreviewResponse(
            final int minimumCashDelta,
            final int maximumCashDelta,
            final boolean rangePreview
        ) {
            this.minimumCashDelta = minimumCashDelta;
            this.maximumCashDelta = maximumCashDelta;
            this.rangePreview = rangePreview;
        }

        private static CashPreviewResponse from(
            final TurnSlotPreviewPolicy.CashPreview cashPreview
        ) {
            return CashPreviewResponse.builder()
                .minimumCashDelta(cashPreview.minimumCashDelta())
                .maximumCashDelta(cashPreview.maximumCashDelta())
                .rangePreview(cashPreview.rangePreview())
                .build();
        }
    }
}
