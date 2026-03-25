package io.ssafy.p.j14c103.homerun.api.service.world.result;

import java.util.List;
import lombok.Getter;

@Getter
public class EventResolveExecutionResult {

    private final Integer pendingEventId;
    private final Integer gameEventId;
    private final Integer eventChoiceId;
    private final String selectedChoiceCode;
    private final List<EventResolveResult.ResolvedEffect> resultEffects;
    private final String resultSummary;

    private EventResolveExecutionResult(
        final Integer pendingEventId,
        final Integer gameEventId,
        final Integer eventChoiceId,
        final String selectedChoiceCode,
        final List<EventResolveResult.ResolvedEffect> resultEffects,
        final String resultSummary
    ) {
        this.pendingEventId = pendingEventId;
        this.gameEventId = gameEventId;
        this.eventChoiceId = eventChoiceId;
        this.selectedChoiceCode = selectedChoiceCode;
        this.resultEffects = List.copyOf(resultEffects);
        this.resultSummary = resultSummary;
    }

    public static EventResolveExecutionResult of(
        final Integer pendingEventId,
        final Integer gameEventId,
        final Integer eventChoiceId,
        final String selectedChoiceCode,
        final List<EventResolveResult.ResolvedEffect> resultEffects,
        final String resultSummary
    ) {
        return new EventResolveExecutionResult(
            pendingEventId,
            gameEventId,
            eventChoiceId,
            selectedChoiceCode,
            resultEffects,
            resultSummary
        );
    }
}
