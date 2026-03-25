package io.ssafy.p.j14c103.homerun.api.service.world;

import io.ssafy.p.j14c103.homerun.api.service.world.result.GameWorldResult;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousing;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousingRepository;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleTransitionPolicy;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GameWorldResultService {

    private final GameSessionRepository gameSessionRepository;
    private final GameHousingRepository gameHousingRepository;
    private final CycleTransitionPolicy cycleTransitionPolicy;

    public GameWorldResult buildWorldResult(final Long gameSessionId, final int roll) {
        final GameSession gameSession = gameSessionRepository.findById(gameSessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.WORLD_SESSION_NOT_FOUND));
        final CyclePhase currentPhase = requireCyclePhase(gameSession);
        final CyclePhase nextPhase = cycleTransitionPolicy.nextPhase(currentPhase, roll);
        final String description = cycleTransitionPolicy.descriptionOf(nextPhase);

        return GameWorldResult.of(
            GameWorldResult.CycleResult.of(nextPhase, description),
            List.of(),
            List.of(),
            buildHousingSnapshot(gameSession)
        );
    }

    private CyclePhase requireCyclePhase(final GameSession gameSession) {
        if (gameSession.getCyclePhase() == null) {
            throw new HomerunException(ErrorCode.WORLD_CYCLE_STATE_INVALID);
        }
        return gameSession.getCyclePhase();
    }

    private GameWorldResult.HousingSnapshot buildHousingSnapshot(final GameSession gameSession) {
        return gameHousingRepository.findByGameSessionId(gameSession.getGameSessionId())
            .map(gameHousing -> toHousingSnapshot(gameSession, gameHousing))
            .orElseGet(() -> GameWorldResult.HousingSnapshot.of(
                null,
                null,
                gameSession.getTargetPropertyId(),
                false
            ));
    }

    private GameWorldResult.HousingSnapshot toHousingSnapshot(
        final GameSession gameSession,
        final GameHousing gameHousing
    ) {
        return GameWorldResult.HousingSnapshot.of(
            gameHousing.getCurrentHousingType(),
            gameHousing.getCurrentPropertyId(),
            gameSession.getTargetPropertyId(),
            false
        );
    }
}
