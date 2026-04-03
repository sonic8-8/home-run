package io.ssafy.p.j14c103.homerun.api.acceptance.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ssafy.p.j14c103.homerun.api.service.world.GameWorldRollService;
import io.ssafy.p.j14c103.homerun.api.service.world.WorldContentSeedService;
import io.ssafy.p.j14c103.homerun.api.service.world.WorldEventTriggerService;
import io.ssafy.p.j14c103.homerun.api.service.world.WorldPendingEventProviderService;
import io.ssafy.p.j14c103.homerun.api.service.world.WorldPendingEventQueueService;
import io.ssafy.p.j14c103.homerun.api.service.world.response.PendingEventsProviderResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.result.GameWorldResult;
import io.ssafy.p.j14c103.homerun.config.JwtTokenProvider;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.GameStatRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareerRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.SessionStatus;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousingRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.GameLoan;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.GameLoanRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.RepaymentType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameReport;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameReportAchievement;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameReportRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameTimeline;
import io.ssafy.p.j14c103.homerun.domain.gamesession.report.GameTimelineRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.settlement.SettlementLogRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.GameSessionTurnSlotRepository;
import io.ssafy.p.j14c103.homerun.domain.history.GameplayHistory;
import io.ssafy.p.j14c103.homerun.domain.history.GameplayHistoryRepository;
import io.ssafy.p.j14c103.homerun.domain.history.event.GameEventLog;
import io.ssafy.p.j14c103.homerun.domain.history.event.GameEventLogRepository;
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
import io.ssafy.p.j14c103.homerun.domain.world.event.EventChoiceRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventConditionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventEffectRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventPresentationType;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventTriggerType;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEvent;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEventRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.GamePendingEventRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.domain.world.news.NewsMaster;
import io.ssafy.p.j14c103.homerun.domain.world.news.NewsMasterRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.support.HttpIntegrationTestSupport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Tag("container")
@Testcontainers(disabledWithoutDocker = true)
class GameCoreContractAcceptanceTest extends HttpIntegrationTestSupport {

    @Container
    static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>(
        DockerImageName.parse("redis:7.2-alpine")
    ).withExposedPorts(6379);

    @DynamicPropertySource
    static void overrideRedisProperties(final DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", REDIS_CONTAINER::getFirstMappedPort);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private WorldContentSeedService worldContentSeedService;

    @Autowired
    private WorldEventTriggerService worldEventTriggerService;

    @Autowired
    private WorldPendingEventQueueService worldPendingEventQueueService;

    @Autowired
    private WorldPendingEventProviderService worldPendingEventProviderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private GameStatRepository gameStatRepository;

    @Autowired
    private GameCareerRepository gameCareerRepository;

    @Autowired
    private GameSessionTurnSlotRepository gameTurnSlotRepository;

    @Autowired
    private SettlementLogRepository settlementLogRepository;

    @Autowired
    private GameReportRepository gameReportRepository;

    @Autowired
    private GameTimelineRepository gameTimelineRepository;

    @Autowired
    private GameHousingRepository gameHousingRepository;

    @Autowired
    private GameLoanRepository gameLoanRepository;

    @Autowired
    private GameplayHistoryRepository gameplayHistoryRepository;

    @Autowired
    private GameNewsLogRepository gameNewsLogRepository;

    @Autowired
    private GameEventLogRepository gameEventLogRepository;

    @Autowired
    private GamePendingEventRepository gamePendingEventRepository;

    @Autowired
    private UserAssetProfileRepository userAssetProfileRepository;

    @Autowired
    private UserAssetCardSpendRepository userAssetCardSpendRepository;

    @Autowired
    private EventEffectRepository eventEffectRepository;

    @Autowired
    private EventConditionRepository eventConditionRepository;

    @Autowired
    private EventChoiceRepository eventChoiceRepository;

    @Autowired
    private GameEventRepository gameEventRepository;

    @Autowired
    private NewsMasterRepository newsMasterRepository;

    @MockitoBean
    private GameWorldRollService gameWorldRollService;

    @BeforeEach
    void setUp() {
        given(gameWorldRollService.resolveTurnRoll(anyLong(), anyInt())).willReturn(50);
        given(gameWorldRollService.deriveRoll(anyLong(), anyInt(), anyString())).willReturn(50);
        given(gameWorldRollService.deriveProbabilityRoll(anyLong(), anyInt(), anyString()))
            .willReturn(BigDecimal.ONE);
    }

    @AfterEach
    void tearDown() {
        final Set<String> keys = stringRedisTemplate.keys("session:*:turn");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }

        gameEventLogRepository.deleteAllInBatch();
        gameNewsLogRepository.deleteAllInBatch();
        gameplayHistoryRepository.deleteAllInBatch();
        gamePendingEventRepository.deleteAllInBatch();
        gameTimelineRepository.deleteAllInBatch();
        gameReportRepository.deleteAllInBatch();
        settlementLogRepository.deleteAllInBatch();
        gameTurnSlotRepository.deleteAllInBatch();
        gameLoanRepository.deleteAllInBatch();
        gameHousingRepository.deleteAllInBatch();
        gameCareerRepository.deleteAllInBatch();
        gameStatRepository.deleteAllInBatch();
        userAssetCardSpendRepository.deleteAllInBatch();
        userAssetProfileRepository.deleteAllInBatch();
        gameSessionRepository.deleteAllInBatch();
        eventEffectRepository.deleteAllInBatch();
        eventConditionRepository.deleteAllInBatch();
        eventChoiceRepository.deleteAllInBatch();
        gameEventRepository.deleteAllInBatch();
        newsMasterRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("현재 턴 조회와 최신 뉴스 조회는 같은 저장 뉴스 source를 읽는다.")
    @Test
    void getTurnAndLatestNewsReadSameSavedNewsSource() throws Exception {
        final SessionFixture fixture = saveSessionFixture(
            "turn-news-user@example.com",
            12,
            CycleState.of(CyclePhase.BOOM, CycleType.CYCLE_BOOM, 1)
        );
        newsMasterRepository.saveAndFlush(NewsMaster.createAiNews(
            "NEWS-001",
            "부동산 시장 과열 경고",
            "negative",
            "테스트 언론",
            "시장 과열 신호가 확인됐다.",
            "BOOM_TO_CRISIS",
            "테스트 이유",
            null,
            null,
            null,
            null
        ));
        gameNewsLogRepository.saveAndFlush(GameNewsLog.create(
            fixture.session().getGameSessionId(),
            12,
            "NEWS-001",
            "부동산 시장 과열 경고",
            LocalDate.of(2026, 1, 1)
        ));

        mockMvc.perform(get("/api/games/sessions/{sessionId}/turn", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.turnNumber").value(12))
            .andExpect(jsonPath("$.data.economicCycle.phase").value("BOOM"))
            .andExpect(jsonPath("$.data.news[0].newsId").value("NEWS-001"))
            .andExpect(jsonPath("$.data.news[0].headline").value("부동산 시장 과열 경고"));

        mockMvc.perform(get("/api/games/sessions/{sessionId}/news/latest", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.turnNumber").value(12))
            .andExpect(jsonPath("$.data.news[0].newsId").value("NEWS-001"))
            .andExpect(jsonPath("$.data.news[0].headline").value("부동산 시장 과열 경고"));
    }

    @DisplayName("currentTurn이 0인 새 세션도 턴과 최신 뉴스 조회를 500 없이 처리해야 한다.")
    @Test
    void getTurnAndLatestNewsForFreshInProgressSession() throws Exception {
        final SessionFixture fixture = saveSessionFixture(
            "turn-zero-user@example.com",
            0,
            CycleState.of(CyclePhase.BOOM, CycleType.CYCLE_BOOM, 1)
        );
        newsMasterRepository.saveAndFlush(NewsMaster.createAiNews(
            "NEWS-000",
            "부동산 시장 안정세",
            "neutral",
            "테스트 언론",
            "currentTurn 0 세션도 최신 뉴스를 읽을 수 있어야 한다.",
            "BOOM_TO_BOOM",
            "테스트 이유",
            null,
            null,
            null,
            null
        ));

        mockMvc.perform(get("/api/games/sessions/{sessionId}/turn", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.turnNumber").value(0))
            .andExpect(jsonPath("$.data.news[0].newsId").value("NEWS-000"))
            .andExpect(jsonPath("$.data.news[0].headline").value("부동산 시장 안정세"));

        mockMvc.perform(get("/api/games/sessions/{sessionId}/news/latest", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.turnNumber").value(0))
            .andExpect(jsonPath("$.data.news[0].newsId").value("NEWS-000"))
            .andExpect(jsonPath("$.data.news[0].headline").value("부동산 시장 안정세"));
    }

    @DisplayName("턴 커밋은 world result에서 생성한 뉴스와 이벤트를 다음 턴 조회 계약에 materialize한다.")
    @Test
    void commitTurnMaterializesWorldResultIntoNewsAndPendingReadBack() throws Exception {
        final SessionFixture fixture = saveSessionFixture(
            "turn-world-result-user@example.com",
            12,
            CycleState.of(CyclePhase.BOOM, CycleType.CYCLE_BOOM, 1)
        );
        saveGameStat(fixture.session(), 70, 20, 20, 50, 50, 12);
        saveGameCareer(fixture.session(), 12, 31_000_000);
        newsMasterRepository.saveAndFlush(NewsMaster.createAiNews(
            "NEWS-COMMIT-001",
            "호황 꺾임 신호",
            "negative",
            "테스트 언론",
            "commit 이후 최신 뉴스는 저장된 source를 읽어야 한다.",
            "BOOM_TO_CRISIS",
            "테스트 사유",
            null,
            null,
            null,
            null
        ));
        gameEventRepository.saveAndFlush(GameEvent.create(
            "PHONE",
            "EVT-COMMIT-001",
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
        ));
        given(gameWorldRollService.resolveTurnRoll(fixture.session().getGameSessionId(), 13))
            .willReturn(60);
        given(gameWorldRollService.deriveRoll(fixture.session().getGameSessionId(), 60, "cycle-subtype"))
            .willReturn(1);
        given(gameWorldRollService.deriveRoll(fixture.session().getGameSessionId(), 60, "cycle-duration"))
            .willReturn(1);
        given(gameWorldRollService.deriveProbabilityRoll(
            fixture.session().getGameSessionId(),
            60,
            "event:EVT-COMMIT-001"
        )).willReturn(BigDecimal.ZERO);

        mockMvc.perform(post("/api/games/sessions/{sessionId}/turn/slots", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(turnSlotsRequest())))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/games/sessions/{sessionId}/turn/commit", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.turnNumber").value(12))
            .andExpect(jsonPath("$.data.endingStatus").value("IN_PROGRESS"))
            .andExpect(jsonPath("$.data.flags.hasEvent").value(true));

        mockMvc.perform(get("/api/games/sessions/{sessionId}/news/history", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.newsHistories[0].newsId").value("NEWS-COMMIT-001"))
            .andExpect(jsonPath("$.data.newsHistories[0].headline").value("호황 꺾임 신호"));

        mockMvc.perform(get("/api/games/sessions/{sessionId}/events/pending", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.events[0].title").value("월드 이벤트"));

        mockMvc.perform(get("/api/games/sessions/{sessionId}/news/latest", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.turnNumber").value(13))
            .andExpect(jsonPath("$.data.news[0].newsId").value("NEWS-COMMIT-001"))
            .andExpect(jsonPath("$.data.news[0].headline").value("호황 꺾임 신호"));

        mockMvc.perform(get("/api/games/sessions/{sessionId}/turn", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.turnNumber").value(13))
            .andExpect(jsonPath("$.data.economicCycle.phase").value("CRISIS"))
            .andExpect(jsonPath("$.data.news[0].newsId").value("NEWS-COMMIT-001"))
            .andExpect(jsonPath("$.data.news[0].headline").value("호황 꺾임 신호"));
    }

    @DisplayName("커밋할 draft가 없으면 public 계약상 400을 반환해야 한다.")
    @Test
    void commitTurnWithoutDraftReturnsBadRequest() throws Exception {
        final SessionFixture fixture = saveSessionFixture(
            "turn-no-draft-user@example.com",
            12,
            CycleState.of(CyclePhase.BOOM, CycleType.CYCLE_BOOM, 1)
        );

        mockMvc.perform(post("/api/games/sessions/{sessionId}/turn/commit", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_TURN_DRAFT_NOT_FOUND.getCode()))
            .andExpect(jsonPath("$.message").value(ErrorCode.GAME_TURN_DRAFT_NOT_FOUND.getMessage()));
    }

    @DisplayName("종료된 세션의 턴 커밋은 409와 GAME_SESSION_CLOSED를 반환한다.")
    @Test
    void commitTurnRejectsClosedSession() throws Exception {
        final SessionFixture fixture = saveSessionFixture(
            "turn-closed-user@example.com",
            12,
            CycleState.of(CyclePhase.BOOM, CycleType.CYCLE_BOOM, 1)
        );
        fixture.session().markEnding(SessionStatus.TIMEOUT);
        gameSessionRepository.saveAndFlush(fixture.session());

        mockMvc.perform(post("/api/games/sessions/{sessionId}/turn/commit", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken()))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_SESSION_CLOSED.getCode()))
            .andExpect(jsonPath("$.message").value(ErrorCode.GAME_SESSION_CLOSED.getMessage()));
    }

    @DisplayName("다른 사용자의 턴 커밋은 403과 GAME_SESSION_FORBIDDEN을 반환한다.")
    @Test
    void commitTurnRejectsOtherUsersSession() throws Exception {
        final SessionFixture ownerFixture = saveSessionFixture(
            "turn-owner-commit@example.com",
            12,
            CycleState.of(CyclePhase.BOOM, CycleType.CYCLE_BOOM, 1)
        );
        final User otherUser = saveUser("turn-requester-commit@example.com");

        mockMvc.perform(post("/api/games/sessions/{sessionId}/turn/commit", ownerFixture.session().getGameSessionId())
                .header(AUTHORIZATION, bearer(otherUser)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_SESSION_FORBIDDEN.getCode()))
            .andExpect(jsonPath("$.message").value(ErrorCode.GAME_SESSION_FORBIDDEN.getMessage()));
    }

    @DisplayName("같은 턴 커밋을 반복 호출해도 상태는 한 번만 반영되고 이후 호출은 400을 반환한다.")
    @Test
    void commitTurnRepeatKeepsSinglePersistedResult() throws Exception {
        final SessionFixture fixture = saveSessionFixture(
            "turn-repeat-commit-user@example.com",
            12,
            CycleState.of(CyclePhase.BOOM, CycleType.CYCLE_BOOM, 1)
        );
        saveGameStat(fixture.session(), 70, 20, 20, 50, 50, 12);
        saveGameCareer(fixture.session(), 12, 31_000_000);

        mockMvc.perform(post("/api/games/sessions/{sessionId}/turn/slots", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(turnSlotsRequest())))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/games/sessions/{sessionId}/turn/commit", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken()))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/games/sessions/{sessionId}/turn/commit", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_TURN_DRAFT_NOT_FOUND.getCode()))
            .andExpect(jsonPath("$.message").value(ErrorCode.GAME_TURN_DRAFT_NOT_FOUND.getMessage()));

        assertThat(gameTurnSlotRepository.findAllByGameSessionIdAndTurnNumberOrderBySlotIndex(
            fixture.session().getGameSessionId(),
            12
        )).hasSize(3);
        assertThat(settlementLogRepository.findAllByGameSessionIdAndTurnNumberOrderBySettlementLogIdAsc(
            fixture.session().getGameSessionId(),
            12
        )).isNotEmpty();
        assertThat(gameTimelineRepository.findAllByGameSessionIdOrderByTurnNumberAsc(
            fixture.session().getGameSessionId()
        )).hasSize(1);
    }

    @DisplayName("다른 사용자의 턴 조회는 403으로 차단된다.")
    @Test
    void getTurnRejectsOtherUsersSession() throws Exception {
        final SessionFixture ownerFixture = saveSessionFixture(
            "turn-owner@example.com",
            12,
            CycleState.of(CyclePhase.BOOM, CycleType.CYCLE_BOOM, 1)
        );
        final User otherUser = saveUser("turn-requester@example.com");

        mockMvc.perform(get("/api/games/sessions/{sessionId}/turn", ownerFixture.session().getGameSessionId())
                .header(AUTHORIZATION, bearer(otherUser)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_SESSION_FORBIDDEN.getCode()));
    }

    @DisplayName("행동 조회와 뉴스 히스토리 조회는 현재 공개 계약을 그대로 반환한다.")
    @Test
    void getTurnActionsAndNewsHistoryExposeCurrentContracts() throws Exception {
        final SessionFixture fixture = saveSessionFixture(
            "turn-actions-user@example.com",
            12,
            CycleState.of(CyclePhase.RECOVERY, CycleType.CYCLE_RATE_HIKE, 18)
        );
        newsMasterRepository.saveAndFlush(NewsMaster.createAiNews(
            "NEWS-011",
            "기준금리 동결",
            "neutral",
            "테스트 언론",
            "테스트 기사 11",
            "RECOVERY_TO_RECOVERY",
            "테스트 이유",
            null,
            null,
            null,
            null
        ));
        newsMasterRepository.saveAndFlush(NewsMaster.createAiNews(
            "NEWS-012",
            "채용 한파 심화",
            "negative",
            "테스트 언론",
            "테스트 기사 12",
            "RECOVERY_TO_CRISIS",
            "테스트 이유",
            null,
            null,
            null,
            null
        ));
        gameNewsLogRepository.saveAndFlush(GameNewsLog.create(
            fixture.session().getGameSessionId(),
            11,
            "NEWS-011",
            "기준금리 동결",
            LocalDate.of(2025, 12, 1)
        ));
        gameNewsLogRepository.saveAndFlush(GameNewsLog.create(
            fixture.session().getGameSessionId(),
            12,
            "NEWS-012",
            "채용 한파 심화",
            LocalDate.of(2026, 1, 1)
        ));

        mockMvc.perform(get("/api/games/sessions/{sessionId}/turn/actions", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.shopping").isArray())
            .andExpect(jsonPath("$.data.activities").isArray())
            .andExpect(jsonPath("$.data.activities[?(@.actionType == 'SIDE_JOB')]").isNotEmpty());

        mockMvc.perform(get("/api/games/sessions/{sessionId}/news/history", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.newsHistories[0].newsId").value("NEWS-012"))
            .andExpect(jsonPath("$.data.newsHistories[1].newsId").value("NEWS-011"));
    }

    @DisplayName("pending 이벤트 조회와 resolve는 같은 세션 상태 변화를 외부 계약으로 닫는다.")
    @Test
    void pendingEventsAndResolveWorkOverHttp() throws Exception {
        worldContentSeedService.seed();
        final SessionFixture fixture = saveSessionFixture(
            "event-flow-user@example.com",
            12,
            CycleState.of(CyclePhase.BOOM, CycleType.CYCLE_BOOM, 1)
        );
        saveGameStat(fixture.session(), 70, 10, 10, 50, 70, 12);
        saveGameCareer(fixture.session(), 12, 31_000_000);

        final List<GameWorldResult.EventCandidate> eventCandidates = worldEventTriggerService.calculateEventCandidates(
            fixture.session().getGameSessionId(),
            Map.of(
                "EVT-VOICE-001", new BigDecimal("0.0100"),
                "EVT-FAMILY-001", new BigDecimal("0.0200"),
                "EVT-OVERTIME-001", new BigDecimal("0.1500")
            )
        );
        worldPendingEventQueueService.enqueuePendingEvents(
            fixture.session().getGameSessionId(),
            eventCandidates
        );

        final PendingEventsProviderResponse pendingEvents = worldPendingEventProviderService.getPendingEvents(
            fixture.session().getGameSessionId()
        );
        final PendingEventsProviderResponse.PendingEventItem familyEvent = pendingEvents.getEvents().stream()
            .filter(event -> "경조사".equals(event.getTitle()))
            .findFirst()
            .orElseThrow();
        final PendingEventsProviderResponse.PendingEventChoiceItem attendChoice =
            familyEvent.getChoices().stream()
                .filter(choice -> "A".equals(choice.getChoiceCode()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(get("/api/games/sessions/{sessionId}/events/pending", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.events").isArray())
            .andExpect(jsonPath("$.data.events[?(@.title == '경조사')]").isNotEmpty());

        mockMvc.perform(post(
                    "/api/games/sessions/{sessionId}/events/{eventId}/resolve",
                    fixture.session().getGameSessionId(),
                    familyEvent.getEventId()
                )
                .header(AUTHORIZATION, fixture.bearerToken())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("choiceId", attendChoice.getChoiceId()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.eventId").value(familyEvent.getEventId()))
            .andExpect(jsonPath("$.data.selectedChoiceCode").value("A"));

        mockMvc.perform(get("/api/games/sessions/{sessionId}/events/pending", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.events[?(@.title == '경조사')]").isEmpty());
    }

    @DisplayName("엔딩 리포트와 로그 조회는 같은 종료 세션 read-back을 반환한다.")
    @Test
    void endingAndLogsReadBackEndedSession() throws Exception {
        final SessionFixture fixture = saveSessionFixture(
            "ending-readback-user@example.com",
            12,
            CycleState.of(CyclePhase.RECOVERY, CycleType.CYCLE_RATE_HIKE, 18)
        );
        fixture.session().markEnding(SessionStatus.CLEAR);
        gameSessionRepository.saveAndFlush(fixture.session());

        gameReportRepository.saveAndFlush(GameReport.create(
            fixture.session().getGameSessionId(),
            SessionStatus.CLEAR,
            "부동산 갑부",
            120_000_000,
            80_000_000,
            "S",
            500_000_000,
            40_000_000,
            "식비",
            BigDecimal.valueOf(35.2),
            List.of(GameReportAchievement.of("첫 내집 마련", "/images/badges/first-house.png"))
        ));
        gameTimelineRepository.saveAndFlush(GameTimeline.create(
            fixture.session().getGameSessionId(),
            12,
            LocalDate.of(2026, 12, 1),
            5_000_000,
            25_000_000,
            75_000_000,
            5_000_000,
            50_000_000,
            2_200_000
        ));
        gameTimelineRepository.saveAndFlush(GameTimeline.create(
            fixture.session().getGameSessionId(),
            1,
            LocalDate.of(2026, 1, 1),
            13_000_000,
            13_000_000,
            13_000_000,
            0,
            0,
            2_000_000
        ));
        gameNewsLogRepository.saveAndFlush(GameNewsLog.create(
            fixture.session().getGameSessionId(),
            8,
            "NEWS-001",
            "금리 인하 기조 지속",
            LocalDate.of(2026, 8, 1)
        ));
        gameEventLogRepository.saveAndFlush(GameEventLog.create(
            fixture.session().getGameSessionId(),
            7,
            2001,
            10,
            "A",
            Map.of("cash", 100000),
            "지원금을 신청했다.",
            LocalDateTime.of(2026, 7, 18, 10, 30)
        ));
        gameplayHistoryRepository.saveAndFlush(
            GameplayHistory.builder()
                .gameId(Math.toIntExact(fixture.session().getGameSessionId()))
                .eventId(920001)
                .tableName("게임주거")
                .columnName("주거형태,보증금,월세,관리비,현재매물ID")
                .targetKey1(String.valueOf(fixture.session().getGameSessionId()))
                .beforeValue("{\"housingType\":\"VILLA\",\"propertyId\":202}")
                .afterValue("{\"housingType\":\"OWNED_APT\",\"propertyId\":301}")
                .summary("자가 아파트를 마련했다.")
                .occurredTurn(12)
                .build()
        );

        mockMvc.perform(get("/api/games/sessions/{sessionId}/ending", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.endingType").value("CLEAR"))
            .andExpect(jsonPath("$.data.title").value("부동산 갑부"))
            .andExpect(jsonPath("$.data.newsHistories[0].newsId").value("NEWS-001"))
            .andExpect(jsonPath("$.data.eventHistories[0].gameEventId").value(2001))
            .andExpect(jsonPath("$.data.housingHistories[0].turnNumber").value(12));

        mockMvc.perform(get("/api/games/sessions/{sessionId}/logs", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.timeline[0].turnNumber").value(1))
            .andExpect(jsonPath("$.data.timeline[1].turnNumber").value(12));
    }

    @DisplayName("압류 종료 세션은 ending 조회에서 foreclosure 타입과 제목을 같은 계약으로 읽어야 한다.")
    @Test
    void endingReadBackForeclosure() throws Exception {
        final SessionFixture fixture = saveSessionFixture(
            "ending-foreclosure-user@example.com",
            12,
            CycleState.of(CyclePhase.RECOVERY, CycleType.CYCLE_RATE_HIKE, 18)
        );
        fixture.session().markEnding(SessionStatus.FORECLOSURE);
        gameSessionRepository.saveAndFlush(fixture.session());
        gameReportRepository.saveAndFlush(GameReport.create(
            fixture.session().getGameSessionId(),
            SessionStatus.FORECLOSURE,
            "압류",
            30_000_000,
            42_000_000,
            "B",
            15_000_000,
            -12_000_000,
            "주거비",
            BigDecimal.valueOf(52.4),
            List.of()
        ));

        mockMvc.perform(get("/api/games/sessions/{sessionId}/ending", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.endingType").value("FORECLOSURE"))
            .andExpect(jsonPath("$.data.title").value("압류"));
    }

    @DisplayName("번아웃 상태 preview는 강제 REST 슬롯을 공개 계약으로 노출해야 한다.")
    @Test
    void submitTurnSlotsShouldExposeForcedRestSlotsForBurnout() throws Exception {
        final SessionFixture fixture = saveSessionFixture(
            "turn-burnout-user@example.com",
            12,
            CycleState.of(CyclePhase.BOOM, CycleType.CYCLE_BOOM, 1)
        );
        saveGameStat(fixture.session(), 70, 85, 82, 50, 50, 12);

        mockMvc.perform(post("/api/games/sessions/{sessionId}/turn/slots", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "slots",
                    List.of(
                        Map.of("slotIndex", 0, "actionType", "REST"),
                        Map.of("slotIndex", 1, "actionType", "STUDY"),
                        Map.of("slotIndex", 2, "actionType", "REST")
                    )
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.slots[0].forcedAction").value(true))
            .andExpect(jsonPath("$.data.slots[1].forcedAction").value(false))
            .andExpect(jsonPath("$.data.slots[2].forcedAction").value(true));
    }

    @DisplayName("변동 수익 행동 preview는 min/max 범위를 공개 계약으로 노출해야 한다. 현재 구현은 RED baseline이다.")
    @Test
    void submitTurnSlotsShouldExposeCashRangeForVariableIncomeActions() throws Exception {
        final SessionFixture fixture = saveSessionFixture(
            "turn-preview-user@example.com",
            12,
            CycleState.of(CyclePhase.BOOM, CycleType.CYCLE_BOOM, 1)
        );
        saveGameStat(fixture.session(), 70, 20, 20, 50, 50, 12);

        mockMvc.perform(post("/api/games/sessions/{sessionId}/turn/slots", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(turnSlotsRequest())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.previewCashMinChange").exists())
            .andExpect(jsonPath("$.data.previewCashMaxChange").exists());
    }

    @DisplayName("턴 커밋 정산 로그는 placeholder 문구를 숨기지 말고 실제 계산 결과만 노출해야 한다. 현재 구현은 RED baseline이다.")
    @Test
    void commitTurnShouldNotPersistPlaceholderSettlementDescriptions() throws Exception {
        final SessionFixture fixture = saveSessionFixture(
            "turn-commit-user@example.com",
            12,
            CycleState.of(CyclePhase.BOOM, CycleType.CYCLE_BOOM, 1)
        );
        saveGameStat(fixture.session(), 70, 20, 20, 50, 50, 12);
        saveGameCareer(fixture.session(), 12, 31_000_000);

        mockMvc.perform(post("/api/games/sessions/{sessionId}/turn/slots", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(turnSlotsRequest())))
            .andExpect(status().isOk());

        final MvcResult commitResult = mockMvc.perform(
                post("/api/games/sessions/{sessionId}/turn/commit", fixture.session().getGameSessionId())
                    .header(AUTHORIZATION, fixture.bearerToken())
            )
            .andExpect(status().isOk())
            .andReturn();

        final JsonNode root = objectMapper.readTree(commitResult.getResponse().getContentAsString());
        final List<String> descriptions = new ArrayList<>();
        root.path("data").path("settlementLog").forEach(item ->
            descriptions.add(item.path("description").asText())
        );

        assertThat(descriptions).doesNotContain("대기한다");
    }

    @DisplayName("턴 커밋과 종료 로그 조회는 급여, 고정지출, 주거비, 카드대금, 대출 상환 결과를 같은 source로 읽는다.")
    @Test
    void commitTurnAndEndingLogsShouldExposeRealSettlementSources() throws Exception {
        final SessionFixture fixture = saveSessionFixture(
            "turn-real-settlement-user@example.com",
            359,
            CycleState.of(CyclePhase.BOOM, CycleType.CYCLE_BOOM, 1),
            DataSourceType.MY_DATA
        );
        saveGameStat(fixture.session(), 70, 20, 20, 50, 50, 359);
        saveGameCareer(fixture.session(), 12, 31_200_000);
        saveUserAssetProfile(fixture.user().getId(), 2_600_000L, 400_000L);
        saveCardSpend(fixture.user().getId(), 80_000L);
        saveCardSpend(fixture.user().getId(), 50_000L);
        saveRentalHousing(fixture.session().getGameSessionId(), 300_000L, 50_000L);
        saveGameLoan(fixture.session().getGameSessionId(), 1_200_000, 25_000, 120);

        mockMvc.perform(post("/api/games/sessions/{sessionId}/turn/slots", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(noCashTurnSlotsRequest())))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/games/sessions/{sessionId}/turn/commit", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.endingStatus").value("TIMEOUT"))
            .andExpect(jsonPath("$.data.updatedAssets.cash").value(3_695_000))
            .andExpect(jsonPath("$.data.updatedAssets.loan").value(1_200_000))
            .andExpect(jsonPath("$.data.updatedAssets.netAssets").value(7_495_000))
            .andExpect(jsonPath("$.data.settlementLog[?(@.cashChange == 2600000)]").isNotEmpty())
            .andExpect(jsonPath("$.data.settlementLog[?(@.cashChange == -400000)]").isNotEmpty())
            .andExpect(jsonPath("$.data.settlementLog[?(@.cashChange == -350000)]").isNotEmpty())
            .andExpect(jsonPath("$.data.settlementLog[?(@.cashChange == -130000)]").isNotEmpty())
            .andExpect(jsonPath("$.data.settlementLog[?(@.cashChange == -25000)]").isNotEmpty());

        mockMvc.perform(get("/api/games/sessions/{sessionId}/logs", fixture.session().getGameSessionId())
                .header(AUTHORIZATION, fixture.bearerToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.timeline[0].cash").value(3_695_000))
            .andExpect(jsonPath("$.data.timeline[0].loanBalance").value(1_200_000))
            .andExpect(jsonPath("$.data.timeline[0].salary").value(2_600_000));
    }

    private SessionFixture saveSessionFixture(
        final String email,
        final int currentTurn,
        final CycleState cycleState
    ) {
        return saveSessionFixture(email, currentTurn, cycleState, DataSourceType.PROFILE);
    }

    private SessionFixture saveSessionFixture(
        final String email,
        final int currentTurn,
        final CycleState cycleState,
        final DataSourceType dataSourceType
    ) {
        final User user = saveUser(email);
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), currentTurn, cycleState, 101L, dataSourceType)
        );
        return new SessionFixture(user, gameSession, bearer(user));
    }

    private User saveUser(final String email) {
        return userRepository.save(User.register(Email.of(email), "tester", "hashed-password"));
    }

    private String bearer(final User user) {
        return "Bearer " + jwtTokenProvider.createAccessToken(user.getId(), user.getEmail().getValue());
    }

    private GameSession createGameSession(
        final Long userId,
        final int currentTurn,
        final CycleState cycleState,
        final Long targetPropertyId
    ) {
        return createGameSession(userId, currentTurn, cycleState, targetPropertyId, DataSourceType.PROFILE);
    }

    private GameSession createGameSession(
        final Long userId,
        final int currentTurn,
        final CycleState cycleState,
        final Long targetPropertyId,
        final DataSourceType dataSourceType
    ) {
        final GameSession gameSession = GameSession.create(
            userId,
            1,
            "윤서",
            CharacterType.FEMALE,
            JobType.STARTUP,
            HousingType.STUDIO,
            "11",
            "11680",
            targetPropertyId,
            dataSourceType
        );
        gameSession.initializeCapital(
            Money.of(2_000_000L),
            Money.of(2_000_000L),
            LocalDate.of(2026, 1, 1),
            cycleState
        );
        gameSession.advanceTurn(
            currentTurn,
            LocalDate.of(2026, 1, 1),
            Money.of(2_000_000L),
            Money.of(2_000_000L),
            cycleState
        );
        return gameSession;
    }

    private void saveGameStat(
        final GameSession gameSession,
        final int health,
        final int fatigue,
        final int stress,
        final int happiness,
        final int knowledge,
        final int currentTurn
    ) {
        gameStatRepository.saveAndFlush(GameStat.create(
            Math.toIntExact(gameSession.getGameSessionId()),
            health,
            fatigue,
            stress,
            happiness,
            knowledge,
            currentTurn
        ));
    }

    private void saveGameCareer(
        final GameSession gameSession,
        final int tenureTurns,
        final int salary
    ) {
        gameCareerRepository.saveAndFlush(GameCareer.builder()
            .gameId(Math.toIntExact(gameSession.getGameSessionId()))
            .jobType(JobType.STARTUP)
            .jobTitle("사원")
            .salary(salary)
            .tenureTurns(tenureTurns)
            .recentStudyCount(0)
            .recentNetworkingCount(0)
            .negotiationPreparationScore(0)
            .lastNegotiatedTurn(0)
            .employmentStatus(EmploymentStatus.EMPLOYED)
            .probationEndTurn(null)
            .rehireAvailableTurn(null)
            .remainingUnemploymentBenefitTurns(0)
            .salaryBeforeResignation(null)
            .build());
    }

    private Map<String, Object> turnSlotsRequest() {
        return Map.of(
            "slots",
            List.of(
                Map.of("slotIndex", 0, "actionType", "STUDY"),
                Map.of("slotIndex", 1, "actionType", "REST"),
                Map.of("slotIndex", 2, "actionType", "SIDE_JOB")
            )
        );
    }

    private Map<String, Object> noCashTurnSlotsRequest() {
        return Map.of(
            "slots",
            List.of(
                Map.of("slotIndex", 0, "actionType", "STUDY"),
                Map.of("slotIndex", 1, "actionType", "REST"),
                Map.of("slotIndex", 2, "actionType", "REST")
            )
        );
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

    private void saveRentalHousing(
        final Long gameSessionId,
        final long monthlyRentAmount,
        final long maintenanceFeeAmount
    ) {
        gameHousingRepository.saveAndFlush(
            io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousing.create(
                gameSessionId,
                HousingType.STUDIO,
                Money.of(5_000_000L),
                Money.of(monthlyRentAmount),
                Money.of(maintenanceFeeAmount),
                null
            )
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

    private record SessionFixture(
        User user,
        GameSession session,
        String bearerToken
    ) {
    }
}
