package io.ssafy.p.j14c103.homerun.api.service.game.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.then;

import io.ssafy.p.j14c103.homerun.api.service.game.session.request.CreateGameSessionServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.session.response.CreateGameSessionResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.session.response.GameSessionDetailResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.session.response.GameSessionListResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.stock.StockTradingService;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.CommitTurnService;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.SubmitTurnSlotsService;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.request.SubmitTurnSlotsServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.response.CommitTurnResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.response.TurnPreviewResponse;
import io.ssafy.p.j14c103.homerun.domain.account.AccountType;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccount;
import io.ssafy.p.j14c103.homerun.domain.account.UserAccountRepository;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterSeedPolicy;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.GameStatRepository;
import io.ssafy.p.j14c103.homerun.domain.character.SeedType;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareerRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.financial.FinancialProductSourceType;
import io.ssafy.p.j14c103.homerun.domain.financial.FinancialProductType;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialProduct;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialProductRepository;
import io.ssafy.p.j14c103.homerun.domain.financial.UserFinancialSummaryRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockMarket;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockMarketRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.GameStockMarketStateRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.ActionType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.TurnDraft;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.TurnDraftRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.spending.SpendingCategory;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetCardSpend;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetCardSpendRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetOtherIncome;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetOtherIncomeRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetProfile;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetProfileRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingDistrict;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingDistrictRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingRegion;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingRegionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateProperty;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@Import(GameSessionServiceTest.TurnDraftRepositoryTestConfig.class)
class GameSessionServiceTest extends IntegrationTestSupport {

    @Autowired
    private GameSessionService gameSessionService;

    @Autowired
    private SubmitTurnSlotsService submitTurnSlotsService;

    @Autowired
    private CommitTurnService commitTurnService;

    @Autowired
    private StockTradingService stockTradingService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private GameStatRepository gameStatRepository;

    @Autowired
    private GameCareerRepository gameCareerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HousingRegionRepository housingRegionRepository;

    @Autowired
    private HousingDistrictRepository housingDistrictRepository;

    @Autowired
    private RealEstatePropertyRepository realEstatePropertyRepository;

    @Autowired
    private UserAssetProfileRepository userAssetProfileRepository;

    @Autowired
    private UserAssetOtherIncomeRepository userAssetOtherIncomeRepository;

    @Autowired
    private UserAssetCardSpendRepository userAssetCardSpendRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserFinancialProductRepository userFinancialProductRepository;

    @Autowired
    private UserFinancialSummaryRepository userFinancialSummaryRepository;

    @Autowired
    private GameStockMarketStateRepository gameStockMarketStateRepository;

    @Autowired
    private StockMarketRepository stockMarketRepository;

    @Autowired
    private TurnDraftRepositoryTestSupport turnDraftRepositoryTestSupport;

    @MockitoBean
    private GameSessionCleanupService gameSessionCleanupService;

    @AfterEach
    void tearDown() {
        turnDraftRepositoryTestSupport.clear();
        userFinancialSummaryRepository.deleteAllInBatch();
        userFinancialProductRepository.deleteAllInBatch();
        userAccountRepository.deleteAllInBatch();
        userAssetCardSpendRepository.deleteAllInBatch();
        userAssetOtherIncomeRepository.deleteAllInBatch();
        userAssetProfileRepository.deleteAllInBatch();
        gameStockMarketStateRepository.deleteAllInBatch();
        gameCareerRepository.deleteAllInBatch();
        gameStatRepository.deleteAllInBatch();
        gameSessionRepository.deleteAllInBatch();
        stockMarketRepository.deleteAllInBatch();
        realEstatePropertyRepository.deleteAllInBatch();
        housingDistrictRepository.deleteAllInBatch();
        housingRegionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @TestConfiguration
    static class TurnDraftRepositoryTestConfig {

        @Bean
        @Primary
        TurnDraftRepositoryTestSupport turnDraftRepository() {
            return new TurnDraftRepositoryTestSupport();
        }
    }

    static class TurnDraftRepositoryTestSupport implements TurnDraftRepository {

        private final Map<Long, TurnDraft> store = new ConcurrentHashMap<>();

        @Override
        public void save(final TurnDraft turnDraft) {
            validateSessionId(turnDraft.getSessionId());
            store.put(turnDraft.getSessionId(), turnDraft);
        }

        @Override
        public Optional<TurnDraft> findBySessionId(final Long sessionId) {
            validateSessionId(sessionId);
            return Optional.ofNullable(store.get(sessionId));
        }

        @Override
        public void deleteBySessionId(final Long sessionId) {
            validateSessionId(sessionId);
            store.remove(sessionId);
        }

        void clear() {
            store.clear();
        }

        private void validateSessionId(final Long sessionId) {
            if (sessionId == null || sessionId <= 0L) {
                throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }
    }

    @DisplayName("세션 목록 조회는 빈 슬롯을 포함한 3개 저장 슬롯을 반환한다.")
    @Test
    void getSessions() {
        // given
        final User user = saveUser("list-user@example.com");
        gameSessionRepository.saveAndFlush(createGameSession(user.getId(), 3, "민지", JobType.MID_BIZ, 303L));
        final GameSession first = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 1, "윤서", JobType.STARTUP, 101L)
        );
        first.initializeCapital(
            Money.of(13_000_000L),
            Money.of(15_000_000L),
            LocalDate.of(2026, 1, 1),
            CyclePhase.RECOVERY
        );
        gameSessionRepository.saveAndFlush(first);

        // when
        final GameSessionListResponse response = gameSessionService.getSessions(user.getId());

        // then
        assertThat(response.getSessions())
            .extracting(
                GameSessionListResponse.SessionSummaryResponse::getSlotNumber,
                GameSessionListResponse.SessionSummaryResponse::getStatus,
                GameSessionListResponse.SessionSummaryResponse::getCharacterName
            )
            .containsExactly(
                tuple(1, "IN_PROGRESS", "윤서"),
                tuple(2, "EMPTY", null),
                tuple(3, "IN_PROGRESS", "민지")
            );
        assertThat(response.getSessions().get(0).getTotalAssets()).isEqualTo(15_000_000L);
        assertThat(response.getSessions().get(1).getSessionId()).isNull();
    }

    @DisplayName("존재하지 않는 사용자로 세션 목록을 조회하면 USER_NOT_FOUND가 발생한다.")
    @Test
    void getSessionsWithUnknownUser() {
        // when & then
        assertThatThrownBy(() -> gameSessionService.getSessions(99L))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @DisplayName("PROFILE 세션 생성은 게임 스탯과 커리어 초기 상태를 함께 생성한다.")
    @Test
    void createWithProfileBootstrapsCharacterState() {
        // given
        final User user = saveUser("create-profile@example.com");
        final RealEstateProperty property = saveTargetProperty("11", "11680");
        seedStockMarkets();
        final CreateGameSessionServiceRequest request = CreateGameSessionServiceRequest.of(
            1,
            CharacterType.FEMALE,
            "하린",
            JobType.MID_BIZ,
            "11",
            "11680",
            property.getPropertyId(),
            false
        );

        // when
        final CreateGameSessionResponse response = gameSessionService.create(user.getId(), request);

        // then
        assertThat(response.getSessionId()).isNotNull();
        assertCharacterBootstrap(
            response.getSessionId(),
            CharacterType.FEMALE,
            JobType.MID_BIZ,
            SeedType.PROFILE
        );
        assertThat(gameStockMarketStateRepository.findAllByGameSessionId(response.getSessionId()))
            .isNotEmpty();
        assertThat(stockTradingService.getMarket(response.getSessionId()).getStocks())
            .isNotEmpty();
    }

    @DisplayName("PROFILE 세션 생성 직후 턴 슬롯 제출과 턴 커밋이 가능하다.")
    @Test
    void createWithProfileSupportsImmediateTurnFlow() {
        // given
        final User user = saveUser("create-profile-turn-flow@example.com");
        final RealEstateProperty property = saveTargetProperty("11", "11680");
        seedStockMarkets();
        final CreateGameSessionServiceRequest request = CreateGameSessionServiceRequest.of(
            1,
            CharacterType.FEMALE,
            "하린",
            JobType.MID_BIZ,
            "11",
            "11680",
            property.getPropertyId(),
            false
        );
        final CreateGameSessionResponse response = gameSessionService.create(user.getId(), request);

        // when
        final TurnPreviewResponse previewResponse = submitTurnSlotsService.submitTurnSlots(
            user.getId(),
            response.getSessionId(),
            createRestTurnSlotsRequest()
        );
        final CommitTurnResponse commitTurnResponse = commitTurnService.commitTurn(
            user.getId(),
            response.getSessionId()
        );

        // then
        assertThat(previewResponse.getSlots()).hasSize(3);
        assertThat(commitTurnResponse.getTurnNumber()).isEqualTo(0);
        assertThat(commitTurnResponse.getSettlementLog()).isNotEmpty();
    }

    @DisplayName("MY_DATA 세션 생성은 온보딩 자산연동의 직업과 자본 상태로 초기화한다.")
    @Test
    void createWithMyDataUsesOnboardingProfile() {
        // given
        final User user = saveUser("create-user@example.com");
        final RealEstateProperty property = saveTargetProperty("11", "11680");
        seedOnboardingAssetLink(user.getId(), JobType.STARTUP);

        final CreateGameSessionServiceRequest request = CreateGameSessionServiceRequest.of(
            1,
            CharacterType.FEMALE,
            "승환",
            null,
            "11",
            "11680",
            property.getPropertyId(),
            true
        );

        // when
        final CreateGameSessionResponse response = gameSessionService.create(user.getId(), request);

        // then
        assertThat(response.getSessionId()).isNotNull();
        assertThat(response.getSlotNumber()).isEqualTo(1);
        assertThat(response.getDataSourceType()).isEqualTo(DataSourceType.MY_DATA);

        final GameSession found = gameSessionRepository.findById(response.getSessionId()).orElseThrow();
        assertThat(found.getUserId()).isEqualTo(user.getId());
        assertThat(found.getCharacterName()).isEqualTo("승환");
        assertThat(found.getCharacterType()).isEqualTo(CharacterType.FEMALE);
        assertThat(found.getJobType()).isEqualTo(JobType.STARTUP);
        assertThat(found.getHousingType()).isEqualTo(HousingType.STUDIO);
        assertThat(found.getRegionCode()).isEqualTo("11");
        assertThat(found.getDistrictCode()).isEqualTo("11680");
        assertThat(found.getTargetPropertyId()).isEqualTo(property.getPropertyId());
        assertThat(found.getDataSourceType()).isEqualTo(DataSourceType.MY_DATA);
        assertThat(found.getCashBalance()).isEqualTo(Money.of(3_000_000L));
        assertThat(found.getTotalAssets()).isEqualTo(Money.of(10_000_000L));
        assertThat(found.getNetWorth()).isEqualTo(Money.of(5_500_000L));
        assertThat(found.getCurrentDate()).isEqualTo(LocalDate.now());
        assertThat(found.getCyclePhase()).isEqualTo(CyclePhase.RECOVERY);
        assertCharacterBootstrap(
            response.getSessionId(),
            CharacterType.FEMALE,
            JobType.STARTUP,
            SeedType.MY_DATA
        );
    }

    @DisplayName("MY_DATA 세션 생성 시 자산연동 정보가 없으면 USER_ASSET_LINK_REQUIRED가 발생한다.")
    @Test
    void createWithMyDataWithoutAssetLink() {
        // given
        final User user = saveUser("create-without-asset-link@example.com");
        final RealEstateProperty property = saveTargetProperty("11", "11680");

        final CreateGameSessionServiceRequest request = CreateGameSessionServiceRequest.of(
            1,
            CharacterType.FEMALE,
            "승환",
            null,
            "11",
            "11680",
            property.getPropertyId(),
            true
        );

        // when & then
        assertThatThrownBy(() -> gameSessionService.create(user.getId(), request))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.USER_ASSET_LINK_REQUIRED);
    }

    @DisplayName("이미 사용 중인 슬롯으로 세션 생성 시 GAME_SLOT_CONFLICT가 발생한다.")
    @Test
    void createWithOccupiedSlot() {
        // given
        final User user = saveUser("create-conflict@example.com");
        gameSessionRepository.saveAndFlush(GameSession.create(
            user.getId(),
            1,
            "윤서",
            CharacterType.FEMALE,
            JobType.STARTUP,
            HousingType.STUDIO,
            "11",
            "11680",
            101L,
            DataSourceType.PROFILE
        ));

        final CreateGameSessionServiceRequest request = CreateGameSessionServiceRequest.of(
            1,
            CharacterType.MALE,
            "도윤",
            JobType.LARGE_BIZ,
            "11",
            "11680",
            202L,
            false
        );

        // when & then
        assertThatThrownBy(() -> gameSessionService.create(user.getId(), request))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SLOT_CONFLICT);
    }

    @DisplayName("존재하지 않는 목표 매물로 세션 생성 시 HOUSING_PROPERTY_NOT_FOUND가 발생한다.")
    @Test
    void createWithUnknownTargetProperty() {
        // given
        final User user = saveUser("create-property@example.com");
        housingRegionRepository.save(HousingRegion.create("11", "서울특별시"));
        housingDistrictRepository.save(HousingDistrict.create("11680", "11", "강남구", "1168000000"));

        final CreateGameSessionServiceRequest request = CreateGameSessionServiceRequest.of(
            1,
            CharacterType.FEMALE,
            "승환",
            JobType.SMALL_BIZ,
            "11",
            "11680",
            999L,
            true
        );

        // when & then
        assertThatThrownBy(() -> gameSessionService.create(user.getId(), request))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.HOUSING_PROPERTY_NOT_FOUND);
    }

    @DisplayName("세션 상세 조회는 세션 스냅샷을 반환한다.")
    @Test
    void getSessionDetail() {
        // given
        final User user = saveUser("detail-user@example.com");
        final GameSession gameSession = GameSession.create(
            user.getId(),
            2,
            "세준",
            CharacterType.MALE,
            JobType.SMALL_BIZ,
            HousingType.STUDIO,
            "11",
            "11680",
            404L,
            DataSourceType.MY_DATA
        );
        gameSession.initializeCapital(
            Money.of(13_000_000L),
            Money.of(14_500_000L),
            LocalDate.of(2026, 1, 1),
            CyclePhase.RECOVERY
        );
        gameSession.advanceTurn(
            1,
            LocalDate.of(2026, 2, 1),
            Money.of(15_000_000L),
            Money.of(16_000_000L),
            CyclePhase.BOOM
        );
        final GameSession saved = gameSessionRepository.saveAndFlush(gameSession);

        // when
        final GameSessionDetailResponse response =
            gameSessionService.getSessionDetail(user.getId(), saved.getGameSessionId());

        // then
        assertThat(response.getSessionId()).isEqualTo(saved.getGameSessionId());
        assertThat(response.getSlotNumber()).isEqualTo(2);
        assertThat(response.getCharacterName()).isEqualTo("세준");
        assertThat(response.getCurrentTurn()).isEqualTo(1);
        assertThat(response.getCurrentDate()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(response.getCyclePhase()).isEqualTo(CyclePhase.BOOM);
        assertThat(response.getCashBalance()).isEqualTo(15_000_000L);
        assertThat(response.getNetWorth()).isEqualTo(16_000_000L);
    }

    @DisplayName("다른 사용자의 세션 상세 조회는 GAME_SESSION_FORBIDDEN이 발생한다.")
    @Test
    void getOtherUsersSessionDetail() {
        // given
        final User requester = saveUser("detail-requester@example.com");
        final User owner = saveUser("detail-owner@example.com");
        final GameSession saved = gameSessionRepository.saveAndFlush(GameSession.create(
            owner.getId(),
            1,
            "타인세션",
            CharacterType.MALE,
            JobType.LARGE_BIZ,
            HousingType.OWNED_APT,
            "11",
            "11710",
            202L,
            DataSourceType.PROFILE
        ));

        // when & then
        assertThatThrownBy(() ->
            gameSessionService.getSessionDetail(requester.getId(), saved.getGameSessionId())
        )
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_FORBIDDEN);
    }

    @DisplayName("존재하지 않는 세션 상세 조회는 GAME_SESSION_NOT_FOUND가 발생한다.")
    @Test
    void getUnknownSessionDetail() {
        // given
        final User requester = saveUser("detail-not-found@example.com");

        // when & then
        assertThatThrownBy(() -> gameSessionService.getSessionDetail(requester.getId(), 999L))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_NOT_FOUND);
    }

    @DisplayName("세션 삭제는 cleanup 호출 후 세션을 제거한다.")
    @Test
    void delete() {
        // given
        final User user = saveUser("delete-user@example.com");
        final GameSession saved = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 1, "윤서", JobType.STARTUP, 101L)
        );

        // when
        gameSessionService.delete(user.getId(), saved.getGameSessionId());

        // then
        assertThat(gameSessionRepository.findById(saved.getGameSessionId())).isEmpty();
        then(gameSessionCleanupService).should().deleteAllByGameSessionId(saved.getGameSessionId());
    }

    @DisplayName("세션 삭제는 커밋 충돌을 피하기 위해 row lock으로 세션을 조회한다.")
    @Test
    void deleteUsesRowLock() {
        // given
        final User user = saveUser("delete-lock-user@example.com");
        final GameSession saved = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 1, "락삭제", JobType.STARTUP, 101L)
        );

        // when
        gameSessionService.delete(user.getId(), saved.getGameSessionId());

        // then
        assertThat(Mockito.mockingDetails(gameSessionRepository).getInvocations())
            .extracting(invocation -> invocation.getMethod().getName())
            .contains("findByIdForUpdate");
        then(gameSessionCleanupService).should().deleteAllByGameSessionId(saved.getGameSessionId());
    }

    @DisplayName("다른 사용자의 세션 삭제는 GAME_SESSION_FORBIDDEN이 발생한다.")
    @Test
    void deleteOtherUsersSession() {
        // given
        final User requester = saveUser("delete-requester@example.com");
        final User owner = saveUser("delete-owner@example.com");
        final GameSession saved = gameSessionRepository.saveAndFlush(
            createGameSession(owner.getId(), 1, "윤서", JobType.STARTUP, 101L)
        );

        // when & then
        assertThatThrownBy(() ->
            gameSessionService.delete(requester.getId(), saved.getGameSessionId())
        )
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_FORBIDDEN);
        then(gameSessionCleanupService).shouldHaveNoInteractions();
    }

    @DisplayName("존재하지 않는 세션 삭제는 GAME_SESSION_NOT_FOUND가 발생한다.")
    @Test
    void deleteUnknownSession() {
        // given
        final User requester = saveUser("delete-not-found@example.com");

        // when & then
        assertThatThrownBy(() -> gameSessionService.delete(requester.getId(), 999L))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_NOT_FOUND);
        then(gameSessionCleanupService).shouldHaveNoInteractions();
    }

    private User saveUser(final String email) {
        return userRepository.save(User.register(Email.of(email), "tester", "hashed-password"));
    }

    private RealEstateProperty saveTargetProperty(final String regionCode, final String districtCode) {
        housingRegionRepository.save(HousingRegion.create(regionCode, "서울특별시"));
        housingDistrictRepository.save(HousingDistrict.create(districtCode, regionCode, "강남구", "1168000000"));

        return realEstatePropertyRepository.saveAndFlush(RealEstateProperty.create(
            "provider-" + regionCode + districtCode,
            "테스트 매물",
            "서울특별시 강남구",
            regionCode,
            districtCode,
            Money.of(375_000_000L),
            BigDecimal.valueOf(37.5172),
            BigDecimal.valueOf(127.0473),
            HousingType.STUDIO,
            List.of()
        ));
    }

    private void seedOnboardingAssetLink(final Long userId, final JobType jobType) {
        userAssetProfileRepository.save(UserAssetProfile.create(
            userId,
            3_000_000,
            25,
            2_500_000,
            1_200_000,
            jobType
        ));
        userAssetOtherIncomeRepository.save(UserAssetOtherIncome.create(userId, "부업", 200_000));
        userAssetOtherIncomeRepository.save(UserAssetOtherIncome.create(userId, "용돈", 100_000));
        userAssetCardSpendRepository.save(UserAssetCardSpend.create(userId, SpendingCategory.LIVING, 300_000));
        userAssetCardSpendRepository.save(UserAssetCardSpend.create(userId, SpendingCategory.TRANSPORT, 50_000));
        userAccountRepository.save(UserAccount.create(
            userId,
            AccountType.MAIN,
            "001",
            "한국은행",
            "1111111111111111",
            3_000_000
        ));
        userAccountRepository.save(UserAccount.create(
            userId,
            AccountType.SEEDMONEY,
            "001",
            "한국은행",
            "2222222222222222",
            0
        ));
        userFinancialProductRepository.save(UserFinancialProduct.create(
            userId,
            FinancialProductType.SAVING_DEPOSIT,
            "사용자 입력",
            "청약저축",
            7_000_000,
            LocalDateTime.now().minusMonths(6),
            FinancialProductSourceType.ASSET_LINK
        ));
        userFinancialProductRepository.save(UserFinancialProduct.create(
            userId,
            FinancialProductType.LOAN,
            "사용자 입력",
            "신용대출",
            4_500_000,
            LocalDateTime.now().minusMonths(3),
            FinancialProductSourceType.ASSET_LINK
        ));
    }

    private void seedStockMarkets() {
        stockMarketRepository.save(StockMarket.create(
            "BIO",
            "바이오주",
            "068270",
            "바이오",
            100_000,
            new BigDecimal("0.15")
        ));
        stockMarketRepository.save(StockMarket.create(
            "SEMI",
            "반도체주",
            "005930",
            "반도체",
            72_000,
            new BigDecimal("0.10")
        ));
    }

    private void assertCharacterBootstrap(
        final Long sessionId,
        final CharacterType characterType,
        final JobType jobType,
        final SeedType seedType
    ) {
        final CharacterSeedPolicy.CharacterSeedPlan seedPlan =
            new CharacterSeedPolicy().calculate(characterType, jobType, seedType);

        final GameStat gameStat = gameStatRepository.findById(sessionId.intValue()).orElseThrow();
        assertThat(gameStat.getHealth()).isEqualTo(seedPlan.stat().health());
        assertThat(gameStat.getFatigue()).isEqualTo(seedPlan.stat().fatigue());
        assertThat(gameStat.getStress()).isEqualTo(seedPlan.stat().stress());
        assertThat(gameStat.getKnowledge()).isEqualTo(seedPlan.stat().knowledge());
        assertThat(gameStat.getHappiness()).isEqualTo(seedPlan.stat().happiness());

        final GameCareer gameCareer = gameCareerRepository.findById(sessionId.intValue()).orElseThrow();
        assertThat(gameCareer.getJobType()).isEqualTo(seedPlan.career().jobType());
        assertThat(gameCareer.getJobTitle()).isEqualTo(seedPlan.career().jobTitle());
        assertThat(gameCareer.getSalary()).isEqualTo(seedPlan.career().annualSalary());
        assertThat(gameCareer.getTenureTurns()).isEqualTo(seedPlan.career().tenureTurns());
        assertThat(gameCareer.getRecentStudyCount()).isEqualTo(seedPlan.career().recentStudyCount());
        assertThat(gameCareer.getRecentNetworkingCount())
            .isEqualTo(seedPlan.career().recentNetworkingCount());
        assertThat(gameCareer.getNegotiationPreparationScore())
            .isEqualTo(seedPlan.career().negotiationPreparationScore());
        assertThat(gameCareer.getLastNegotiatedTurn())
            .isEqualTo(seedPlan.career().lastNegotiatedTurn());
        assertThat(gameCareer.getEmploymentStatus()).isEqualTo(seedPlan.career().employmentStatus());
        assertThat(gameCareer.getProbationEndTurn()).isEqualTo(seedPlan.career().probationEndTurn());
        assertThat(gameCareer.getRehireAvailableTurn()).isEqualTo(seedPlan.career().rehireAvailableTurn());
        assertThat(gameCareer.getRemainingUnemploymentBenefitTurns())
            .isEqualTo(seedPlan.career().remainingUnemploymentBenefitTurns());
        assertThat(gameCareer.getSalaryBeforeResignation())
            .isEqualTo(seedPlan.career().salaryBeforeResignation());
    }

    private SubmitTurnSlotsServiceRequest createRestTurnSlotsRequest() {
        return SubmitTurnSlotsServiceRequest.of(
            List.of(
                SubmitTurnSlotsServiceRequest.TurnSlotRequest.of(0, ActionType.REST),
                SubmitTurnSlotsServiceRequest.TurnSlotRequest.of(1, ActionType.REST),
                SubmitTurnSlotsServiceRequest.TurnSlotRequest.of(2, ActionType.REST)
            )
        );
    }

    private GameSession createGameSession(
        final Long userId,
        final Integer slotNumber,
        final String characterName,
        final JobType jobType,
        final Long targetPropertyId
    ) {
        return GameSession.create(
            userId,
            slotNumber,
            characterName,
            CharacterType.FEMALE,
            jobType,
            HousingType.STUDIO,
            "11",
            "11680",
            targetPropertyId,
            DataSourceType.PROFILE
        );
    }
}
