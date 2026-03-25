package io.ssafy.p.j14c103.homerun.api.service.world;

import io.ssafy.p.j14c103.homerun.api.service.world.result.EventResolveResult;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventChoice;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventChoiceRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventEffect;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventEffectRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventPresentationType;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEvent;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEventRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.GamePendingEvent;
import io.ssafy.p.j14c103.homerun.domain.world.event.GamePendingEventRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WorldEventResolveService {

    private final GameSessionRepository gameSessionRepository;
    private final GamePendingEventRepository gamePendingEventRepository;
    private final GameEventRepository gameEventRepository;
    private final EventChoiceRepository eventChoiceRepository;
    private final EventEffectRepository eventEffectRepository;

    public EventResolveResult resolveEvent(
        final Long gameSessionId,
        final int eventId,
        final Integer choiceId
    ) {
        validateSession(gameSessionId);

        final GamePendingEvent pendingEvent = findPendingEvent(eventId);
        validatePendingEvent(gameSessionId, pendingEvent);

        final GameEvent gameEvent = findGameEvent(pendingEvent);
        validatePresentationType(pendingEvent, gameEvent);

        final EventChoice selectedChoice = resolveChoice(gameEvent, choiceId);
        final List<EventResolveResult.ResolvedEffect> resultEffects = resolveEffects(
            gameEvent.getGameEventId(),
            selectedChoice
        );

        return EventResolveResult.of(
            pendingEvent.getGamePendingEventId(),
            pendingEvent.getGameSessionId(),
            pendingEvent.getTurnNumber(),
            gameEvent.getGameEventId(),
            selectedChoice == null ? null : selectedChoice.getEventChoiceId(),
            selectedChoice == null ? null : selectedChoice.getChoiceCode(),
            resultEffects
        );
    }

    private void validateSession(final Long gameSessionId) {
        gameSessionRepository.findById(gameSessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.WORLD_SESSION_NOT_FOUND));
    }

    private GamePendingEvent findPendingEvent(final int eventId) {
        return gamePendingEventRepository.findById(eventId)
            .orElseThrow(() -> new HomerunException(ErrorCode.INVALID_INPUT_VALUE));
    }

    private void validatePendingEvent(
        final Long gameSessionId,
        final GamePendingEvent pendingEvent
    ) {
        if (!pendingEvent.getGameSessionId().equals(gameSessionId)) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (pendingEvent.isResolvedYn()) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private GameEvent findGameEvent(final GamePendingEvent pendingEvent) {
        return gameEventRepository.findById(pendingEvent.getGameEventId())
            .orElseThrow(() -> new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID));
    }

    private void validatePresentationType(
        final GamePendingEvent pendingEvent,
        final GameEvent gameEvent
    ) {
        if (pendingEvent.getEventPresentationType() != gameEvent.getEventPresentationType()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private EventChoice resolveChoice(
        final GameEvent gameEvent,
        final Integer choiceId
    ) {
        if (isChoiceRequired(gameEvent.getEventPresentationType())) {
            if (choiceId == null) {
                throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
            }
            return findChoice(gameEvent.getGameEventId(), choiceId);
        }
        if (choiceId == null) {
            return null;
        }
        return findChoice(gameEvent.getGameEventId(), choiceId);
    }

    private boolean isChoiceRequired(final EventPresentationType presentationType) {
        if (presentationType == EventPresentationType.CHOICE) {
            return true;
        }
        if (presentationType == EventPresentationType.PHONE) {
            return true;
        }
        return presentationType == EventPresentationType.JOB_TRANSFER;
    }

    private EventChoice findChoice(
        final Integer gameEventId,
        final Integer choiceId
    ) {
        return eventChoiceRepository.findByEventChoiceIdAndGameEventId(choiceId, gameEventId)
            .orElseThrow(() -> new HomerunException(ErrorCode.INVALID_INPUT_VALUE));
    }

    private List<EventResolveResult.ResolvedEffect> resolveEffects(
        final Integer gameEventId,
        final EventChoice selectedChoice
    ) {
        return eventEffectRepository.findAllByGameEventIdOrderByEffectOrderAscEventEffectIdAsc(gameEventId).stream()
            .filter(effect -> matchesEffect(effect, selectedChoice))
            .map(this::toResolvedEffect)
            .toList();
    }

    private boolean matchesEffect(
        final EventEffect eventEffect,
        final EventChoice selectedChoice
    ) {
        if (eventEffect.getEventChoiceId() == null) {
            return true;
        }
        if (selectedChoice == null) {
            return false;
        }
        return eventEffect.getEventChoiceId().equals(selectedChoice.getEventChoiceId());
    }

    private EventResolveResult.ResolvedEffect toResolvedEffect(final EventEffect eventEffect) {
        return EventResolveResult.ResolvedEffect.of(
            eventEffect.getEffectOrder(),
            eventEffect.getApplicationTimingType(),
            eventEffect.getTargetTableName(),
            eventEffect.getTargetColumnName(),
            eventEffect.getOperationType(),
            eventEffect.getBaseNumberValue(),
            eventEffect.getMinNumberValue(),
            eventEffect.getMaxNumberValue(),
            eventEffect.getBaseTextValue(),
            eventEffect.getDurationTurns(),
            eventEffect.getNote()
        );
    }
}
