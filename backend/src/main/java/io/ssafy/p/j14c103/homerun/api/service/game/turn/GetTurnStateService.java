package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import io.ssafy.p.j14c103.homerun.api.service.game.turn.response.TurnStateResponse;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.api.service.world.GameTurnWorldStateService;
import io.ssafy.p.j14c103.homerun.api.service.world.response.GameTurnWorldStateResponse;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetTurnStateService {

    private final GameSessionRepository gameSessionRepository;
    private final UserAuthContextService userAuthContextService;
    private final GameTurnWorldStateService gameTurnWorldStateService;

    @Transactional(readOnly = true)
    public TurnStateResponse getTurnState(final Long userId, final Long sessionId) {
        userAuthContextService.getContext(userId);
        final GameSession gameSession = getOwnedGameSession(userId, sessionId);
        final GameTurnWorldStateResponse turnWorldState = gameTurnWorldStateService.getWorldState(
            gameSession
        );

        return TurnStateResponse.of(
            gameSession.getCurrentTurn(),
            gameSession.getCurrentDate(),
            TurnStateResponse.EconomicCycleResponse.of(
                turnWorldState.getPhase(),
                turnWorldState.getDescription()
            ),
            TurnStateResponse.NewsResponse.emptyList()
        );
    }

    private GameSession getOwnedGameSession(final Long userId, final Long sessionId) {
        final GameSession gameSession = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.GAME_SESSION_NOT_FOUND));
        gameSession.assertOwner(userId);
        return gameSession;
    }
}
