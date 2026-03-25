package io.ssafy.p.j14c103.homerun.api.service.world;

import io.ssafy.p.j14c103.homerun.api.service.world.response.PendingEventsProviderResponse;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventChoice;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventChoiceRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEvent;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEventRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.GamePendingEvent;
import io.ssafy.p.j14c103.homerun.domain.world.event.GamePendingEventRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WorldPendingEventProviderService {

    private static final String DESCRIPTION = "description";
    private static final String SENDER = "sender";
    private static final String RECEIVER = "receiver";
    private static final String DATE = "date";
    private static final String OFFERED_SALARY = "offeredSalary";
    private static final String CURRENT_SALARY = "currentSalary";

    private final GameSessionRepository gameSessionRepository;
    private final GamePendingEventRepository gamePendingEventRepository;
    private final GameEventRepository gameEventRepository;
    private final EventChoiceRepository eventChoiceRepository;

    public PendingEventsProviderResponse getPendingEvents(final Long gameSessionId) {
        gameSessionRepository.findById(gameSessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.WORLD_SESSION_NOT_FOUND));

        final List<GamePendingEvent> pendingEvents = gamePendingEventRepository
            .findAllByGameSessionIdAndResolvedYnFalseOrderByCreatedAtAscGamePendingEventIdAsc(gameSessionId);

        if (pendingEvents.isEmpty()) {
            return PendingEventsProviderResponse.empty();
        }

        final Map<Integer, GameEvent> gameEvents = findGameEvents(pendingEvents);
        final Map<Integer, List<PendingEventsProviderResponse.PendingEventChoiceItem>> eventChoices = findEventChoices(
            pendingEvents
        );

        return PendingEventsProviderResponse.of(
            pendingEvents.stream()
                .map(pendingEvent -> toPendingEventItem(
                    pendingEvent,
                    gameEvents,
                    eventChoices
                ))
                .toList()
        );
    }

    private PendingEventsProviderResponse.PendingEventItem toPendingEventItem(
        final GamePendingEvent pendingEvent,
        final Map<Integer, GameEvent> gameEvents,
        final Map<Integer, List<PendingEventsProviderResponse.PendingEventChoiceItem>> eventChoices
    ) {
        final GameEvent gameEvent = requireGameEvent(gameEvents, pendingEvent.getGameEventId());
        final List<PendingEventsProviderResponse.PendingEventChoiceItem> choices = eventChoices.getOrDefault(
            gameEvent.getGameEventId(),
            List.of()
        );

        return PendingEventsProviderResponse.PendingEventItem.of(
            pendingEvent.getGamePendingEventId(),
            gameEvent.getEventPresentationType(),
            requireTitle(gameEvent),
            resolveDescription(pendingEvent.getPayload(), gameEvent),
            gameEvent.getImageUrl(),
            choices,
            resolveString(pendingEvent.getPayload(), SENDER),
            resolveString(pendingEvent.getPayload(), RECEIVER),
            resolveDate(pendingEvent.getPayload()),
            resolveInteger(pendingEvent.getPayload(), OFFERED_SALARY),
            resolveInteger(pendingEvent.getPayload(), CURRENT_SALARY)
        );
    }

    private Map<Integer, GameEvent> findGameEvents(final List<GamePendingEvent> pendingEvents) {
        final List<Integer> gameEventIds = pendingEvents.stream()
            .map(GamePendingEvent::getGameEventId)
            .distinct()
            .toList();

        final Map<Integer, GameEvent> gameEvents = new LinkedHashMap<>();
        for (GameEvent gameEvent : gameEventRepository.findAllByGameEventIdIn(gameEventIds)) {
            gameEvents.put(gameEvent.getGameEventId(), gameEvent);
        }
        return gameEvents;
    }

    private Map<Integer, List<PendingEventsProviderResponse.PendingEventChoiceItem>> findEventChoices(
        final List<GamePendingEvent> pendingEvents
    ) {
        final List<Integer> gameEventIds = pendingEvents.stream()
            .map(GamePendingEvent::getGameEventId)
            .distinct()
            .toList();

        final Map<Integer, List<PendingEventsProviderResponse.PendingEventChoiceItem>> eventChoices = new LinkedHashMap<>();
        for (EventChoice eventChoice : eventChoiceRepository
            .findAllByGameEventIdInOrderByGameEventIdAscChoiceOrderAsc(gameEventIds)) {
            eventChoices.computeIfAbsent(eventChoice.getGameEventId(), key -> new ArrayList<>())
                .add(toPendingEventChoiceItem(eventChoice));
        }
        return eventChoices;
    }

    private GameEvent requireGameEvent(
        final Map<Integer, GameEvent> gameEvents,
        final Integer gameEventId
    ) {
        if (gameEvents.containsKey(gameEventId)) {
            return gameEvents.get(gameEventId);
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private PendingEventsProviderResponse.PendingEventChoiceItem toPendingEventChoiceItem(
        final EventChoice eventChoice
    ) {
        return PendingEventsProviderResponse.PendingEventChoiceItem.of(
            eventChoice.getEventChoiceId(),
            eventChoice.getChoiceCode(),
            eventChoice.getChoiceName(),
            eventChoice.getChoiceDescription()
        );
    }

    private String requireTitle(final GameEvent gameEvent) {
        if (gameEvent.getEventName() == null || gameEvent.getEventName().isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }

        return gameEvent.getEventName();
    }

    private String resolveDescription(
        final Map<String, Object> payload,
        final GameEvent gameEvent
    ) {
        final String payloadDescription = resolveString(payload, DESCRIPTION);
        if (payloadDescription != null) {
            return payloadDescription;
        }
        if (gameEvent.getDescription() == null || gameEvent.getDescription().isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }

        return gameEvent.getDescription();
    }

    private String resolveString(final Map<String, Object> payload, final String key) {
        if (payload == null || !payload.containsKey(key)) {
            return null;
        }

        final Object value = payload.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof String stringValue) {
            return stringValue;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private Integer resolveInteger(final Map<String, Object> payload, final String key) {
        if (payload == null || !payload.containsKey(key)) {
            return null;
        }

        final Object value = payload.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer integerValue) {
            return integerValue;
        }
        if (value instanceof Number numberValue) {
            return numberValue.intValue();
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private LocalDate resolveDate(final Map<String, Object> payload) {
        if (payload == null || !payload.containsKey(DATE)) {
            return null;
        }

        final Object value = payload.get(DATE);
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate localDateValue) {
            return localDateValue;
        }
        if (value instanceof String stringValue) {
            try {
                return LocalDate.parse(stringValue);
            } catch (DateTimeParseException exception) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID, exception);
            }
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }
}
