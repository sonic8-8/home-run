package io.ssafy.p.j14c103.homerun.api.service.world;

import io.ssafy.p.j14c103.homerun.api.service.world.response.CareerCycleImpactProviderResponse;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareerRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleJobImpact;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleJobImpactPolicy;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleState;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WorldCareerCycleImpactProviderService {

    private final GameSessionRepository gameSessionRepository;
    private final GameCareerRepository gameCareerRepository;
    private final CycleJobImpactPolicy cycleJobImpactPolicy;

    public CareerCycleImpactProviderResponse getCareerCycleImpact(final Long gameSessionId) {
        final GameSession gameSession = gameSessionRepository.findById(gameSessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.WORLD_SESSION_NOT_FOUND));
        final CycleState cycleState = requireCycleState(gameSession);
        final GameCareer gameCareer = findGameCareer(gameSessionId);
        final JobType jobType = requireJobType(gameCareer);
        final CycleJobImpact cycleJobImpact = cycleJobImpactPolicy.resolve(
            cycleState.getType(),
            jobType
        );

        return CareerCycleImpactProviderResponse.from(
            cycleState.getType(),
            jobType,
            cycleJobImpact
        );
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
            if (exception.getErrorCode() == ErrorCode.WORLD_CYCLE_INPUT_INVALID) {
                throw new HomerunException(ErrorCode.WORLD_CYCLE_STATE_INVALID, exception);
            }
            throw exception;
        }
    }

    private GameCareer findGameCareer(final Long gameSessionId) {
        return gameCareerRepository.findById(toGameId(gameSessionId))
            .orElseThrow(() -> new HomerunException(ErrorCode.CHARACTER_STATE_UNINITIALIZED));
    }

    private int toGameId(final Long gameSessionId) {
        try {
            return Math.toIntExact(gameSessionId);
        } catch (ArithmeticException exception) {
            throw new HomerunException(ErrorCode.CHARACTER_GAME_ID_INVALID, exception);
        }
    }

    private JobType requireJobType(final GameCareer gameCareer) {
        final JobType jobType = gameCareer.getJobType();
        if (jobType != null) {
            return jobType;
        }
        throw new HomerunException(ErrorCode.CHARACTER_JOB_TYPE_UNSUPPORTED);
    }
}
