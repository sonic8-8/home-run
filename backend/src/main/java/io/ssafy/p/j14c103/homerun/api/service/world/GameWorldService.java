package io.ssafy.p.j14c103.homerun.api.service.world;

import io.ssafy.p.j14c103.homerun.api.service.world.response.GameTurnResponse;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleTransitionPolicy;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GameWorldService {

    private final GameSessionRepository gameSessionRepository;
    private final CycleTransitionPolicy cycleTransitionPolicy;

    public GameTurnResponse getTurn(final Long gameSessionId) {
        final GameSession gameSession = gameSessionRepository.findById(gameSessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.WORLD_SESSION_NOT_FOUND));
        final CyclePhase cyclePhase = requireCyclePhase(gameSession);
        final String description = cycleTransitionPolicy.descriptionOf(cyclePhase);

        return GameTurnResponse.of(
            gameSession.getCurrentTurn(),
            gameSession.getCurrentDate(),
            GameTurnResponse.EconomicCycleResponse.of(cyclePhase, description),
            GameTurnResponse.NewsResponse.emptyList()
        );
    }

    private CyclePhase requireCyclePhase(final GameSession gameSession) {
        if (gameSession.getCyclePhase() == null) {
            throw new HomerunException(ErrorCode.WORLD_CYCLE_STATE_INVALID);
        }
        return gameSession.getCyclePhase();
    }
}
