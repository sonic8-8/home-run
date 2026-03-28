package io.ssafy.p.j14c103.homerun.api.service.world;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.ssafy.p.j14c103.homerun.api.service.world.result.GameWorldResult;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousing;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousingRepository;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleDecisionRolls;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleState;
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

    private static final String TURN_COMMIT_DURATION_METRIC = "homerun.turn.commit.duration";
    private static final String BOUNDARY_TAG = "boundary";
    private static final String RESULT_TAG = "result";
    private static final String WORLD_RESULT_BOUNDARY = "world-result";

    private final GameSessionRepository gameSessionRepository;
    private final GameHousingRepository gameHousingRepository;
    private final CycleTransitionPolicy cycleTransitionPolicy;
    private final MeterRegistry meterRegistry;

    public GameWorldResult buildWorldResult(final Long gameSessionId, final int roll) {
        return buildWorldResult(gameSessionId, CycleDecisionRolls.of(roll, roll, roll));
    }

    public GameWorldResult buildWorldResult(
        final Long gameSessionId,
        final CycleDecisionRolls rolls
    ) {
        final Timer.Sample sample = Timer.start(meterRegistry);
        try {
            final GameSession gameSession = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new HomerunException(ErrorCode.WORLD_SESSION_NOT_FOUND));
            final CycleState currentState = requireCycleState(gameSession);
            final CycleState nextState = cycleTransitionPolicy.nextState(currentState, rolls);
            final String description = cycleTransitionPolicy.descriptionOf(nextState.getPhase());

            final GameWorldResult result = GameWorldResult.of(
                GameWorldResult.CycleResult.of(
                    nextState.getPhase(),
                    nextState.getType(),
                    nextState.getRemainingTurns(),
                    description
                ),
                List.of(),
                List.of(),
                buildHousingSnapshot(gameSession)
            );
            sample.stop(turnCommitTimer("success"));
            return result;
        } catch (RuntimeException exception) {
            sample.stop(turnCommitTimer("failure"));
            throw exception;
        }
    }

    private Timer turnCommitTimer(final String result) {
        return Timer.builder(TURN_COMMIT_DURATION_METRIC)
            .tag(BOUNDARY_TAG, WORLD_RESULT_BOUNDARY)
            .tag(RESULT_TAG, result)
            .register(meterRegistry);
    }

    private CycleState requireCycleState(final GameSession gameSession) {
        if (gameSession.getCyclePhase() == null
            || gameSession.getCycleType() == null
            || gameSession.getCycleRemainingTurns() == null) {
            throw new HomerunException(ErrorCode.WORLD_CYCLE_STATE_INVALID);
        }
        try {
            return CycleState.of(
                gameSession.getCyclePhase(),
                gameSession.getCycleType(),
                gameSession.getCycleRemainingTurns()
            );
        } catch (HomerunException exception) {
            throw new HomerunException(ErrorCode.WORLD_CYCLE_STATE_INVALID, exception);
        }
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
