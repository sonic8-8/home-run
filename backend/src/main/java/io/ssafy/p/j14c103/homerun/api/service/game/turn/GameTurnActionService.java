package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import io.ssafy.p.j14c103.homerun.api.service.game.turn.response.AvailableActionsResponse;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameTurnActionService {

    private final GameSessionRepository gameSessionRepository;
    private final UserAuthContextService userAuthContextService;
    private final AvailableActionReader availableActionReader;

    @Transactional(readOnly = true)
    public AvailableActionsResponse getAvailableActions(final Long userId, final Long sessionId) {
        userAuthContextService.getContext(userId);
        final GameSession gameSession = getOwnedGameSession(userId, sessionId);
        gameSession.assertInProgress();
        return AvailableActionsResponse.from(availableActionReader.read(gameSession));
    }

    private GameSession getOwnedGameSession(final Long userId, final Long sessionId) {
        final GameSession gameSession = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.GAME_SESSION_NOT_FOUND));
        gameSession.assertOwner(userId);
        return gameSession;
    }
}
