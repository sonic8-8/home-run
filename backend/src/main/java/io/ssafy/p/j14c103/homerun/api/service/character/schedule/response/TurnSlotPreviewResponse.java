package io.ssafy.p.j14c103.homerun.api.service.character.schedule.response;

import io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionCategory;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionType;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.TurnSlotPreviewPolicy;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;

public record TurnSlotPreviewResponse(
    List<TurnSlotResponse> slots,
    StatPreviewResponse statPreview,
    CashPreviewResponse cashPreview
) {

    public TurnSlotPreviewResponse {
        if (slots == null || statPreview == null || cashPreview == null) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        slots = List.copyOf(slots);
    }

    public static TurnSlotPreviewResponse from(
        final TurnSlotPreviewPolicy.PreviewResult previewResult
    ) {
        return new TurnSlotPreviewResponse(
            previewResult.slots().stream()
                .map(TurnSlotResponse::from)
                .toList(),
            StatPreviewResponse.from(previewResult.statPreview()),
            CashPreviewResponse.from(previewResult.cashPreview())
        );
    }

    public record TurnSlotResponse(
        int slotIndex,
        ActionType actionType,
        ActionCategory actionCategory,
        boolean forcedAction
    ) {

        public TurnSlotResponse {
            if (actionType == null || actionCategory == null) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }
        }

        private static TurnSlotResponse from(final TurnSlotPreviewPolicy.PreviewSlot previewSlot) {
            return new TurnSlotResponse(
                previewSlot.slotIndex(),
                previewSlot.actionType(),
                previewSlot.actionCategory(),
                previewSlot.forcedAction()
            );
        }
    }

    public record StatPreviewResponse(
        int healthDelta,
        int fatigueDelta,
        int stressDelta,
        int happinessDelta,
        int knowledgeDelta
    ) {

        private static StatPreviewResponse from(
            final TurnSlotPreviewPolicy.StatPreview statPreview
        ) {
            return new StatPreviewResponse(
                statPreview.healthDelta(),
                statPreview.fatigueDelta(),
                statPreview.stressDelta(),
                statPreview.happinessDelta(),
                statPreview.knowledgeDelta()
            );
        }
    }

    public record CashPreviewResponse(
        int minimumCashDelta,
        int maximumCashDelta,
        boolean rangePreview
    ) {

        private static CashPreviewResponse from(
            final TurnSlotPreviewPolicy.CashPreview cashPreview
        ) {
            return new CashPreviewResponse(
                cashPreview.minimumCashDelta(),
                cashPreview.maximumCashDelta(),
                cashPreview.rangePreview()
            );
        }
    }
}
