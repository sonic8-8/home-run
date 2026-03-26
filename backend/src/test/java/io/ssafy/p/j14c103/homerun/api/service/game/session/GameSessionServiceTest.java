package io.ssafy.p.j14c103.homerun.api.service.game.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.then;

import io.ssafy.p.j14c103.homerun.api.service.game.session.request.CreateGameSessionServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.session.response.CreateGameSessionResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.session.response.GameSessionDetailResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.session.response.GameSessionListResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class GameSessionServiceTest {

    @Autowired
    private GameSessionService gameSessionService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HousingRegionRepository housingRegionRepository;

    @Autowired
    private HousingDistrictRepository housingDistrictRepository;

    @Autowired
    private RealEstatePropertyRepository realEstatePropertyRepository;

    @MockitoBean
    private GameSessionCleanupService gameSessionCleanupService;

    @AfterEach
    void tearDown() {
        gameSessionRepository.deleteAllInBatch();
        realEstatePropertyRepository.deleteAllInBatch();
        housingDistrictRepository.deleteAllInBatch();
        housingRegionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
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

    @DisplayName("세션 생성은 슬롯, 목표 매물, 데이터 소스 정보를 저장한다.")
    @Test
    void create() {
        // given
        final User user = saveUser("create-user@example.com");
        final RealEstateProperty property = saveTargetProperty("11", "11680");

        final CreateGameSessionServiceRequest request = CreateGameSessionServiceRequest.of(
            1,
            CharacterType.FEMALE,
            "승환",
            JobType.SMALL_BIZ,
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
        assertThat(found.getJobType()).isEqualTo(JobType.SMALL_BIZ);
        assertThat(found.getHousingType()).isEqualTo(HousingType.STUDIO);
        assertThat(found.getRegionCode()).isEqualTo("11");
        assertThat(found.getDistrictCode()).isEqualTo("11680");
        assertThat(found.getTargetPropertyId()).isEqualTo(property.getPropertyId());
        assertThat(found.getDataSourceType()).isEqualTo(DataSourceType.MY_DATA);
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
