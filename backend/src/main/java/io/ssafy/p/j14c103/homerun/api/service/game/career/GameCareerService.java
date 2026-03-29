package io.ssafy.p.j14c103.homerun.api.service.game.career;

import io.ssafy.p.j14c103.homerun.api.service.character.career.JobOfferQueryService;
import io.ssafy.p.j14c103.homerun.api.service.character.career.JobTransferService;
import io.ssafy.p.j14c103.homerun.api.service.character.career.SalaryNegotiationService;
import io.ssafy.p.j14c103.homerun.api.service.character.career.request.JobOfferQueryRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.career.request.JobTransferServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.career.request.SalaryNegotiationServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.career.response.JobOfferQueryResponse;
import io.ssafy.p.j14c103.homerun.api.service.character.career.response.JobTransferServiceResponse;
import io.ssafy.p.j14c103.homerun.api.service.character.career.response.SalaryNegotiationResultResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.career.request.AcceptJobTransferServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.career.response.JobOfferListResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.career.response.JobTransferResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.career.response.SalaryNegotiationResponse;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.GameStatRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareerRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.character.career.SalaryNegotiationPolicy;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameCareerService {

    private final GameSessionRepository gameSessionRepository;
    private final GameCareerRepository gameCareerRepository;
    private final GameStatRepository gameStatRepository;
    private final UserAuthContextService userAuthContextService;
    private final SalaryNegotiationService salaryNegotiationService;
    private final JobOfferQueryService jobOfferQueryService;
    private final JobTransferService jobTransferService;
    private final EntityManager entityManager;

    @Transactional
    public SalaryNegotiationResponse negotiateSalary(final Long userId, final Long sessionId) {
        userAuthContextService.getContext(userId);
        final GameSession gameSession = getOwnedGameSession(userId, sessionId);
        final GameCareer gameCareer = getRequiredGameCareer(sessionId);
        final GameStat gameStat = getRequiredGameStat(sessionId);
        final Integer currentTurn = requirePlayableTurn(gameSession);
        final CyclePhase cyclePhase = requireCyclePhase(gameSession);

        try {
            final SalaryNegotiationResultResponse result = salaryNegotiationService.negotiate(
                SalaryNegotiationServiceRequest.of(
                    gameCareer,
                    gameStat,
                    currentTurn,
                    cyclePhase
                )
            );
            gameCareer.applySalaryNegotiation(SalaryNegotiationPolicy.NegotiationResult.of(
                result.previousSalary(),
                result.newSalary(),
                result.raiseRate(),
                result.lastNegotiatedTurn(),
                result.message()
            ));
            gameCareerRepository.saveAndFlush(gameCareer);
            return SalaryNegotiationResponse.from(result);
        } catch (HomerunException exception) {
            throw mapCharacterException(exception);
        }
    }

    @Transactional(readOnly = true)
    public JobOfferListResponse getJobOffers(final Long userId, final Long sessionId) {
        userAuthContextService.getContext(userId);
        final GameSession gameSession = getOwnedGameSession(userId, sessionId);
        final GameCareer gameCareer = getRequiredGameCareer(sessionId);
        final GameStat gameStat = getRequiredGameStat(sessionId);

        try {
            final JobOfferQueryResponse response = jobOfferQueryService.getJobOffers(
                JobOfferQueryRequest.of(
                    gameCareer,
                    gameStat,
                    requireRecentNetworkingCount(gameCareer)
                )
            );
            return JobOfferListResponse.from(response);
        } catch (HomerunException exception) {
            throw mapCharacterException(exception);
        }
    }

    @Transactional
    public JobTransferResponse transfer(
        final Long userId,
        final Long sessionId,
        final AcceptJobTransferServiceRequest request
    ) {
        userAuthContextService.getContext(userId);
        final GameSession gameSession = getOwnedGameSession(userId, sessionId);
        final GameCareer gameCareer = getRequiredGameCareer(sessionId);
        final GameStat gameStat = getRequiredGameStat(sessionId);

        try {
            final JobTransferServiceResponse response = jobTransferService.transfer(
                JobTransferServiceRequest.of(
                    gameCareer,
                    gameStat,
                    requireRecentNetworkingCount(gameCareer),
                    request.getOfferId(),
                    requirePlayableTurn(gameSession)
                )
            );
            gameCareerRepository.saveAndFlush(gameCareer);
            syncSessionJobType(sessionId, gameCareer.getJobType());
            return JobTransferResponse.from(response);
        } catch (HomerunException exception) {
            throw mapCharacterException(exception);
        }
    }

    private GameSession getOwnedGameSession(final Long userId, final Long sessionId) {
        final GameSession gameSession = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.GAME_SESSION_NOT_FOUND));
        gameSession.assertOwner(userId);
        gameSession.assertInProgress();
        return gameSession;
    }

    private GameCareer getRequiredGameCareer(final Long sessionId) {
        return gameCareerRepository.findById(sessionId.intValue())
            .orElseThrow(() -> new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID));
    }

    private GameStat getRequiredGameStat(final Long sessionId) {
        return gameStatRepository.findById(sessionId.intValue())
            .orElseThrow(() -> new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID));
    }

    private Integer requirePlayableTurn(final GameSession gameSession) {
        final Integer currentTurn = gameSession.getCurrentTurn();
        if (currentTurn == null || currentTurn < 1) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return currentTurn;
    }

    private CyclePhase requireCyclePhase(final GameSession gameSession) {
        final CyclePhase cyclePhase = gameSession.getCyclePhase();
        if (cyclePhase == null) {
            throw new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
        }
        return cyclePhase;
    }

    private int requireRecentNetworkingCount(final GameCareer gameCareer) {
        final Integer recentNetworkingCount = gameCareer.getRecentNetworkingCount();
        if (recentNetworkingCount == null || recentNetworkingCount < 0) {
            throw new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
        }
        return recentNetworkingCount;
    }

    private void syncSessionJobType(final Long sessionId, final JobType jobType) {
        entityManager.createQuery(
                "update GameSession gameSession "
                    + "set gameSession.jobType = :jobType "
                    + "where gameSession.gameSessionId = :sessionId"
            )
            .setParameter("jobType", jobType)
            .setParameter("sessionId", sessionId)
            .executeUpdate();
    }

    private HomerunException mapCharacterException(final HomerunException exception) {
        final ErrorCode errorCode = exception.getErrorCode();
        if (!errorCode.name().startsWith("CHARACTER_")) {
            return exception;
        }
        if (isClientVisibleCharacterError(errorCode)) {
            return new HomerunException(ErrorCode.INVALID_INPUT_VALUE, exception);
        }
        return new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID, exception);
    }

    private boolean isClientVisibleCharacterError(final ErrorCode errorCode) {
        return errorCode == ErrorCode.CHARACTER_REQUEST_INVALID
            || errorCode == ErrorCode.CHARACTER_TURN_INVALID
            || errorCode == ErrorCode.CHARACTER_STAT_INVALID
            || errorCode == ErrorCode.CHARACTER_JOB_TYPE_UNSUPPORTED;
    }
}
