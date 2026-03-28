package io.ssafy.p.j14c103.homerun.domain.gamesession.turn;

import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public class TurnDraft {

    private static final int MAX_SLOT_COUNT = 3;

    private final Long sessionId;
    private final Integer turnNumber;
    private final List<TurnDraftSlot> slots;
    private final Money previewCashChange;
    private final Map<String, Integer> previewStatChanges;

    private TurnDraft(
        final Long sessionId,
        final Integer turnNumber,
        final List<TurnDraftSlot> slots,
        final Money previewCashChange,
        final Map<String, Integer> previewStatChanges
    ) {
        validateSessionId(sessionId);
        validateTurnNumber(turnNumber);
        validateSlots(slots);
        validatePreviewCashChange(previewCashChange);
        validatePreviewStatChanges(previewStatChanges);

        this.sessionId = sessionId;
        this.turnNumber = turnNumber;
        this.slots = List.copyOf(slots);
        this.previewCashChange = previewCashChange;
        this.previewStatChanges = Map.copyOf(previewStatChanges);
    }

    public static TurnDraft of(
        final Long sessionId,
        final Integer turnNumber,
        final List<TurnDraftSlot> slots,
        final Money previewCashChange,
        final Map<String, Integer> previewStatChanges
    ) {
        return new TurnDraft(
            sessionId,
            turnNumber,
            slots,
            previewCashChange,
            previewStatChanges
        );
    }

    private static void validateSessionId(final Long sessionId) {
        if (sessionId == null || sessionId <= 0L) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private static void validateTurnNumber(final Integer turnNumber) {
        if (turnNumber == null || turnNumber < 0) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private static void validateSlots(final List<TurnDraftSlot> slots) {
        if (slots == null || slots.isEmpty() || slots.size() > MAX_SLOT_COUNT) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }

        final long distinctSlotCount = slots.stream()
            .map(TurnDraftSlot::getSlotIndex)
            .distinct()
            .count();

        if (distinctSlotCount != slots.size()) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private static void validatePreviewCashChange(final Money previewCashChange) {
        if (previewCashChange == null) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private static void validatePreviewStatChanges(final Map<String, Integer> previewStatChanges) {
        if (previewStatChanges == null) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }

        final boolean hasInvalidEntry = previewStatChanges.entrySet().stream()
            .anyMatch(entry -> entry.getKey() == null || entry.getKey().isBlank() || entry.getValue() == null);

        if (hasInvalidEntry) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
