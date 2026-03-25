package io.ssafy.p.j14c103.homerun.api.service.character.schedule.response;

import io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionCategory;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionType;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.TurnSlotPreviewPolicy;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.Getter;

@Getter
public class TurnSlotPreviewResponse {

    private final List<TurnSlotResponse> slots;
    private final StatPreviewResponse statPreview;
    private final CashPreviewResponse cashPreview;

    private TurnSlotPreviewResponse(
        final List<TurnSlotResponse> slots,
        final StatPreviewResponse statPreview,
        final CashPreviewResponse cashPreview
    ) {
        if (slots == null || statPreview == null || cashPreview == null) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        this.slots = List.copyOf(slots);
        this.statPreview = statPreview;
        this.cashPreview = cashPreview;
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

    public List<TurnSlotResponse> slots() {
        return slots;
    }

    public StatPreviewResponse statPreview() {
        return statPreview;
    }

    public CashPreviewResponse cashPreview() {
        return cashPreview;
    }

    @Getter
    public static class TurnSlotResponse {

        private final int slotIndex;
        private final ActionType actionType;
        private final ActionCategory actionCategory;
        private final boolean forcedAction;

        private TurnSlotResponse(
            final int slotIndex,
            final ActionType actionType,
            final ActionCategory actionCategory,
            final boolean forcedAction
        ) {
            if (actionType == null || actionCategory == null) {
                throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
            }

            this.slotIndex = slotIndex;
            this.actionType = actionType;
            this.actionCategory = actionCategory;
            this.forcedAction = forcedAction;
        }

        private static TurnSlotResponse from(final TurnSlotPreviewPolicy.PreviewSlot previewSlot) {
            return new TurnSlotResponse(
                previewSlot.slotIndex(),
                previewSlot.actionType(),
                previewSlot.actionCategory(),
                previewSlot.forcedAction()
            );
        }

        public int slotIndex() {
            return slotIndex;
        }

        public ActionType actionType() {
            return actionType;
        }

        public ActionCategory actionCategory() {
            return actionCategory;
        }

        public boolean forcedAction() {
            return forcedAction;
        }
    }

    @Getter
    public static class StatPreviewResponse {

        private final int healthDelta;
        private final int fatigueDelta;
        private final int stressDelta;
        private final int happinessDelta;
        private final int knowledgeDelta;

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
            return new StatPreviewResponse(
                statPreview.healthDelta(),
                statPreview.fatigueDelta(),
                statPreview.stressDelta(),
                statPreview.happinessDelta(),
                statPreview.knowledgeDelta()
            );
        }

        public int healthDelta() {
            return healthDelta;
        }

        public int fatigueDelta() {
            return fatigueDelta;
        }

        public int stressDelta() {
            return stressDelta;
        }

        public int happinessDelta() {
            return happinessDelta;
        }

        public int knowledgeDelta() {
            return knowledgeDelta;
        }
    }

    @Getter
    public static class CashPreviewResponse {

        private final int minimumCashDelta;
        private final int maximumCashDelta;
        private final boolean rangePreview;

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
            return new CashPreviewResponse(
                cashPreview.minimumCashDelta(),
                cashPreview.maximumCashDelta(),
                cashPreview.rangePreview()
            );
        }

        public int minimumCashDelta() {
            return minimumCashDelta;
        }

        public int maximumCashDelta() {
            return maximumCashDelta;
        }

        public boolean rangePreview() {
            return rangePreview;
        }
    }
}
