package io.ssafy.p.j14c103.homerun.api.service.game.events.response;

import io.ssafy.p.j14c103.homerun.api.service.world.result.EventResolveExecutionResult;
import io.ssafy.p.j14c103.homerun.api.service.world.result.EventResolveResult;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ResolveEventResponse {

    private final Integer eventId;
    private final Integer gameEventId;
    private final Integer choiceId;
    private final String selectedChoiceCode;
    private final List<ResolvedEffectResponse> resultEffects;
    private final String resultSummary;

    @Builder(access = AccessLevel.PRIVATE)
    private ResolveEventResponse(
        final Integer eventId,
        final Integer gameEventId,
        final Integer choiceId,
        final String selectedChoiceCode,
        final List<ResolvedEffectResponse> resultEffects,
        final String resultSummary
    ) {
        this.eventId = eventId;
        this.gameEventId = gameEventId;
        this.choiceId = choiceId;
        this.selectedChoiceCode = selectedChoiceCode;
        this.resultEffects = List.copyOf(resultEffects);
        this.resultSummary = resultSummary;
    }

    public static ResolveEventResponse of(
        final Integer eventId,
        final Integer gameEventId,
        final Integer choiceId,
        final String selectedChoiceCode,
        final List<ResolvedEffectResponse> resultEffects,
        final String resultSummary
    ) {
        return ResolveEventResponse.builder()
            .eventId(eventId)
            .gameEventId(gameEventId)
            .choiceId(choiceId)
            .selectedChoiceCode(selectedChoiceCode)
            .resultEffects(resultEffects)
            .resultSummary(resultSummary)
            .build();
    }

    public static ResolveEventResponse from(final EventResolveExecutionResult result) {
        return ResolveEventResponse.of(
            result.getPendingEventId(),
            result.getGameEventId(),
            result.getEventChoiceId(),
            result.getSelectedChoiceCode(),
            result.getResultEffects().stream()
                .map(ResolvedEffectResponse::from)
                .toList(),
            result.getResultSummary()
        );
    }

    @Getter
    public static class ResolvedEffectResponse {

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

        @Builder(access = AccessLevel.PRIVATE)
        private ResolvedEffectResponse(
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

        public static ResolvedEffectResponse of(
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
            return ResolvedEffectResponse.builder()
                .effectOrder(effectOrder)
                .applicationTimingType(applicationTimingType)
                .targetTableName(targetTableName)
                .targetColumnName(targetColumnName)
                .operationType(operationType)
                .baseNumberValue(baseNumberValue)
                .minNumberValue(minNumberValue)
                .maxNumberValue(maxNumberValue)
                .baseTextValue(baseTextValue)
                .durationTurns(durationTurns)
                .note(note)
                .build();
        }

        public static ResolvedEffectResponse from(final EventResolveResult.ResolvedEffect effect) {
            return ResolvedEffectResponse.of(
                effect.getEffectOrder(),
                effect.getApplicationTimingType(),
                effect.getTargetTableName(),
                effect.getTargetColumnName(),
                effect.getOperationType(),
                effect.getBaseNumberValue(),
                effect.getMinNumberValue(),
                effect.getMaxNumberValue(),
                effect.getBaseTextValue(),
                effect.getDurationTurns(),
                effect.getNote()
            );
        }
    }
}
