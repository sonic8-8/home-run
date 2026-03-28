package io.ssafy.p.j14c103.homerun.api.service.game.port;

import io.ssafy.p.j14c103.homerun.api.service.game.turn.request.SubmitTurnSlotsServiceRequest;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.ActionType;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import java.util.Map;
import lombok.Getter;

public interface TurnPreviewCalculator {

    TurnPreviewResult calculate(GameSession gameSession, SubmitTurnSlotsServiceRequest request);

    @Getter
    class TurnPreviewResult {

        private final List<PreviewSlot> slots;
        private final Money previewCashChange;
        private final Map<String, Integer> previewStatChanges;

        private TurnPreviewResult(
            final List<PreviewSlot> slots,
            final Money previewCashChange,
            final Map<String, Integer> previewStatChanges
        ) {
            validate(slots, previewCashChange, previewStatChanges);
            this.slots = List.copyOf(slots);
            this.previewCashChange = previewCashChange;
            this.previewStatChanges = Map.copyOf(previewStatChanges);
        }

        public static TurnPreviewResult of(
            final List<PreviewSlot> slots,
            final Money previewCashChange,
            final Map<String, Integer> previewStatChanges
        ) {
            return new TurnPreviewResult(slots, previewCashChange, previewStatChanges);
        }

        private static void validate(
            final List<PreviewSlot> slots,
            final Money previewCashChange,
            final Map<String, Integer> previewStatChanges
        ) {
            if (slots == null || previewCashChange == null || previewStatChanges == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
        }
    }

    @Getter
    class PreviewSlot {

        private final Integer slotIndex;
        private final ActionType actionType;
        private final boolean forcedAction;

        private PreviewSlot(
            final Integer slotIndex,
            final ActionType actionType,
            final boolean forcedAction
        ) {
            if (slotIndex == null || actionType == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            this.slotIndex = slotIndex;
            this.actionType = actionType;
            this.forcedAction = forcedAction;
        }

        public static PreviewSlot of(
            final Integer slotIndex,
            final ActionType actionType,
            final boolean forcedAction
        ) {
            return new PreviewSlot(slotIndex, actionType, forcedAction);
        }
    }
}
