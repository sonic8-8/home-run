package io.ssafy.p.j14c103.homerun.api.service.world;

import io.ssafy.p.j14c103.homerun.api.service.world.result.EventResolveResult;
import io.ssafy.p.j14c103.homerun.api.service.world.result.EventResolveExecutionResult;
import io.ssafy.p.j14c103.homerun.domain.history.event.GameEventLog;
import io.ssafy.p.j14c103.homerun.domain.history.event.GameEventLogRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventChoice;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventChoiceRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEvent;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEventRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.GamePendingEvent;
import io.ssafy.p.j14c103.homerun.domain.world.event.GamePendingEventRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class WorldEventResolveExecutionService {

    private static final String EFFECTS = "effects";

    private final WorldEventResolveService worldEventResolveService;
    private final GamePendingEventRepository gamePendingEventRepository;
    private final GameEventLogRepository gameEventLogRepository;
    private final GameEventRepository gameEventRepository;
    private final EventChoiceRepository eventChoiceRepository;

    public EventResolveExecutionResult resolveEvent(
        final int gameSessionId,
        final int eventId,
        final Integer choiceId
    ) {
        final EventResolveResult resolveResult = worldEventResolveService.resolveEvent(
            gameSessionId,
            eventId,
            choiceId
        );

        final GamePendingEvent pendingEvent = findPendingEvent(resolveResult.getPendingEventId());
        final GameEvent gameEvent = findGameEvent(resolveResult.getGameEventId());
        final EventChoice selectedChoice = findSelectedChoice(resolveResult.getEventChoiceId());
        final String resultSummary = buildResultSummary(
            gameEvent,
            selectedChoice,
            resolveResult.getResultEffects()
        );

        gameEventLogRepository.saveAndFlush(GameEventLog.create(
            resolveResult.getGameSessionId(),
            resolveResult.getTurnNumber(),
            resolveResult.getGameEventId(),
            resolveResult.getEventChoiceId(),
            resolveResult.getSelectedChoiceCode(),
            toResultEffectsSnapshot(resolveResult.getResultEffects()),
            resultSummary,
            LocalDateTime.now()
        ));

        pendingEvent.markResolved();
        gamePendingEventRepository.saveAndFlush(pendingEvent);

        return EventResolveExecutionResult.of(
            resolveResult.getPendingEventId(),
            resolveResult.getGameEventId(),
            resolveResult.getEventChoiceId(),
            resolveResult.getSelectedChoiceCode(),
            resolveResult.getResultEffects(),
            resultSummary
        );
    }

    private GamePendingEvent findPendingEvent(final Integer pendingEventId) {
        return gamePendingEventRepository.findById(pendingEventId)
            .orElseThrow(() -> new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID));
    }

    private GameEvent findGameEvent(final Integer gameEventId) {
        return gameEventRepository.findById(gameEventId)
            .orElseThrow(() -> new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID));
    }

    private EventChoice findSelectedChoice(final Integer eventChoiceId) {
        if (eventChoiceId == null) {
            return null;
        }
        return eventChoiceRepository.findById(eventChoiceId)
            .orElseThrow(() -> new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID));
    }

    private String buildResultSummary(
        final GameEvent gameEvent,
        final EventChoice selectedChoice,
        final List<EventResolveResult.ResolvedEffect> resultEffects
    ) {
        final String note = findPrimaryNote(resultEffects);
        if (note != null && selectedChoice != null) {
            return gameEvent.getEventName() + ": " + selectedChoice.getChoiceName() + " - " + note;
        }
        if (note != null) {
            return gameEvent.getEventName() + ": " + note;
        }
        if (selectedChoice != null) {
            return gameEvent.getEventName() + ": " + selectedChoice.getChoiceName() + " 결과가 적용되었습니다.";
        }
        return gameEvent.getEventName() + " 결과가 적용되었습니다.";
    }

    private String findPrimaryNote(final List<EventResolveResult.ResolvedEffect> resultEffects) {
        for (EventResolveResult.ResolvedEffect resultEffect : resultEffects) {
            if (resultEffect.getNote() != null && !resultEffect.getNote().isBlank()) {
                return resultEffect.getNote();
            }
        }
        return null;
    }

    private Map<String, Object> toResultEffectsSnapshot(
        final List<EventResolveResult.ResolvedEffect> resultEffects
    ) {
        final List<Map<String, Object>> effects = resultEffects.stream()
            .map(this::toEffectSnapshot)
            .toList();

        final Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put(EFFECTS, effects);
        return snapshot;
    }

    private Map<String, Object> toEffectSnapshot(final EventResolveResult.ResolvedEffect resultEffect) {
        final Map<String, Object> effect = new LinkedHashMap<>();
        effect.put("effectOrder", resultEffect.getEffectOrder());
        effect.put("applicationTimingType", resultEffect.getApplicationTimingType());
        effect.put("targetTableName", resultEffect.getTargetTableName());
        effect.put("targetColumnName", resultEffect.getTargetColumnName());
        effect.put("operationType", resultEffect.getOperationType());
        effect.put("baseNumberValue", resultEffect.getBaseNumberValue());
        effect.put("minNumberValue", resultEffect.getMinNumberValue());
        effect.put("maxNumberValue", resultEffect.getMaxNumberValue());
        effect.put("baseTextValue", resultEffect.getBaseTextValue());
        effect.put("durationTurns", resultEffect.getDurationTurns());
        effect.put("note", resultEffect.getNote());
        return effect;
    }
}
