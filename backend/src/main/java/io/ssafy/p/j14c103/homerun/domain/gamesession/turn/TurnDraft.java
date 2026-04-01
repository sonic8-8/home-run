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
    private final Money previewCashMinChange;
    private final Money previewCashMaxChange;
    private final Map<String, Integer> previewStatChanges;

    private TurnDraft(
        final Long sessionId,
        final Integer turnNumber,
        final List<TurnDraftSlot> slots,
        final Money previewCashChange,
        final Money previewCashMinChange,
        final Money previewCashMaxChange,
        final Map<String, Integer> previewStatChanges
    ) {
        validateSessionId(sessionId);
        validateTurnNumber(turnNumber);
        validateSlots(slots);
        validatePreviewCashRange(previewCashChange, previewCashMinChange, previewCashMaxChange);
        validatePreviewStatChanges(previewStatChanges);

        this.sessionId = sessionId;
        this.turnNumber = turnNumber;
        this.slots = List.copyOf(slots);
        this.previewCashChange = previewCashChange;
        this.previewCashMinChange = previewCashMinChange;
        this.previewCashMaxChange = previewCashMaxChange;
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
            previewCashChange,
            previewCashChange,
            previewStatChanges
        );
    }

    public static TurnDraft of(
        final Long sessionId,
        final Integer turnNumber,
        final List<TurnDraftSlot> slots,
        final Money previewCashChange,
        final Money previewCashMinChange,
        final Money previewCashMaxChange,
        final Map<String, Integer> previewStatChanges
    ) {
        return new TurnDraft(
            sessionId,
            turnNumber,
            slots,
            previewCashChange,
            previewCashMinChange,
            previewCashMaxChange,
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

    private static void validatePreviewCashRange(
        final Money previewCashChange,
        final Money previewCashMinChange,
        final Money previewCashMaxChange
    ) {
        if (previewCashChange == null || previewCashMinChange == null || previewCashMaxChange == null) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (previewCashMinChange.isGreaterThan(previewCashMaxChange)) {
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
