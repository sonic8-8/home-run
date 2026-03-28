package io.ssafy.p.j14c103.homerun.api.service.world;

import io.ssafy.p.j14c103.homerun.api.service.world.response.GameTurnWorldStateResponse;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleTransitionPolicy;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameTurnWorldStateService {

    private final CycleTransitionPolicy cycleTransitionPolicy;

    public GameTurnWorldStateResponse getWorldState(final GameSession gameSession) {
        final CyclePhase cyclePhase = requireCyclePhase(gameSession);
        return GameTurnWorldStateResponse.of(
            cyclePhase,
            cycleTransitionPolicy.descriptionOf(cyclePhase)
        );
    }

    private CyclePhase requireCyclePhase(final GameSession gameSession) {
        if (gameSession.getCyclePhase() == null) {
            throw new HomerunException(ErrorCode.WORLD_CYCLE_STATE_INVALID);
        }
        return gameSession.getCyclePhase();
    }
}
