package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.ssafy.p.j14c103.homerun.api.service.game.session.GameSessionCleanupService;
import io.ssafy.p.j14c103.homerun.api.service.game.session.GameSessionService;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.response.CommitTurnResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.GameWorldRollService;
import io.ssafy.p.j14c103.homerun.api.service.world.GameWorldResultService;
import io.ssafy.p.j14c103.homerun.api.service.world.result.GameWorldResult;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareerRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.SessionStatus;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousing;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousingRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.GameLoan;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.GameLoanRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.RepaymentType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameReport;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameReportRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameTimeline;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameTimelineRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.settlement.SettlementLog;
import io.ssafy.p.j14c103.homerun.domain.gamesession.settlement.SettlementLogRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.settlement.SettlementPhaseType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.GameStockMarketState;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.GameStockMarketStateRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockOrder;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockOrderRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockHolding;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockHoldingRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.ActionCategory;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.ActionType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.GameSessionTurnSlotRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.GameTurnSlot;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.TurnDraft;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.TurnDraftRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.TurnDraftSlot;
import io.ssafy.p.j14c103.homerun.domain.history.news.GameNewsLog;
import io.ssafy.p.j14c103.homerun.domain.history.news.GameNewsLogRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.spending.SpendingCategory;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetCardSpend;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetCardSpendRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetProfile;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetProfileRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleState;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleType;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventPresentationType;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventTriggerType;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEvent;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEventRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.GamePendingEvent;
import io.ssafy.p.j14c103.homerun.domain.world.event.GamePendingEventRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateProperty;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class CommitTurnServiceTest extends IntegrationTestSupport {

    private static final String TURN_COMMIT_DURATION = "homerun.turn.commit.duration";
    private static final String COMMIT_TURN_BOUNDARY = "commit-turn";

    @Autowired
    private CommitTurnService commitTurnService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private GameSessionTurnSlotRepository gameTurnSlotRepository;

    @Autowired
    private SettlementLogRepository settlementLogRepository;

    @Autowired
    private GameTimelineRepository gameTimelineRepository;

    @Autowired
    private GameReportRepository gameReportRepository;

    @Autowired
    private GameHousingRepository gameHousingRepository;

    @Autowired
    private GameLoanRepository gameLoanRepository;

    @Autowired
    private RealEstatePropertyRepository realEstatePropertyRepository;

    @Autowired
    private StockHoldingRepository stockHoldingRepository;

    @Autowired
    private StockOrderRepository stockOrderRepository;

    @Autowired
    private GameStockMarketStateRepository gameStockMarketStateRepository;

    @Autowired
    private GameCareerRepository gameCareerRepository;

    @Autowired
    private GameNewsLogRepository gameNewsLogRepository;

    @Autowired
    private GamePendingEventRepository gamePendingEventRepository;

    @Autowired
    private GameEventRepository gameEventRepository;

    @Autowired
    private UserAssetProfileRepository userAssetProfileRepository;

    @Autowired
    private UserAssetCardSpendRepository userAssetCardSpendRepository;

    @Autowired
    private GameSessionService gameSessionService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MeterRegistry meterRegistry;

    @MockitoBean
    private TurnDraftRepository turnDraftRepository;

    @MockitoBean
    private GameWorldResultService gameWorldResultService;

    @MockitoBean
    private GameWorldRollService gameWorldRollService;

    @MockitoBean
    private GameSessionCleanupService gameSessionCleanupService;

    @BeforeEach
    void setUp() {
        given(gameWorldRollService.resolveTurnRoll(anyLong(), anyInt())).willReturn(50);
    }

    @AfterEach
    void tearDown() {
        stockOrderRepository.deleteAllInBatch();
        stockHoldingRepository.deleteAllInBatch();
        gameStockMarketStateRepository.deleteAllInBatch();
        gamePendingEventRepository.deleteAllInBatch();
        gameNewsLogRepository.deleteAllInBatch();
        gameLoanRepository.deleteAllInBatch();
        gameHousingRepository.deleteAllInBatch();
        gameCareerRepository.deleteAllInBatch();
        userAssetCardSpendRepository.deleteAllInBatch();
        userAssetProfileRepository.deleteAllInBatch();
        realEstatePropertyRepository.deleteAllInBatch();
        gameEventRepository.deleteAllInBatch();
        settlementLogRepository.deleteAllInBatch();
        gameTimelineRepository.deleteAllInBatch();
        gameReportRepository.deleteAllInBatch();
        gameTurnSlotRepository.deleteAllInBatch();
        gameSessionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("턴 커밋은 draft의 forcedAction을 저장 슬롯에 그대로 반영한다.")
    @Test
    void commitTurnPreservesForcedActionFromDraft() {
        // given
        final User user = saveUser("turn-commit-forced@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 5)
        );
        saveGameCareer(gameSession.getGameSessionId(), 26_400_000, EmploymentStatus.EMPLOYED);
        final TurnDraft turnDraft = createTurnDraft(
            gameSession.getGameSessionId(),
            5,
            List.of(
                TurnDraftSlot.of(0, ActionType.REST, true),
                TurnDraftSlot.of(1, ActionType.STUDY, false),
                TurnDraftSlot.of(2, ActionType.REST, true)
            ),
            Money.of(300_000L),
            Map.of(
                "health", 3,
                "fatigue", -14,
                "stress", -8,
                "happiness", 4,
                "knowledge", 0
            )
        );
        given(turnDraftRepository.findBySessionId(gameSession.getGameSessionId()))
            .willReturn(Optional.of(turnDraft));
        given(gameWorldResultService.buildWorldResult(gameSession.getGameSessionId(), 50))
            .willReturn(GameWorldResult.of(
                GameWorldResult.CycleResult.of(
                    CyclePhase.RECOVERY,
                    CycleType.CYCLE_RATE_HIKE,
                    18,
                    "경기 회복기"
                ),
                List.of(),
                List.of(),
                GameWorldResult.HousingSnapshot.empty()
            ));

        // when
        commitTurnService.commitTurn(user.getId(), gameSession.getGameSessionId());

        // then
        assertThat(gameTurnSlotRepository.findAllByGameSessionIdAndTurnNumberOrderBySlotIndex(
                gameSession.getGameSessionId(),
                5
            ))
            .extracting(
                GameTurnSlot::getSlotIndex,
                GameTurnSlot::getActionType,
                GameTurnSlot::isForcedAction
            )
            .containsExactly(
                tuple(0, ActionType.REST, true),
                tuple(1, ActionType.STUDY, false),
                tuple(2, ActionType.REST, true)
            );
    }

    @DisplayName("턴 커밋은 정산 결과를 settlement log와 timeline에 저장하고 세션을 다음 턴으로 전진시킨다.")
    @Test
    void commitTurn() {
        // given
        final long successTimerCountBefore = turnCommitTimerCount("success");
        final User user = saveUser("turn-commit@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 5)
        );
        saveGameCareer(gameSession.getGameSessionId(), 26_400_000, EmploymentStatus.EMPLOYED);
        final TurnDraft turnDraft = createTurnDraft(gameSession.getGameSessionId(), 5);
        given(turnDraftRepository.findBySessionId(gameSession.getGameSessionId()))
            .willReturn(Optional.of(turnDraft));
        given(gameWorldResultService.buildWorldResult(gameSession.getGameSessionId(), 50))
            .willReturn(GameWorldResult.of(
                GameWorldResult.CycleResult.of(
                    CyclePhase.RECOVERY,
                    CycleType.CYCLE_RATE_HIKE,
                    18,
                    "경기 회복기"
                ),
                List.of(),
                List.of(),
                GameWorldResult.HousingSnapshot.empty()
            ));

        // when
        final CommitTurnResponse response = commitTurnService.commitTurn(
            user.getId(),
            gameSession.getGameSessionId()
        );

        // then
        assertThat(response.getTurnNumber()).isEqualTo(5);
        assertThat(response.getSettlementLog()).hasSize(13);
        assertThat(response.getSettlementLog().get(0).getPhase()).isEqualTo("MARKET_UPDATE");
        assertThat(response.getSettlementLog().get(0).getDescription()).isEqualTo("경기 회복기");
        assertThat(response.getSettlementLog())
            .extracting(CommitTurnResponse.SettlementLogItemResponse::getCashChange)
            .contains(2_200_000L, 300_000L);
        assertThat(response.getUpdatedAssets().getCash()).isEqualTo(4_500_000L);
        assertThat(response.getUpdatedAssets().getLoan()).isEqualTo(0L);
        assertThat(response.getUpdatedAssets().getRealEstateValue()).isEqualTo(0L);
        assertThat(response.getUpdatedAssets().getNetAssets()).isEqualTo(4_500_000L);
        assertThat(response.getStatChanges().getHealth()).isEqualTo(3);
        assertThat(response.getStatChanges().getFatigue()).isEqualTo(-14);
        assertThat(response.getStatChanges().getKnowledge()).isEqualTo(8);
        assertThat(response.getFlags().isHasEvent()).isFalse();

        assertThat(gameTurnSlotRepository.findAllByGameSessionIdAndTurnNumberOrderBySlotIndex(
                gameSession.getGameSessionId(),
                5
            ))
            .extracting(
                GameTurnSlot::getSlotIndex,
                GameTurnSlot::getActionType,
                GameTurnSlot::getActionCategory,
                GameTurnSlot::isForcedAction
            )
            .containsExactly(
                tuple(0, ActionType.STUDY, ActionCategory.ACTIVITY, false),
                tuple(1, ActionType.HOBBY, ActionCategory.SHOPPING, false),
                tuple(2, ActionType.SIDE_JOB, ActionCategory.ACTIVITY, false)
            );

        final GameSession updated = gameSessionRepository.findById(gameSession.getGameSessionId())
            .orElseThrow();
        assertThat(updated.getCurrentTurn()).isEqualTo(6);
        assertThat(updated.getCurrentDate()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(updated.getCyclePhase()).isEqualTo(CyclePhase.RECOVERY);
        assertThat(updated.getCycleType()).isEqualTo(CycleType.CYCLE_RATE_HIKE);
        assertThat(updated.getCycleRemainingTurns()).isEqualTo(18);
        assertThat(updated.getCashBalance()).isEqualTo(Money.of(4_500_000L));
        assertThat(updated.getTotalAssets()).isEqualTo(Money.of(4_500_000L));
        assertThat(updated.getNetWorth()).isEqualTo(Money.of(4_500_000L));
        assertThat(updated.getSessionStatus()).isEqualTo(SessionStatus.IN_PROGRESS);
        assertThat(gameReportRepository.existsById(gameSession.getGameSessionId())).isFalse();

        final List<SettlementLog> settlementLogs = settlementLogRepository
            .findAllByGameSessionIdAndTurnNumberOrderBySettlementLogIdAsc(gameSession.getGameSessionId(), 5);
        assertThat(settlementLogs).hasSize(13);
        assertThat(settlementLogs.get(0).getSettlementPhaseType()).isEqualTo(SettlementPhaseType.MARKET_UPDATE);
        assertThat(settlementLogs.get(0).getDescription()).isEqualTo("경기 회복기");
        assertThat(settlementLogs)
            .filteredOn(log -> log.getCashChangeAmount() != null && log.getCashChangeAmount() != 0)
            .extracting(SettlementLog::getCashChangeAmount)
            .containsExactly(2_200_000, 300_000);
        assertThat(settlementLogs)
            .filteredOn(log -> !log.getStatChanges().isEmpty())
            .singleElement()
            .extracting(log -> log.getStatChanges().get("knowledge"))
            .isEqualTo(8);

        final List<GameTimeline> timelines = gameTimelineRepository.findAllByGameSessionIdOrderByTurnNumberAsc(
            gameSession.getGameSessionId()
        );
        assertThat(timelines).hasSize(1);
        assertThat(timelines.get(0).getTurnNumber()).isEqualTo(5);
        assertThat(timelines.get(0).getLoggedDate()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(timelines.get(0).getCash()).isEqualTo(4_500_000);
        assertThat(timelines.get(0).getStockValueAmount()).isEqualTo(0);
        assertThat(timelines.get(0).getLoanBalanceAmount()).isEqualTo(0);
        assertThat(timelines.get(0).getSalaryAmount()).isEqualTo(2_200_000);

        assertTurnCommitTimerRecorded("success", successTimerCountBefore);
        then(gameWorldRollService).should().resolveTurnRoll(gameSession.getGameSessionId(), 6);
        then(gameWorldResultService).should().buildWorldResult(gameSession.getGameSessionId(), 50);
        then(turnDraftRepository).should().deleteBySessionId(gameSession.getGameSessionId());
    }

    @DisplayName("턴 커밋은 world result에서 생성된 뉴스와 이벤트를 다음 턴 read-back source로 저장한다.")
    @Test
    void commitTurnMaterializesNewsLogAndPendingEvents() {
        // given
        final User user = saveUser("turn-commit-world-result@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 5)
        );
        saveGameCareer(gameSession.getGameSessionId(), 26_400_000, EmploymentStatus.EMPLOYED);
        final TurnDraft turnDraft = createTurnDraft(gameSession.getGameSessionId(), 5);
        final GameEvent gameEvent = gameEventRepository.saveAndFlush(createPhoneEvent("EVT-COMMIT-001"));
        given(turnDraftRepository.findBySessionId(gameSession.getGameSessionId()))
            .willReturn(Optional.of(turnDraft));
        given(gameWorldResultService.buildWorldResult(gameSession.getGameSessionId(), 50))
            .willReturn(GameWorldResult.of(
                GameWorldResult.CycleResult.of(
                    CyclePhase.RECOVERY,
                    CycleType.CYCLE_RATE_HIKE,
                    18,
                    "월드 결과 저장"
                ),
                List.of(GameWorldResult.NewsCandidate.of("NEWS-COMMIT-001", "커밋 기준 뉴스")),
                List.of(GameWorldResult.EventCandidate.of(
                    gameEvent.getGameEventId(),
                    gameEvent.getEventCode(),
                    gameEvent.getEventName(),
                    gameEvent.getEventPresentationType()
                )),
                GameWorldResult.HousingSnapshot.empty()
            ));

        // when
        final CommitTurnResponse response = commitTurnService.commitTurn(
            user.getId(),
            gameSession.getGameSessionId()
        );

        // then
        assertThat(response.getFlags().isHasEvent()).isTrue();
        assertThat(gameNewsLogRepository
            .findAllByGameSessionIdAndTurnNumberOrderByGameNewsLogIdAsc(
                gameSession.getGameSessionId(),
                6
            ))
            .singleElement()
            .extracting(
                GameNewsLog::getNewsId,
                GameNewsLog::getHeadlineSnapshot,
                GameNewsLog::getPublishedDate
            )
            .containsExactly(
                "NEWS-COMMIT-001",
                "커밋 기준 뉴스",
                LocalDate.of(2026, 2, 1)
            );
        assertThat(gamePendingEventRepository
            .findAllByGameSessionIdAndResolvedYnFalseOrderByCreatedAtAscGamePendingEventIdAsc(
                gameSession.getGameSessionId()
            ))
            .singleElement()
            .extracting(
                GamePendingEvent::getTurnNumber,
                GamePendingEvent::getGameEventId,
                GamePendingEvent::getEventPresentationType
            )
            .containsExactly(
                6,
                gameEvent.getGameEventId(),
                EventPresentationType.PHONE
            );
    }

    @DisplayName("턴 커밋 결과가 종료 상태면 세션을 종료하고 game report를 생성한다.")
    @Test
    void commitTurnAndCloseSession() {
        // given
        final User user = saveUser("turn-timeout@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 359)
        );
        final TurnDraft turnDraft = createTurnDraft(
            gameSession.getGameSessionId(),
            359,
            Money.zero(),
            Map.of()
        );
        settlementLogRepository.saveAndFlush(SettlementLog.create(
            gameSession.getGameSessionId(),
            120,
            SettlementPhaseType.INCOME_EXPENSE,
            "월급을 반영한다",
            500_000,
            Map.of()
        ));
        settlementLogRepository.saveAndFlush(SettlementLog.create(
            gameSession.getGameSessionId(),
            120,
            SettlementPhaseType.INCOME_EXPENSE,
            "고정 지출을 차감한다",
            -200_000,
            Map.of()
        ));
        given(turnDraftRepository.findBySessionId(gameSession.getGameSessionId()))
            .willReturn(Optional.of(turnDraft));
        given(gameWorldResultService.buildWorldResult(gameSession.getGameSessionId(), 50))
            .willReturn(GameWorldResult.of(
                GameWorldResult.CycleResult.of(
                    CyclePhase.RECOVERY,
                    CycleType.CYCLE_RATE_HIKE,
                    18,
                    "장기 정체 구간"
                ),
                List.of(),
                List.of(),
                GameWorldResult.HousingSnapshot.empty()
            ));

        // when
        final CommitTurnResponse response = commitTurnService.commitTurn(
            user.getId(),
            gameSession.getGameSessionId()
        );

        // then
        assertThat(response.getTurnNumber()).isEqualTo(359);
        assertThat(response.getSettlementLog()).hasSize(13);
        assertThat(response.getFlags().isBankrupt()).isFalse();
        assertThat(response.getFlags().isCleared()).isFalse();

        final GameSession updated = gameSessionRepository.findById(gameSession.getGameSessionId())
            .orElseThrow();
        assertThat(updated.getCurrentTurn()).isEqualTo(360);
        assertThat(updated.getSessionStatus()).isEqualTo(SessionStatus.TIMEOUT);

        final GameReport gameReport = gameReportRepository.findById(gameSession.getGameSessionId())
            .orElseThrow();
        assertThat(gameReport.getEndingType()).isEqualTo(SessionStatus.TIMEOUT);
        assertThat(gameReport.getEndingTitle()).isEqualTo("시간 초과");
        assertThat(gameReport.getTotalAssetsAmount()).isEqualTo(2_000_000);
        assertThat(gameReport.getTotalIncomeAmount()).isEqualTo(500_000);
        assertThat(gameReport.getTotalExpenseAmount()).isEqualTo(200_000);
        assertThat(gameReport.getNetProfitAmount()).isEqualTo(300_000);

        assertThat(gameTimelineRepository.findAllByGameSessionIdOrderByTurnNumberAsc(gameSession.getGameSessionId()))
            .hasSize(1);
    }

    @DisplayName("턴 커밋 결과가 목표 주택 확보면 세션을 클리어하고 report 기본값을 채운다.")
    @Test
    void commitTurnAndClearSession() {
        // given
        final User user = saveUser("turn-clear@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 12)
        );
        final TurnDraft turnDraft = createTurnDraft(
            gameSession.getGameSessionId(),
            12,
            Money.zero(),
            Map.of()
        );
        given(turnDraftRepository.findBySessionId(gameSession.getGameSessionId()))
            .willReturn(Optional.of(turnDraft));
        given(gameWorldResultService.buildWorldResult(gameSession.getGameSessionId(), 50))
            .willReturn(GameWorldResult.of(
                GameWorldResult.CycleResult.of(
                    CyclePhase.RECOVERY,
                    CycleType.CYCLE_RATE_HIKE,
                    18,
                    "목표 주택 확보"
                ),
                List.of(),
                List.of(),
                GameWorldResult.HousingSnapshot.of(
                    HousingType.OWNED_APT,
                    gameSession.getTargetPropertyId(),
                    gameSession.getTargetPropertyId(),
                    false
                )
            ));

        // when
        final CommitTurnResponse response = commitTurnService.commitTurn(
            user.getId(),
            gameSession.getGameSessionId()
        );

        // then
        assertThat(response.getFlags().isCleared()).isTrue();
        assertThat(response.getFlags().isBankrupt()).isFalse();

        final GameSession updated = gameSessionRepository.findById(gameSession.getGameSessionId())
            .orElseThrow();
        assertThat(updated.getCurrentTurn()).isEqualTo(13);
        assertThat(updated.getSessionStatus()).isEqualTo(SessionStatus.CLEAR);

        final GameReport gameReport = gameReportRepository.findById(gameSession.getGameSessionId())
            .orElseThrow();
        assertThat(gameReport.getEndingType()).isEqualTo(SessionStatus.CLEAR);
        assertThat(gameReport.getEndingTitle()).isEqualTo("부동산 갑부");
        assertThat(gameReport.getGrade()).isEqualTo("S");
        assertThat(gameReport.getTopSpendingCategory()).isEqualTo("미집계");
        assertThat(gameReport.getTopSpendingRatio()).isEqualByComparingTo("0");
    }

    @DisplayName("주거 상실 신호가 있으면 턴 커밋은 세션을 압류 종료 상태로 닫아야 한다.")
    @Test
    void commitTurnAndCloseSessionAsForeclosure() {
        // given
        final User user = saveUser("turn-foreclosure@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 12)
        );
        final TurnDraft turnDraft = createTurnDraft(
            gameSession.getGameSessionId(),
            12,
            Money.zero(),
            Map.of()
        );
        given(turnDraftRepository.findBySessionId(gameSession.getGameSessionId()))
            .willReturn(Optional.of(turnDraft));
        given(gameWorldResultService.buildWorldResult(gameSession.getGameSessionId(), 50))
            .willReturn(GameWorldResult.of(
                GameWorldResult.CycleResult.of(
                    CyclePhase.RECOVERY,
                    CycleType.CYCLE_RATE_HIKE,
                    18,
                    "주거 상실 정산"
                ),
                List.of(),
                List.of(),
                GameWorldResult.HousingSnapshot.of(
                    HousingType.NONE,
                    null,
                    gameSession.getTargetPropertyId(),
                    true
                )
            ));

        // when
        commitTurnService.commitTurn(user.getId(), gameSession.getGameSessionId());

        // then
        final GameSession updated = gameSessionRepository.findById(gameSession.getGameSessionId())
            .orElseThrow();
        assertThat(updated.getSessionStatus()).isEqualTo(SessionStatus.FORECLOSURE);
    }

    @DisplayName("자가 주택 매물 연결이 끊긴 주거 상실 신호도 턴 커밋에서 압류 종료로 닫아야 한다.")
    @Test
    void commitTurnAndCloseOwnedHousingLossSessionAsForeclosure() {
        // given
        final User user = saveUser("turn-foreclosure-owned@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 12)
        );
        final TurnDraft turnDraft = createTurnDraft(
            gameSession.getGameSessionId(),
            12,
            Money.zero(),
            Map.of()
        );
        given(turnDraftRepository.findBySessionId(gameSession.getGameSessionId()))
            .willReturn(Optional.of(turnDraft));
        given(gameWorldResultService.buildWorldResult(gameSession.getGameSessionId(), 50))
            .willReturn(GameWorldResult.of(
                GameWorldResult.CycleResult.of(
                    CyclePhase.RECOVERY,
                    CycleType.CYCLE_RATE_HIKE,
                    18,
                    "자가 주거 상실 정산"
                ),
                List.of(),
                List.of(),
                GameWorldResult.HousingSnapshot.of(
                    HousingType.OWNED_APT,
                    null,
                    gameSession.getTargetPropertyId(),
                    true
                )
            ));

        // when
        commitTurnService.commitTurn(user.getId(), gameSession.getGameSessionId());

        // then
        final GameSession updated = gameSessionRepository.findById(gameSession.getGameSessionId())
            .orElseThrow();
        assertThat(updated.getSessionStatus()).isEqualTo(SessionStatus.FORECLOSURE);
    }

    @DisplayName("턴 커밋은 부동산 자산과 주식 평가금액을 분리해 응답과 timeline에 반영한다.")
    @Test
    void commitTurnWithSeparatedAssetSnapshots() {
        // given
        final User user = saveUser("turn-assets@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(
                user.getId(),
                7,
                999L,
                2_000_000L,
                11_000_000L,
                10_000_000L
            )
        );
        saveGameCareer(gameSession.getGameSessionId(), 31_200_000, EmploymentStatus.EMPLOYED);
        final Long currentPropertyId = saveOwnedHousing(gameSession.getGameSessionId(), 8_000_000L);
        saveGameLoan(gameSession.getGameSessionId(), 1_000_000, 25_000, 120);
        saveStockHolding(gameSession.getGameSessionId(), "BIGTECH", 10, 90_000, 100_000, 7);
        final TurnDraft turnDraft = createTurnDraft(gameSession.getGameSessionId(), 7);
        given(turnDraftRepository.findBySessionId(gameSession.getGameSessionId()))
            .willReturn(Optional.of(turnDraft));
        given(gameWorldResultService.buildWorldResult(gameSession.getGameSessionId(), 50))
            .willReturn(GameWorldResult.of(
                GameWorldResult.CycleResult.of(
                    CyclePhase.RECOVERY,
                    CycleType.CYCLE_RATE_HIKE,
                    18,
                    "자산 분리 정산"
                ),
                List.of(),
                List.of(),
                GameWorldResult.HousingSnapshot.of(
                    HousingType.OWNED_APT,
                    currentPropertyId,
                    gameSession.getTargetPropertyId(),
                    false
                )
            ));

        // when
        final CommitTurnResponse response = commitTurnService.commitTurn(
            user.getId(),
            gameSession.getGameSessionId()
        );

        // then
        assertThat(response.getUpdatedAssets().getCash()).isEqualTo(4_875_000L);
        assertThat(response.getUpdatedAssets().getLoan()).isEqualTo(1_000_000L);
        assertThat(response.getUpdatedAssets().getRealEstateValue()).isEqualTo(8_000_000L);
        assertThat(response.getUpdatedAssets().getNetAssets()).isEqualTo(12_875_000L);
        assertThat(response.getFlags().isCleared()).isFalse();

        final GameTimeline timeline = gameTimelineRepository.findAllByGameSessionIdOrderByTurnNumberAsc(
            gameSession.getGameSessionId()
        ).get(0);
        assertThat(timeline.getCash()).isEqualTo(4_875_000);
        assertThat(timeline.getTotalAssets()).isEqualTo(13_875_000);
        assertThat(timeline.getNetAssets()).isEqualTo(12_875_000);
        assertThat(timeline.getStockValueAmount()).isEqualTo(1_000_000);
        assertThat(timeline.getLoanBalanceAmount()).isEqualTo(1_000_000);
        assertThat(timeline.getSalaryAmount()).isEqualTo(2_600_000);
    }

    @DisplayName("턴 커밋은 급여, 고정지출, 주거비, 카드대금, 대출 상환을 실제 source 기준으로 합산한다.")
    @Test
    void commitTurnWithRealSettlementSources() {
        // given
        final User user = saveUser("turn-real-settlement@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 359, DataSourceType.MY_DATA)
        );
        saveGameCareer(gameSession.getGameSessionId(), 31_200_000, EmploymentStatus.EMPLOYED);
        saveUserAssetProfile(user.getId(), 2_600_000L, 400_000L);
        saveCardSpend(user.getId(), 80_000L);
        saveCardSpend(user.getId(), 50_000L);
        saveRentalHousing(gameSession.getGameSessionId(), 300_000L, 50_000L);
        saveGameLoan(gameSession.getGameSessionId(), 1_200_000, 25_000, 120);
        final TurnDraft turnDraft = createTurnDraft(
            gameSession.getGameSessionId(),
            359,
            Money.zero(),
            Map.of("health", 1, "fatigue", -2, "stress", -3, "happiness", 0, "knowledge", 4)
        );
        given(turnDraftRepository.findBySessionId(gameSession.getGameSessionId()))
            .willReturn(Optional.of(turnDraft));
        given(gameWorldResultService.buildWorldResult(gameSession.getGameSessionId(), 50))
            .willReturn(createWorldResult("실제 정산 합산"));

        // when
        final CommitTurnResponse response = commitTurnService.commitTurn(
            user.getId(),
            gameSession.getGameSessionId()
        );

        // then
        assertThat(response.getUpdatedAssets().getCash()).isEqualTo(3_695_000L);
        assertThat(response.getUpdatedAssets().getLoan()).isEqualTo(1_200_000L);
        assertThat(response.getUpdatedAssets().getNetAssets()).isEqualTo(7_495_000L);
        assertThat(response.getSettlementLog())
            .extracting(CommitTurnResponse.SettlementLogItemResponse::getCashChange)
            .contains(2_600_000L, -400_000L, -350_000L, -130_000L, -25_000L);
        assertThat(response.getSettlementLog())
            .extracting(CommitTurnResponse.SettlementLogItemResponse::getDescription)
            .doesNotContain("대기한다");

        final GameTimeline timeline = gameTimelineRepository.findAllByGameSessionIdOrderByTurnNumberAsc(
            gameSession.getGameSessionId()
        ).get(0);
        assertThat(timeline.getCash()).isEqualTo(3_695_000);
        assertThat(timeline.getLoanBalanceAmount()).isEqualTo(1_200_000);
        assertThat(timeline.getSalaryAmount()).isEqualTo(2_600_000);

        final GameSession updated = gameSessionRepository.findById(gameSession.getGameSessionId())
            .orElseThrow();
        assertThat(updated.getSessionStatus()).isEqualTo(SessionStatus.TIMEOUT);
        assertThat(gameReportRepository.findById(gameSession.getGameSessionId())).isPresent();
    }

    @DisplayName("턴 커밋은 pending 주식 주문을 체결하고 현금과 주식 평가액을 동시에 반영한다.")
    @Test
    void commitTurnSettlesPendingStockOrders() {
        // given
        final User user = saveUser("turn-stock-settlement@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 7)
        );
        saveGameCareer(gameSession.getGameSessionId(), 31_200_000, EmploymentStatus.EMPLOYED);
        saveStockHolding(gameSession.getGameSessionId(), "BIGTECH", 10, 90_000, 100_000, 7);
        stockOrderRepository.saveAndFlush(StockOrder.createBuyOrder(gameSession.getGameSessionId(), "BIGTECH", 2, 7));
        final TurnDraft turnDraft = createTurnDraft(
            gameSession.getGameSessionId(),
            7,
            Money.zero(),
            Map.of("health", 0, "fatigue", 0, "stress", 0, "happiness", 0, "knowledge", 0)
        );
        given(turnDraftRepository.findBySessionId(gameSession.getGameSessionId()))
            .willReturn(Optional.of(turnDraft));
        given(gameWorldResultService.buildWorldResult(gameSession.getGameSessionId(), 50))
            .willReturn(createWorldResult("주식 주문 체결"));

        // when
        final CommitTurnResponse response = commitTurnService.commitTurn(
            user.getId(),
            gameSession.getGameSessionId()
        );

        // then
        assertThat(response.getUpdatedAssets().getCash()).isEqualTo(4_400_000L);
        assertThat(response.getUpdatedAssets().getNetAssets()).isEqualTo(5_600_000L);

        final GameTimeline timeline = gameTimelineRepository.findAllByGameSessionIdOrderByTurnNumberAsc(
            gameSession.getGameSessionId()
        ).get(0);
        assertThat(timeline.getCash()).isEqualTo(4_400_000);
        assertThat(timeline.getStockValueAmount()).isEqualTo(1_200_000);
        assertThat(timeline.getTotalAssets()).isEqualTo(5_600_000);
    }

    @DisplayName("종료된 세션의 턴 커밋은 GAME_SESSION_CLOSED가 발생한다.")
    @Test
    void commitClosedSession() {
        // given
        final User user = saveUser("turn-commit-closed-service@example.com");
        final GameSession gameSession = createGameSession(user.getId(), 5);
        gameSession.markEnding(SessionStatus.TIMEOUT);
        final GameSession saved = gameSessionRepository.saveAndFlush(gameSession);

        // when & then
        assertThatThrownBy(() -> commitTurnService.commitTurn(user.getId(), saved.getGameSessionId()))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_CLOSED);
        then(turnDraftRepository).shouldHaveNoInteractions();
        then(gameWorldResultService).shouldHaveNoInteractions();
    }

    @DisplayName("커밋할 draft가 없으면 GAME_TURN_DRAFT_NOT_FOUND가 발생한다.")
    @Test
    void commitWithoutDraft() {
        // given
        final long failureTimerCountBefore = turnCommitTimerCount("failure");
        final User user = saveUser("turn-commit-no-draft-service@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 5)
        );
        given(turnDraftRepository.findBySessionId(gameSession.getGameSessionId()))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commitTurnService.commitTurn(user.getId(), gameSession.getGameSessionId()))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_TURN_DRAFT_NOT_FOUND);
        then(gameWorldResultService).shouldHaveNoInteractions();
        assertTurnCommitTimerRecorded("failure", failureTimerCountBefore);
        assertThat(gameTurnSlotRepository.findAllByGameSessionIdAndTurnNumberOrderBySlotIndex(
            gameSession.getGameSessionId(),
            5
        )).isEmpty();
    }

    @DisplayName("같은 세션의 턴 커밋이 동시에 들어오면 하나만 성공하고 나머지는 GAME_TURN_ALREADY_COMMITTED가 발생한다.")
    @Test
    void commitTurnConcurrently() {
        // given
        final User user = saveUser("turn-commit-concurrency@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 8)
        );
        saveGameCareer(gameSession.getGameSessionId(), 26_400_000, EmploymentStatus.EMPLOYED);
        final TurnDraft turnDraft = createTurnDraft(gameSession.getGameSessionId(), 8);
        final CountDownLatch firstCommitEnteredWorld = new CountDownLatch(1);
        final CountDownLatch releaseFirstCommit = new CountDownLatch(1);
        given(turnDraftRepository.findBySessionId(gameSession.getGameSessionId()))
            .willReturn(Optional.of(turnDraft));
        given(gameWorldResultService.buildWorldResult(gameSession.getGameSessionId(), 50))
            .willAnswer(invocation -> {
                firstCommitEnteredWorld.countDown();
                awaitLatch(releaseFirstCommit);
                return createWorldResult("동시 커밋 정산");
            });

        final ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            final Future<CommitTurnResponse> firstCommit = executorService.submit(
                () -> commitTurnService.commitTurn(user.getId(), gameSession.getGameSessionId())
            );
            awaitLatch(firstCommitEnteredWorld);
            final Future<CommitTurnResponse> secondCommit = executorService.submit(
                () -> commitTurnService.commitTurn(user.getId(), gameSession.getGameSessionId())
            );
            releaseFirstCommit.countDown();

            final CommitTurnResponse response = awaitFuture(firstCommit);
            final Throwable failure = awaitFailure(secondCommit);

            assertThat(response.getTurnNumber()).isEqualTo(8);
            assertThat(failure).isInstanceOf(HomerunException.class);
            assertThat(((HomerunException) failure).getErrorCode())
                .isEqualTo(ErrorCode.GAME_TURN_ALREADY_COMMITTED);
            assertThat(gameTurnSlotRepository.findAllByGameSessionIdAndTurnNumberOrderBySlotIndex(
                gameSession.getGameSessionId(),
                8
            )).hasSize(3);
            assertThat(settlementLogRepository.findAllByGameSessionIdAndTurnNumberOrderBySettlementLogIdAsc(
                gameSession.getGameSessionId(),
                8
            )).hasSize(13);
            assertThat(gameTimelineRepository.findAllByGameSessionIdOrderByTurnNumberAsc(
                gameSession.getGameSessionId()
            )).hasSize(1);
            then(gameWorldResultService).should().buildWorldResult(gameSession.getGameSessionId(), 50);
            then(turnDraftRepository).should().deleteBySessionId(gameSession.getGameSessionId());
        } finally {
            executorService.shutdownNow();
        }
    }

    @DisplayName("턴 커밋과 세션 삭제가 겹쳐도 삭제는 커밋 이후 최신 데이터를 정리하고 종료된다.")
    @Test
    void deleteWhileCommitInProgress() {
        // given
        final User user = saveUser("turn-commit-delete-collision@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 9)
        );
        saveGameCareer(gameSession.getGameSessionId(), 26_400_000, EmploymentStatus.EMPLOYED);
        final TurnDraft turnDraft = createTurnDraft(gameSession.getGameSessionId(), 9);
        final CountDownLatch commitEnteredWorld = new CountDownLatch(1);
        final CountDownLatch releaseCommit = new CountDownLatch(1);
        final CountDownLatch cleanupStarted = new CountDownLatch(1);
        given(turnDraftRepository.findBySessionId(gameSession.getGameSessionId()))
            .willReturn(Optional.of(turnDraft));
        given(gameWorldResultService.buildWorldResult(gameSession.getGameSessionId(), 50))
            .willAnswer(invocation -> {
                commitEnteredWorld.countDown();
                awaitLatch(releaseCommit);
                return createWorldResult("삭제 충돌 정산");
            });
        willAnswer(invocation -> {
            cleanupStarted.countDown();
            new GameSessionCleanupService(jdbcTemplate).deleteAllByGameSessionId(gameSession.getGameSessionId());
            return null;
        }).given(gameSessionCleanupService).deleteAllByGameSessionId(gameSession.getGameSessionId());

        final ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            final Future<CommitTurnResponse> commitFuture = executorService.submit(
                () -> commitTurnService.commitTurn(user.getId(), gameSession.getGameSessionId())
            );
            awaitLatch(commitEnteredWorld);
            final Future<Void> deleteFuture = executorService.submit(() -> {
                gameSessionService.delete(user.getId(), gameSession.getGameSessionId());
                return null;
            });
            assertThat(awaitLatch(cleanupStarted, 300, TimeUnit.MILLISECONDS)).isFalse();
            releaseCommit.countDown();

            final CommitTurnResponse response = awaitFuture(commitFuture);
            awaitFuture(deleteFuture);

            assertThat(response.getTurnNumber()).isEqualTo(9);
            assertThat(gameSessionRepository.findById(gameSession.getGameSessionId())).isEmpty();
            assertThat(gameTurnSlotRepository.findAllByGameSessionIdAndTurnNumberOrderBySlotIndex(
                gameSession.getGameSessionId(),
                9
            )).isEmpty();
            assertThat(settlementLogRepository.findAllByGameSessionIdAndTurnNumberOrderBySettlementLogIdAsc(
                gameSession.getGameSessionId(),
                9
            )).isEmpty();
            assertThat(gameTimelineRepository.findAllByGameSessionIdOrderByTurnNumberAsc(
                gameSession.getGameSessionId()
            )).isEmpty();
            assertThat(gameReportRepository.findById(gameSession.getGameSessionId())).isEmpty();
        } finally {
            releaseCommit.countDown();
            executorService.shutdownNow();
        }
    }

    private User saveUser(final String email) {
        return userRepository.save(User.register(Email.of(email), "tester", "hashed-password"));
    }

    private GameWorldResult createWorldResult(final String description) {
        return GameWorldResult.of(
            GameWorldResult.CycleResult.of(
                CyclePhase.RECOVERY,
                CycleType.CYCLE_RATE_HIKE,
                18,
                description
            ),
            List.of(),
            List.of(),
            GameWorldResult.HousingSnapshot.empty()
        );
    }

    private GameSession createGameSession(final Long userId, final Integer currentTurn) {
        return createGameSession(userId, currentTurn, DataSourceType.PROFILE);
    }

    private GameSession createGameSession(
        final Long userId,
        final Integer currentTurn,
        final DataSourceType dataSourceType
    ) {
        return createGameSession(userId, currentTurn, 101L, 2_000_000L, 2_000_000L, 2_000_000L, dataSourceType);
    }

    private GameSession createGameSession(
        final Long userId,
        final Integer currentTurn,
        final Long targetPropertyId,
        final long cashBalance,
        final long totalAssets,
        final long netWorth
    ) {
        return createGameSession(
            userId,
            currentTurn,
            targetPropertyId,
            cashBalance,
            totalAssets,
            netWorth,
            DataSourceType.PROFILE
        );
    }

    private GameSession createGameSession(
        final Long userId,
        final Integer currentTurn,
        final Long targetPropertyId,
        final long cashBalance,
        final long totalAssets,
        final long netWorth,
        final DataSourceType dataSourceType
    ) {
        final CycleState currentCycleState = CycleState.of(
            CyclePhase.BOOM,
            CycleType.CYCLE_BOOM,
            24
        );
        final GameSession gameSession = GameSession.create(
            userId,
            1,
            "커밋 세션",
            CharacterType.FEMALE,
            JobType.STARTUP,
            HousingType.STUDIO,
            "11",
            "11680",
            targetPropertyId,
            dataSourceType
        );
        gameSession.initializeCapital(
            Money.of(cashBalance),
            Money.of(totalAssets),
            Money.of(netWorth),
            LocalDate.of(2026, 1, 1),
            currentCycleState
        );
        gameSession.advanceTurn(
            currentTurn,
            LocalDate.of(2026, 1, 1),
            Money.of(cashBalance),
            Money.of(totalAssets),
            Money.of(netWorth),
            currentCycleState
        );
        return gameSession;
    }

    private void saveGameCareer(
        final Long gameSessionId,
        final int salary,
        final EmploymentStatus employmentStatus
    ) {
        gameCareerRepository.saveAndFlush(GameCareer.builder()
            .gameId(Math.toIntExact(gameSessionId))
            .salary(salary)
            .employmentStatus(employmentStatus)
            .build());
    }

    private Long saveOwnedHousing(final Long gameSessionId, final long basePriceAmount) {
        final RealEstateProperty property = realEstatePropertyRepository.saveAndFlush(
            RealEstateProperty.create(
                "provider-" + gameSessionId,
                "테스트 아파트",
                "서울시 강남구 역삼동",
                "11",
                "11680",
                Money.of(basePriceAmount),
                BigDecimal.valueOf(37.4979),
                BigDecimal.valueOf(127.0276),
                HousingType.OWNED_APT,
                List.of()
            )
        );
        gameHousingRepository.saveAndFlush(GameHousing.create(
            gameSessionId,
            HousingType.OWNED_APT,
            Money.zero(),
            Money.zero(),
            Money.zero(),
            property.getPropertyId()
        ));
        return property.getPropertyId();
    }

    private GameEvent createPhoneEvent(final String eventCode) {
        return GameEvent.create(
            "PHONE",
            eventCode,
            "월드 이벤트",
            EventPresentationType.PHONE,
            EventTriggerType.PROBABILITY,
            BigDecimal.ONE,
            false,
            null,
            null,
            null,
            "월드 이벤트 설명",
            true
        );
    }

    private void saveRentalHousing(
        final Long gameSessionId,
        final long monthlyRentAmount,
        final long maintenanceFeeAmount
    ) {
        gameHousingRepository.saveAndFlush(GameHousing.create(
            gameSessionId,
            HousingType.STUDIO,
            Money.of(5_000_000L),
            Money.of(monthlyRentAmount),
            Money.of(maintenanceFeeAmount),
            null
        ));
    }

    private void saveStockHolding(
        final Long gameSessionId,
        final String stockCode,
        final int quantity,
        final int averagePurchasePriceAmount,
        final int currentPriceAmount,
        final int currentTurn
    ) {
        stockHoldingRepository.saveAndFlush(
            StockHolding.create(gameSessionId, stockCode, averagePurchasePriceAmount, quantity)
        );
        gameStockMarketStateRepository.saveAndFlush(
            GameStockMarketState.initializeFrom(gameSessionId, stockCode, currentPriceAmount, currentTurn)
        );
    }

    private void saveGameLoan(
        final Long gameSessionId,
        final int principalAmount,
        final int monthlyPaymentAmount,
        final int remainingRepaymentTurns
    ) {
        gameLoanRepository.saveAndFlush(GameLoan.create(
            gameSessionId,
            "테스트 대출",
            principalAmount,
            BigDecimal.valueOf(3.5),
            monthlyPaymentAmount,
            remainingRepaymentTurns,
            "TEST-LOAN",
            RepaymentType.EQUAL_PRINCIPAL_INTEREST
        ));
    }

    private void saveUserAssetProfile(
        final Long userId,
        final long monthlySalaryAmount,
        final long monthlyFixedExpenseAmount
    ) {
        userAssetProfileRepository.saveAndFlush(UserAssetProfile.create(
            userId,
            Long.valueOf(0L),
            Integer.valueOf(25),
            Long.valueOf(monthlySalaryAmount),
            Long.valueOf(monthlyFixedExpenseAmount),
            JobType.STARTUP
        ));
    }

    private void saveCardSpend(final Long userId, final long amount) {
        userAssetCardSpendRepository.saveAndFlush(
            UserAssetCardSpend.create(userId, SpendingCategory.LIVING, amount)
        );
    }

    private TurnDraft createTurnDraft(final Long sessionId, final Integer turnNumber) {
        return createTurnDraft(
            sessionId,
            turnNumber,
            Money.of(300_000L),
            Map.of(
                "health", 3,
                "fatigue", -14,
                "stress", -8,
                "happiness", 4,
                "knowledge", 8
            )
        );
    }

    private TurnDraft createTurnDraft(
        final Long sessionId,
        final Integer turnNumber,
        final Money previewCashChange,
        final Map<String, Integer> previewStatChanges
    ) {
        return createTurnDraft(
            sessionId,
            turnNumber,
            List.of(
                TurnDraftSlot.of(0, ActionType.STUDY),
                TurnDraftSlot.of(1, ActionType.HOBBY),
                TurnDraftSlot.of(2, ActionType.SIDE_JOB)
            ),
            previewCashChange,
            previewStatChanges
        );
    }

    private TurnDraft createTurnDraft(
        final Long sessionId,
        final Integer turnNumber,
        final List<TurnDraftSlot> slots,
        final Money previewCashChange,
        final Map<String, Integer> previewStatChanges
    ) {
        return TurnDraft.of(
            sessionId,
            turnNumber,
            slots,
            previewCashChange,
            previewCashChange,
            previewCashChange,
            previewStatChanges
        );
    }

    private void awaitLatch(final CountDownLatch latch) {
        try {
            if (latch.await(5, TimeUnit.SECONDS)) {
                return;
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError(exception);
        }

        throw new AssertionError("동시성 테스트 대기 시간이 초과되었습니다.");
    }

    private boolean awaitLatch(
        final CountDownLatch latch,
        final long timeout,
        final TimeUnit timeUnit
    ) {
        try {
            return latch.await(timeout, timeUnit);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError(exception);
        }
    }

    private <T> T awaitFuture(final Future<T> future) {
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError(exception);
        } catch (ExecutionException exception) {
            final Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new AssertionError(cause);
        } catch (TimeoutException exception) {
            throw new AssertionError("동시성 테스트 future 대기 시간이 초과되었습니다.", exception);
        }
    }

    private Throwable awaitFailure(final Future<?> future) {
        try {
            future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError(exception);
        } catch (ExecutionException exception) {
            return exception.getCause();
        } catch (TimeoutException exception) {
            throw new AssertionError("동시성 테스트 future 대기 시간이 초과되었습니다.", exception);
        }

        throw new AssertionError("실패를 기대한 future가 성공했습니다.");
    }

    private void assertTurnCommitTimerRecorded(final String result, final long timerCountBefore) {
        final Timer timer = meterRegistry.get(TURN_COMMIT_DURATION)
            .tag("boundary", COMMIT_TURN_BOUNDARY)
            .tag("result", result)
            .timer();
        assertThat(timer.count()).isEqualTo(timerCountBefore + 1);
        assertThat(timer.totalTime(TimeUnit.NANOSECONDS)).isGreaterThanOrEqualTo(0L);
    }

    private long turnCommitTimerCount(final String result) {
        final Timer timer = meterRegistry.find(TURN_COMMIT_DURATION)
            .tag("boundary", COMMIT_TURN_BOUNDARY)
            .tag("result", result)
            .timer();
        if (timer == null) {
            return 0L;
        }
        return timer.count();
    }
}
