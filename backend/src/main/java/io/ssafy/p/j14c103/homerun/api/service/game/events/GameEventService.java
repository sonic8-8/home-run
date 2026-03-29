package io.ssafy.p.j14c103.homerun.api.service.game.events;

import io.ssafy.p.j14c103.homerun.api.service.game.events.request.ResolveEventServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.events.response.PendingEventsResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.events.response.ResolveEventResponse;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.api.service.world.WorldEventResolveExecutionService;
import io.ssafy.p.j14c103.homerun.api.service.world.WorldPendingEventProviderService;
import io.ssafy.p.j14c103.homerun.api.service.world.response.PendingEventsProviderResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.result.EventResolveExecutionResult;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameEventService {

    private final GameSessionRepository gameSessionRepository;
    private final UserAuthContextService userAuthContextService;
    private final WorldPendingEventProviderService worldPendingEventProviderService;
    private final WorldEventResolveExecutionService worldEventResolveExecutionService;

    @Transactional(readOnly = true)
    public PendingEventsResponse getPendingEvents(final Long userId, final Long sessionId) {
        userAuthContextService.getContext(userId);
        final GameSession gameSession = getOwnedGameSession(userId, sessionId);
        final PendingEventsProviderResponse response = worldPendingEventProviderService.getPendingEvents(
            gameSession.getGameSessionId()
        );
        return PendingEventsResponse.from(response);
    }

    @Transactional
    public ResolveEventResponse resolveEvent(
        final Long userId,
        final Long sessionId,
        final Integer eventId,
        final ResolveEventServiceRequest request
    ) {
        userAuthContextService.getContext(userId);
        final GameSession gameSession = getOwnedGameSession(userId, sessionId);
        final EventResolveExecutionResult response = worldEventResolveExecutionService.resolveEvent(
            gameSession.getGameSessionId(),
            eventId,
            request.getChoiceId()
        );
        return ResolveEventResponse.from(response);
    }

    private GameSession getOwnedGameSession(final Long userId, final Long sessionId) {
        final GameSession gameSession = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.GAME_SESSION_NOT_FOUND));
        gameSession.assertOwner(userId);
        return gameSession;
    }
}
