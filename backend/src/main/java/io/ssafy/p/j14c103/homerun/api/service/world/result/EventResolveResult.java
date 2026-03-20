package io.ssafy.p.j14c103.homerun.api.service.world.result;

import java.util.List;
import lombok.Getter;

@Getter
public class EventResolveResult {

    private final Integer pendingEventId;
    private final Integer gameSessionId;
    private final Integer turnNumber;
    private final Integer gameEventId;
    private final Integer eventChoiceId;
    private final String selectedChoiceCode;
    private final List<ResolvedEffect> resultEffects;

    private EventResolveResult(
        final Integer pendingEventId,
        final Integer gameSessionId,
        final Integer turnNumber,
        final Integer gameEventId,
        final Integer eventChoiceId,
        final String selectedChoiceCode,
        final List<ResolvedEffect> resultEffects
    ) {
        this.pendingEventId = pendingEventId;
        this.gameSessionId = gameSessionId;
        this.turnNumber = turnNumber;
        this.gameEventId = gameEventId;
        this.eventChoiceId = eventChoiceId;
        this.selectedChoiceCode = selectedChoiceCode;
        this.resultEffects = resultEffects;
    }

    public static EventResolveResult of(
        final Integer pendingEventId,
        final Integer gameSessionId,
        final Integer turnNumber,
        final Integer gameEventId,
        final Integer eventChoiceId,
        final String selectedChoiceCode,
        final List<ResolvedEffect> resultEffects
    ) {
        return new EventResolveResult(
            pendingEventId,
            gameSessionId,
            turnNumber,
            gameEventId,
            eventChoiceId,
            selectedChoiceCode,
            resultEffects
        );
    }

    @Getter
    public static class ResolvedEffect {

        private final Integer effectOrder;
        private final String applicationTimingType;
        private final String targetTableName;
        private final String targetColumnName;
        private final String operationType;
        private final Integer baseNumberValue;
        private final Integer minNumberValue;
        private final Integer maxNumberValue;
        private final String baseTextValue;
        private final Integer durationTurns;
        private final String note;

        private ResolvedEffect(
            final Integer effectOrder,
            final String applicationTimingType,
            final String targetTableName,
            final String targetColumnName,
            final String operationType,
            final Integer baseNumberValue,
            final Integer minNumberValue,
            final Integer maxNumberValue,
            final String baseTextValue,
            final Integer durationTurns,
            final String note
        ) {
            this.effectOrder = effectOrder;
            this.applicationTimingType = applicationTimingType;
            this.targetTableName = targetTableName;
            this.targetColumnName = targetColumnName;
            this.operationType = operationType;
            this.baseNumberValue = baseNumberValue;
            this.minNumberValue = minNumberValue;
            this.maxNumberValue = maxNumberValue;
            this.baseTextValue = baseTextValue;
            this.durationTurns = durationTurns;
            this.note = note;
        }

        public static ResolvedEffect of(
            final Integer effectOrder,
            final String applicationTimingType,
            final String targetTableName,
            final String targetColumnName,
            final String operationType,
            final Integer baseNumberValue,
            final Integer minNumberValue,
            final Integer maxNumberValue,
            final String baseTextValue,
            final Integer durationTurns,
            final String note
        ) {
            return new ResolvedEffect(
                effectOrder,
                applicationTimingType,
                targetTableName,
                targetColumnName,
                operationType,
                baseNumberValue,
                minNumberValue,
                maxNumberValue,
                baseTextValue,
                durationTurns,
                note
            );
        }
    }
}
