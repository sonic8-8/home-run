package io.ssafy.p.j14c103.homerun.api.service.game.session;

import io.ssafy.p.j14c103.homerun.api.service.game.session.request.CreateGameSessionServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.session.response.CreateGameSessionResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.session.response.GameSessionDetailResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.session.response.GameSessionListResponse;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.GameStatRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareerRepository;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.api.service.world.TargetPropertyValidationService;
import io.ssafy.p.j14c103.homerun.api.service.world.response.TargetPropertyValidationResponse;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameSessionService {

    private final GameSessionRepository gameSessionRepository;
    private final UserAuthContextService userAuthContextService;
    private final TargetPropertyValidationService targetPropertyValidationService;
    private final GameSessionCleanupService gameSessionCleanupService;
    private final GameSessionInitialSnapshotService gameSessionInitialSnapshotService;
    private final GameStatRepository gameStatRepository;
    private final GameCareerRepository gameCareerRepository;

    @Transactional(readOnly = true)
    public GameSessionListResponse getSessions(final Long userId) {
        validateUser(userId);
        final List<GameSession> gameSessions =
            gameSessionRepository.findAllByUserIdOrderBySlotNumberAsc(userId);
        return GameSessionListResponse.from(gameSessions);
    }

    @Transactional
    public CreateGameSessionResponse create(
        final Long userId,
        final CreateGameSessionServiceRequest request
    ) {
        validateUser(userId);
        validateSlotConflict(userId, request.getSlotNumber());
        final DataSourceType dataSourceType = request.toDataSourceType();

        final TargetPropertyValidationResponse validationResponse =
            targetPropertyValidationService.validateTargetProperty(
                request.getRegionCode(),
                request.getDistrictCode(),
                request.getTargetPropertyId()
            );
        final GameSessionInitialSnapshot initialSnapshot = gameSessionInitialSnapshotService.read(
            userId,
            request.getCharacterType(),
            request.getJobType(),
            dataSourceType
        );

        final GameSession gameSession = GameSession.create(
            userId,
            request.getSlotNumber(),
            request.getCharacterName(),
            request.getCharacterType(),
            initialSnapshot.getJobType(),
            validationResponse.getHousingType(),
            request.getRegionCode(),
            request.getDistrictCode(),
            validationResponse.getPropertyId(),
            dataSourceType
        );

        final GameSession saved = gameSessionRepository.saveAndFlush(gameSession);
        saved.initializeCapital(
            initialSnapshot.getCashBalance(),
            initialSnapshot.getTotalAssets(),
            initialSnapshot.getNetWorth(),
            initialSnapshot.getCurrentDate(),
            initialSnapshot.getCycleState()
        );
        initializeCharacterState(saved, initialSnapshot);
        return CreateGameSessionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public GameSessionDetailResponse getSessionDetail(final Long userId, final Long sessionId) {
        validateUser(userId);
        final GameSession gameSession = getOwnedGameSession(userId, sessionId);
        return GameSessionDetailResponse.from(gameSession);
    }

    @Transactional
    public void delete(final Long userId, final Long sessionId) {
        validateUser(userId);
        final GameSession gameSession = getOwnedGameSessionForUpdate(userId, sessionId);
        gameSessionCleanupService.deleteAllByGameSessionId(sessionId);
        gameSessionRepository.delete(gameSession);
    }

    private void validateUser(final Long userId) {
        userAuthContextService.getContext(userId);
    }

    private GameSession getOwnedGameSession(final Long userId, final Long sessionId) {
        final GameSession gameSession = getRequiredGameSession(sessionId);
        gameSession.assertOwner(userId);
        return gameSession;
    }

    private GameSession getOwnedGameSessionForUpdate(final Long userId, final Long sessionId) {
        final GameSession gameSession = getRequiredGameSessionForUpdate(sessionId);
        gameSession.assertOwner(userId);
        return gameSession;
    }

    private GameSession getRequiredGameSession(final Long sessionId) {
        return gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.GAME_SESSION_NOT_FOUND));
    }

    private GameSession getRequiredGameSessionForUpdate(final Long sessionId) {
        return gameSessionRepository.findByIdForUpdate(sessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.GAME_SESSION_NOT_FOUND));
    }

    private void validateSlotConflict(final Long userId, final Integer slotNumber) {
        if (!gameSessionRepository.existsByUserIdAndSlotNumber(userId, slotNumber)) {
            return;
        }
        throw new HomerunException(ErrorCode.GAME_SLOT_CONFLICT);
    }

    private void initializeCharacterState(
        final GameSession gameSession,
        final GameSessionInitialSnapshot initialSnapshot
    ) {
        final Integer gameId = toGameId(gameSession.getGameSessionId());
        gameStatRepository.save(createGameStat(gameId, gameSession, initialSnapshot));
        gameCareerRepository.save(createGameCareer(gameId, initialSnapshot));
    }

    private GameStat createGameStat(
        final Integer gameId,
        final GameSession gameSession,
        final GameSessionInitialSnapshot initialSnapshot
    ) {
        return GameStat.create(
            gameId,
            initialSnapshot.getStatSeed().health(),
            initialSnapshot.getStatSeed().fatigue(),
            initialSnapshot.getStatSeed().stress(),
            initialSnapshot.getStatSeed().happiness(),
            initialSnapshot.getStatSeed().knowledge(),
            gameSession.getCurrentTurn()
        );
    }

    private GameCareer createGameCareer(
        final Integer gameId,
        final GameSessionInitialSnapshot initialSnapshot
    ) {
        return GameCareer.builder()
            .gameId(gameId)
            .jobType(initialSnapshot.getCareerSeed().jobType())
            .jobTitle(initialSnapshot.getCareerSeed().jobTitle())
            .salary(initialSnapshot.getCareerSeed().annualSalary())
            .tenureTurns(initialSnapshot.getCareerSeed().tenureTurns())
            .recentStudyCount(initialSnapshot.getCareerSeed().recentStudyCount())
            .recentNetworkingCount(initialSnapshot.getCareerSeed().recentNetworkingCount())
            .negotiationPreparationScore(
                initialSnapshot.getCareerSeed().negotiationPreparationScore()
            )
            .lastNegotiatedTurn(initialSnapshot.getCareerSeed().lastNegotiatedTurn())
            .employmentStatus(initialSnapshot.getCareerSeed().employmentStatus())
            .probationEndTurn(initialSnapshot.getCareerSeed().probationEndTurn())
            .rehireAvailableTurn(initialSnapshot.getCareerSeed().rehireAvailableTurn())
            .remainingUnemploymentBenefitTurns(
                initialSnapshot.getCareerSeed().remainingUnemploymentBenefitTurns()
            )
            .salaryBeforeResignation(initialSnapshot.getCareerSeed().salaryBeforeResignation())
            .build();
    }

    private Integer toGameId(final Long sessionId) {
        try {
            return Math.toIntExact(sessionId);
        } catch (ArithmeticException exception) {
            throw new HomerunException(ErrorCode.CHARACTER_GAME_ID_INVALID, exception);
        }
    }
}
