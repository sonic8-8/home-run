package io.ssafy.p.j14c103.homerun.api.service.game.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.game.session.request.CreateGameSessionServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.session.response.CreateGameSessionResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
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
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CreateGameSessionServiceTest {

    @Autowired
    private CreateGameSessionService createGameSessionService;

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

    @AfterEach
    void tearDown() {
        gameSessionRepository.deleteAllInBatch();
        realEstatePropertyRepository.deleteAllInBatch();
        housingDistrictRepository.deleteAllInBatch();
        housingRegionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
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
        final CreateGameSessionResponse response = createGameSessionService.create(user.getId(), request);

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
        assertThatThrownBy(() -> createGameSessionService.create(user.getId(), request))
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
        assertThatThrownBy(() -> createGameSessionService.create(user.getId(), request))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.HOUSING_PROPERTY_NOT_FOUND);
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
}
