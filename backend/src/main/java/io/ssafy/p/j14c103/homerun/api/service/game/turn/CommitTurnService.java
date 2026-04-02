package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.request.SettlementOrchestratorRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.response.CommitTurnResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.response.SettlementOrchestratorResult;
import io.ssafy.p.j14c103.homerun.api.service.world.GameWorldRollService;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.GameStatRepository;
import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareerRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.SessionStatus;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousing;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousingRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.GameLoan;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.GameLoanRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameReport;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameReportRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameTimeline;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameTimelineRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.settlement.SettlementLog;
import io.ssafy.p.j14c103.homerun.domain.gamesession.settlement.SettlementLogRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.GameStockMarketState;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.GameStockMarketStateRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockHolding;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockHoldingRepository;
import io.ssafy.p.j14c103.homerun.api.service.world.GameWorldResultService;
import io.ssafy.p.j14c103.homerun.api.service.world.WorldPendingEventQueueService;
import io.ssafy.p.j14c103.homerun.api.service.world.result.GameWorldResult;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.ActionCatalog;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.GameSessionTurnSlotRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.GameTurnSlot;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.TurnDraft;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.TurnDraftRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.TurnDraftSlot;
import io.ssafy.p.j14c103.homerun.domain.history.news.GameNewsLog;
import io.ssafy.p.j14c103.homerun.domain.history.news.GameNewsLogRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleState;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateProperty;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommitTurnService {

    private static final String TURN_COMMIT_DURATION = "homerun.turn.commit.duration";
    private static final String BOUNDARY_TAG = "boundary";
    private static final String RESULT_TAG = "result";
    private static final String COMMIT_TURN_BOUNDARY = "commit-turn";
    private static final String CLEAR_ENDING_TITLE = "부동산 갑부";
    private static final String BANKRUPT_ENDING_TITLE = "파산";
    private static final String TIMEOUT_ENDING_TITLE = "시간 초과";
    private static final String FORECLOSURE_ENDING_TITLE = "압류";
    private static final String DEFAULT_SPENDING_CATEGORY = "미집계";
    private final GameTurnCommitGuardService gameTurnCommitGuardService;
    private final GameWorldResultService gameWorldResultService;
    private final GameWorldRollService gameWorldRollService;
    private final WorldPendingEventQueueService worldPendingEventQueueService;
    private final SettlementOrchestratorService settlementOrchestratorService;
    private final GameSessionRepository gameSessionRepository;
    private final GameSessionTurnSlotRepository gameSessionTurnSlotRepository;
    private final SettlementLogRepository settlementLogRepository;
    private final GameTimelineRepository gameTimelineRepository;
    private final GameReportRepository gameReportRepository;
    private final TurnDraftRepository turnDraftRepository;
    private final GameNewsLogRepository gameNewsLogRepository;
    private final ActionCatalog actionCatalog;
    private final GameHousingRepository gameHousingRepository;
    private final RealEstatePropertyRepository realEstatePropertyRepository;
    private final StockHoldingRepository stockHoldingRepository;
    private final GameStockMarketStateRepository gameStockMarketStateRepository;
    private final GameLoanRepository gameLoanRepository;
    private final GameStatRepository gameStatRepository;
    private final MeterRegistry meterRegistry;

    @Transactional
    public CommitTurnResponse commitTurn(final Long userId, final Long sessionId) {
        final Timer.Sample sample = Timer.start(meterRegistry);
        try {
            final GameTurnCommitGuardService.CommitTurnGuardResult guardResult =
                gameTurnCommitGuardService.guard(userId, sessionId);
            final GameSession gameSession = guardResult.getGameSession();
            final TurnDraft turnDraft = guardResult.getTurnDraft();
            final Integer committedTurn = gameSession.getCurrentTurn();
            final int worldRoll = gameWorldRollService.resolveTurnRoll(sessionId, committedTurn + 1);
            final GameWorldResult worldResult = gameWorldResultService.buildWorldResult(
                sessionId,
                worldRoll
            );
            final SettlementOrchestratorResult settlementResult =
                settlementOrchestratorService.orchestrate(
                    buildSettlementRequest(gameSession, committedTurn + 1, turnDraft, worldResult)
                );

            saveCommittedSlots(gameSession.getGameSessionId(), committedTurn, turnDraft);
            applyCharacterState(gameSession.getGameSessionId(), committedTurn + 1, settlementResult);
            final SessionAdvanceResult sessionAdvanceResult =
                advanceSession(gameSession, worldResult, settlementResult);
            saveSettlementLogs(gameSession.getGameSessionId(), committedTurn, settlementResult);
            saveNewsLog(
                gameSession.getGameSessionId(),
                committedTurn + 1,
                sessionAdvanceResult.nextDate(),
                worldResult
            );
            worldPendingEventQueueService.enqueuePendingEvents(
                gameSession.getGameSessionId(),
                worldResult.getEventCandidates()
            );
            saveTimeline(
                gameSession.getGameSessionId(),
                committedTurn,
                sessionAdvanceResult.nextDate(),
                settlementResult
            );
            saveGameReportIfEnded(gameSession, settlementResult);
            turnDraftRepository.deleteBySessionId(sessionId);

            final CommitTurnResponse response = CommitTurnResponse.of(
                committedTurn,
                buildSettlementLog(settlementResult),
                CommitTurnResponse.UpdatedAssetsResponse.of(
                    toLong(settlementResult.getFinalCash()),
                    toLong(settlementResult.getFinalLoanBalance()),
                    toLong(resolveCurrentRealEstateAssetValue(sessionId)),
                    toLong(settlementResult.getNetWorth())
                ),
                CommitTurnResponse.StatChangesResponse.from(settlementResult.getAggregatedStatChanges()),
                CommitTurnResponse.FlagsResponse.of(
                    settlementResult.getEndingStatus() == SessionStatus.BANKRUPT,
                    settlementResult.getEndingStatus() == SessionStatus.CLEAR,
                    false,
                    false,
                    settlementResult.isHasEvent()
                )
            );
            sample.stop(turnCommitTimer("success"));
            return response;
        } catch (RuntimeException exception) {
            sample.stop(turnCommitTimer("failure"));
            throw exception;
        }
    }

    private Timer turnCommitTimer(final String result) {
        return Timer.builder(TURN_COMMIT_DURATION)
            .tag(BOUNDARY_TAG, COMMIT_TURN_BOUNDARY)
            .tag(RESULT_TAG, result)
            .register(meterRegistry);
    }

    private void saveCommittedSlots(
        final Long sessionId,
        final Integer committedTurn,
        final TurnDraft turnDraft
    ) {
        final List<GameTurnSlot> committedSlots = turnDraft.getSlots().stream()
            .sorted(Comparator.comparing(TurnDraftSlot::getSlotIndex))
            .map(slot -> GameTurnSlot.create(
                sessionId,
                committedTurn,
                slot.getSlotIndex(),
                slot.getActionType(),
                actionCatalog.getDefinition(slot.getActionType()).category(),
                slot.isForcedAction()
            ))
            .toList();
        gameSessionTurnSlotRepository.saveAllAndFlush(committedSlots);
    }

    private SettlementOrchestratorRequest buildSettlementRequest(
        final GameSession gameSession,
        final Integer nextTurnNumber,
        final TurnDraft turnDraft,
        final GameWorldResult worldResult
    ) {
        return SettlementOrchestratorRequest.of(
            gameSession.getGameSessionId(),
            gameSession.getUserId(),
            nextTurnNumber,
            gameSession.getCashBalance(),
            resolveCurrentStockValue(gameSession.getGameSessionId()),
            resolveCurrentRealEstateAssetValue(gameSession.getGameSessionId()),
            resolveCurrentLoanBalance(gameSession.getGameSessionId()),
            turnDraft.getPreviewCashChange(),
            turnDraft.getPreviewStatChanges(),
            worldResult.getCycleResult().getDescription(),
            !worldResult.getEventCandidates().isEmpty(),
            isTargetPropertyOwned(gameSession, worldResult)
        );
    }

    private void applyCharacterState(
        final Long sessionId,
        final int nextTurnNumber,
        final SettlementOrchestratorResult settlementResult
    ) {
        gameStatRepository.findById(toGameId(sessionId)).ifPresent(gameStat ->
            gameStat.applyChange(
                settlementResult.getAggregatedStatChanges().getOrDefault("health", 0),
                settlementResult.getAggregatedStatChanges().getOrDefault("fatigue", 0),
                settlementResult.getAggregatedStatChanges().getOrDefault("stress", 0),
                settlementResult.getAggregatedStatChanges().getOrDefault("happiness", 0),
                settlementResult.getAggregatedStatChanges().getOrDefault("knowledge", 0),
                nextTurnNumber
            )
        );
    }

    private SessionAdvanceResult advanceSession(
        final GameSession gameSession,
        final GameWorldResult worldResult,
        final SettlementOrchestratorResult settlementResult
    ) {
        final Money nextCash = settlementResult.getFinalCash();
        final Money nextTotalAssets = settlementResult.getTotalAssets();
        final Money nextNetWorth = settlementResult.getNetWorth();
        final LocalDate nextDate = requireCurrentDate(gameSession).plusMonths(1);
        final CycleState nextCycleState = CycleState.of(
            worldResult.getCycleResult().getNextPhase(),
            worldResult.getCycleResult().getNextType(),
            worldResult.getCycleResult().getRemainingTurns()
        );

        gameSession.advanceTurn(
            gameSession.getCurrentTurn() + 1,
            nextDate,
            nextCash,
            nextTotalAssets,
            nextNetWorth,
            nextCycleState
        );
        if (settlementResult.getEndingStatus() != SessionStatus.IN_PROGRESS) {
            gameSession.markEnding(settlementResult.getEndingStatus());
        }
        return new SessionAdvanceResult(nextDate);
    }

    private LocalDate requireCurrentDate(final GameSession gameSession) {
        if (gameSession.getCurrentDate() == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
        return gameSession.getCurrentDate();
    }

    private void saveSettlementLogs(
        final Long sessionId,
        final Integer committedTurn,
        final SettlementOrchestratorResult settlementResult
    ) {
        final List<SettlementLog> settlementLogs = settlementResult.getStepResults().stream()
            .map(stepResult -> SettlementLog.create(
                sessionId,
                committedTurn,
                stepResult.getPhaseType(),
                stepResult.getDescription(),
                toInteger(stepResult.getCashDelta()),
                stepResult.getStatChanges()
            ))
            .toList();
        settlementLogRepository.saveAllAndFlush(settlementLogs);
    }

    private void saveNewsLog(
        final Long sessionId,
        final Integer turnNumber,
        final LocalDate publishedDate,
        final GameWorldResult worldResult
    ) {
        if (worldResult.getNewsCandidates().isEmpty()) {
            return;
        }

        final GameWorldResult.NewsCandidate selectedNews = worldResult.getNewsCandidates().get(0);
        gameNewsLogRepository.saveAndFlush(GameNewsLog.create(
            sessionId,
            turnNumber,
            selectedNews.getNewsId(),
            selectedNews.getHeadline(),
            publishedDate
        ));
    }

    private void saveTimeline(
        final Long sessionId,
        final Integer committedTurn,
        final LocalDate nextDate,
        final SettlementOrchestratorResult settlementResult
    ) {
        gameTimelineRepository.saveAndFlush(GameTimeline.create(
            sessionId,
            committedTurn,
            nextDate,
            toInteger(settlementResult.getFinalCash()),
            toInteger(settlementResult.getNetWorth()),
            toInteger(settlementResult.getTotalAssets()),
            extractCurrentStockValueAmount(sessionId),
            toInteger(settlementResult.getFinalLoanBalance()),
            extractSalaryAmount(settlementResult)
        ));
    }

    private void saveGameReportIfEnded(
        final GameSession gameSession,
        final SettlementOrchestratorResult settlementResult
    ) {
        if (settlementResult.getEndingStatus() == SessionStatus.IN_PROGRESS) {
            return;
        }

        final List<SettlementLog> settlementLogs =
            settlementLogRepository.findAllByGameSessionIdOrderByTurnNumberAscSettlementLogIdAsc(
                gameSession.getGameSessionId()
            );
        final Integer totalIncome = calculateTotalIncome(settlementLogs);
        final Integer totalExpense = calculateTotalExpense(settlementLogs);

        gameReportRepository.saveAndFlush(GameReport.create(
            gameSession.getGameSessionId(),
            settlementResult.getEndingStatus(),
            resolveEndingTitle(settlementResult.getEndingStatus()),
            totalIncome,
            totalExpense,
            resolveGrade(settlementResult.getEndingStatus()),
            toInteger(settlementResult.getTotalAssets()),
            totalIncome - totalExpense,
            DEFAULT_SPENDING_CATEGORY,
            BigDecimal.ZERO,
            List.of()
        ));
    }

    private List<CommitTurnResponse.SettlementLogItemResponse> buildSettlementLog(
        final SettlementOrchestratorResult settlementResult
    ) {
        return settlementResult.getStepResults().stream()
            .map(stepResult -> CommitTurnResponse.SettlementLogItemResponse.of(
                stepResult.getPhaseType().name(),
                stepResult.getDescription(),
                toLong(stepResult.getCashDelta()),
                CommitTurnResponse.StatChangesResponse.from(stepResult.getStatChanges())
            ))
            .toList();
    }

    private Money resolveCurrentLoanBalance(final Long sessionId) {
        return Money.of(gameLoanRepository.findAllByGameSessionId(sessionId).stream()
            .filter(GameLoan::isActive)
            .mapToLong(loan -> loan.getPrincipalAmount() == null ? 0L : loan.getPrincipalAmount())
            .sum());
    }

    private boolean isTargetPropertyOwned(
        final GameSession gameSession,
        final GameWorldResult worldResult
    ) {
        final Long currentPropertyId = worldResult.getHousingSnapshot().getCurrentPropertyId();
        if (currentPropertyId == null) {
            return false;
        }
        final Long targetPropertyId = worldResult.getHousingSnapshot().getTargetPropertyId();
        if (targetPropertyId != null) {
            return currentPropertyId.equals(targetPropertyId);
        }
        return currentPropertyId.equals(gameSession.getTargetPropertyId());
    }

    private Integer extractCurrentStockValueAmount(final Long sessionId) {
        return toInteger(resolveCurrentStockValue(sessionId));
    }

    private Money resolveCurrentStockValue(final Long sessionId) {
        final Map<String, Integer> currentPriceByStockCode = gameStockMarketStateRepository
            .findAllByGameSessionId(sessionId)
            .stream()
            .collect(Collectors.toMap(
                GameStockMarketState::getStockCode,
                GameStockMarketState::getCurrentPriceAmount
            ));
        final long currentStockValue = stockHoldingRepository.findAllByGameSessionId(sessionId)
            .stream()
            .mapToLong(holding -> resolveCurrentStockValue(holding, currentPriceByStockCode))
            .sum();
        return Money.of(currentStockValue);
    }

    private long resolveCurrentStockValue(
        final StockHolding holding,
        final Map<String, Integer> currentPriceByStockCode
    ) {
        final Integer quantity = holding.getQuantity();
        if (quantity == null || quantity <= 0) {
            return 0L;
        }
        final Integer currentPrice = currentPriceByStockCode.getOrDefault(holding.getStockCode(), 0);
        return (long) currentPrice * quantity;
    }

    private Money resolveCurrentRealEstateAssetValue(final Long sessionId) {
        return gameHousingRepository.findByGameSessionId(sessionId)
            .map(this::resolveCurrentRealEstateAssetValue)
            .orElse(Money.zero());
    }

    private Money resolveCurrentRealEstateAssetValue(final GameHousing gameHousing) {
        final HousingType currentHousingType = gameHousing.getCurrentHousingType();
        if (currentHousingType == null || currentHousingType == HousingType.NONE) {
            return Money.zero();
        }
        if (currentHousingType == HousingType.OWNED_APT) {
            return resolveOwnedHousingAssetValue(gameHousing);
        }
        return requireCurrentDeposit(gameHousing);
    }

    private Money resolveOwnedHousingAssetValue(final GameHousing gameHousing) {
        final Long currentPropertyId = gameHousing.getCurrentPropertyId();
        if (currentPropertyId == null) {
            throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
        }
        return realEstatePropertyRepository.findById(currentPropertyId)
            .map(RealEstateProperty::getBasePrice)
            .orElseThrow(() -> new HomerunException(ErrorCode.WORLD_RESULT_INVALID));
    }

    private Money requireCurrentDeposit(final GameHousing gameHousing) {
        final Money currentDeposit = gameHousing.getCurrentDeposit();
        if (currentDeposit != null) {
            return currentDeposit;
        }
        throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
    }

    private Integer extractSalaryAmount(final SettlementOrchestratorResult settlementResult) {
        return settlementResult.getStepResults().stream()
            .filter(stepResult -> stepResult.getStepType() == SettlementStepType.INCOME_SALARY_SETTLEMENT)
            .findFirst()
            .map(SettlementOrchestratorResult.StepResult::getCashDelta)
            .map(this::toInteger)
            .orElse(0);
    }

    private Integer toGameId(final Long sessionId) {
        try {
            return Math.toIntExact(sessionId);
        } catch (ArithmeticException exception) {
            throw new HomerunException(ErrorCode.CHARACTER_GAME_ID_INVALID, exception);
        }
    }

    private Integer calculateTotalIncome(final List<SettlementLog> settlementLogs) {
        return settlementLogs.stream()
            .map(SettlementLog::getCashChangeAmount)
            .filter(cashChangeAmount -> cashChangeAmount != null && cashChangeAmount > 0)
            .reduce(0, Integer::sum);
    }

    private Integer calculateTotalExpense(final List<SettlementLog> settlementLogs) {
        return settlementLogs.stream()
            .map(SettlementLog::getCashChangeAmount)
            .filter(cashChangeAmount -> cashChangeAmount != null && cashChangeAmount < 0)
            .map(Math::abs)
            .reduce(0, Integer::sum);
    }

    private String resolveEndingTitle(final SessionStatus endingStatus) {
        return switch (endingStatus) {
            case CLEAR -> CLEAR_ENDING_TITLE;
            case BANKRUPT -> BANKRUPT_ENDING_TITLE;
            case TIMEOUT -> TIMEOUT_ENDING_TITLE;
            case FORECLOSURE -> FORECLOSURE_ENDING_TITLE;
            case IN_PROGRESS -> throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        };
    }

    private String resolveGrade(final SessionStatus endingStatus) {
        return switch (endingStatus) {
            case CLEAR -> "S";
            case TIMEOUT -> "A";
            case BANKRUPT, FORECLOSURE -> "B";
            case IN_PROGRESS -> throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        };
    }

    private Long toLong(final Money money) {
        return money.getAmount().longValueExact();
    }

    private Integer toInteger(final Money money) {
        return money.getAmount().intValueExact();
    }

    private record SessionAdvanceResult(
        LocalDate nextDate
    ) {
    }
}
