package io.ssafy.p.j14c103.homerun.api.service.world;

import io.ssafy.p.j14c103.homerun.api.service.character.career.JobOfferQueryService;
import io.ssafy.p.j14c103.homerun.api.service.character.career.request.JobOfferQueryRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.career.response.JobOfferQueryResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.result.GameWorldResult;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.GameStatRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareerRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventPresentationType;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEvent;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEventRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.GamePendingEvent;
import io.ssafy.p.j14c103.homerun.domain.world.event.GamePendingEventRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class WorldPendingEventQueueService {

    private static final String SENDER = "sender";
    private static final String RECEIVER = "receiver";
    private static final String DESCRIPTION = "description";
    private static final String DATE = "date";
    private static final String OFFERED_SALARY = "offeredSalary";
    private static final String CURRENT_SALARY = "currentSalary";

    private final GameSessionRepository gameSessionRepository;
    private final GameStatRepository gameStatRepository;
    private final GameCareerRepository gameCareerRepository;
    private final GameEventRepository gameEventRepository;
    private final GamePendingEventRepository gamePendingEventRepository;
    private final JobOfferQueryService jobOfferQueryService;

    public void enqueuePendingEvents(
        final Long gameSessionId,
        final List<GameWorldResult.EventCandidate> eventCandidates
    ) {
        final GameSession gameSession = gameSessionRepository.findById(gameSessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.WORLD_SESSION_NOT_FOUND));

        validateEventCandidates(eventCandidates);

        if (eventCandidates.isEmpty()) {
            return;
        }

        final Integer turnNumber = requireTurnNumber(gameSession);
        final LocalDateTime baseCreatedAt = LocalDateTime.now();
        final List<GamePendingEvent> pendingEvents = new ArrayList<>();

        for (int index = 0; index < eventCandidates.size(); index++) {
            final GameWorldResult.EventCandidate eventCandidate = eventCandidates.get(index);
            final GameEvent gameEvent = findGameEvent(eventCandidate);
            final Map<String, Object> payload = buildPayload(gameSession, gameEvent);

            pendingEvents.add(GamePendingEvent.create(
                gameSessionId,
                turnNumber,
                gameEvent.getGameEventId(),
                gameEvent.getEventPresentationType(),
                payload,
                false,
                baseCreatedAt.plusNanos(index)
            ));
        }

        gamePendingEventRepository.saveAllAndFlush(pendingEvents);
    }

    private void validateEventCandidates(final List<GameWorldResult.EventCandidate> eventCandidates) {
        if (eventCandidates == null) {
            throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
        }
    }

    private Integer requireTurnNumber(final GameSession gameSession) {
        if (gameSession.getCurrentTurn() == null) {
            throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
        }

        return gameSession.getCurrentTurn();
    }

    private GameEvent findGameEvent(final GameWorldResult.EventCandidate eventCandidate) {
        final GameEvent gameEvent = gameEventRepository.findById(eventCandidate.getGameEventId())
            .orElseThrow(() -> new HomerunException(ErrorCode.WORLD_RESULT_INVALID));

        validateCandidate(gameEvent, eventCandidate);
        return gameEvent;
    }

    private void validateCandidate(
        final GameEvent gameEvent,
        final GameWorldResult.EventCandidate eventCandidate
    ) {
        if (!gameEvent.getEventCode().equals(eventCandidate.getEventCode())) {
            throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
        }
        if (!gameEvent.getEventName().equals(eventCandidate.getEventName())) {
            throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
        }
        if (gameEvent.getEventPresentationType() != eventCandidate.getEventPresentationType()) {
            throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
        }
    }

    private Map<String, Object> buildPayload(final GameSession gameSession, final GameEvent gameEvent) {
        return switch (gameEvent.getEventPresentationType()) {
            case PHONE -> buildPhonePayload(gameEvent);
            case JOB_TRANSFER -> buildJobTransferPayload(gameSession, gameEvent);
            case LETTER, GIFT -> buildLetterOrGiftPayload(gameEvent);
            case CHOICE -> buildChoicePayload(gameEvent);
        };
    }

    private Map<String, Object> buildPhonePayload(final GameEvent gameEvent) {
        return Map.of(DESCRIPTION, requireDescription(gameEvent));
    }

    private Map<String, Object> buildLetterOrGiftPayload(final GameEvent gameEvent) {
        return Map.of(DESCRIPTION, requireDescription(gameEvent));
    }

    private Map<String, Object> buildChoicePayload(final GameEvent gameEvent) {
        return Map.of(DESCRIPTION, requireDescription(gameEvent));
    }

    private Map<String, Object> buildJobTransferPayload(
        final GameSession gameSession,
        final GameEvent gameEvent
    ) {
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put(DESCRIPTION, requireDescription(gameEvent));

        if (hasText(gameEvent.getSenderName())) {
            payload.put(SENDER, gameEvent.getSenderName());
        }
        if (hasText(gameEvent.getReceiverName())) {
            payload.put(RECEIVER, gameEvent.getReceiverName());
        }
        if (gameSession.getCurrentDate() != null) {
            payload.put(DATE, gameSession.getCurrentDate().toString());
        }

        resolveJobTransferOffer(gameSession).ifPresent(offer -> {
            payload.put(OFFERED_SALARY, offer.offeredSalary());
            payload.put(CURRENT_SALARY, offer.currentSalary());
        });

        return payload;
    }

    private java.util.Optional<JobOfferQueryResponse.JobOfferResponse> resolveJobTransferOffer(
        final GameSession gameSession
    ) {
        final Integer gameId = gameSession.getGameSessionId().intValue();
        final java.util.Optional<GameCareer> gameCareer = gameCareerRepository.findById(gameId);
        final java.util.Optional<GameStat> gameStat = gameStatRepository.findById(gameId);

        if (gameCareer.isEmpty() || gameStat.isEmpty()) {
            return java.util.Optional.empty();
        }

        final JobOfferQueryResponse response = jobOfferQueryService.getJobOffers(
            JobOfferQueryRequest.of(
                gameCareer.get(),
                gameStat.get(),
                gameCareer.get().getRecentNetworkingCount()
            )
        );

        return response.offers().stream().findFirst();
    }

    private String requireDescription(final GameEvent gameEvent) {
        if (gameEvent.getDescription() == null || gameEvent.getDescription().isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }

        return gameEvent.getDescription();
    }

    private boolean hasText(final String value) {
        return value != null && !value.isBlank();
    }
}
