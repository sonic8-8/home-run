package io.ssafy.p.j14c103.homerun.api.service.game.start;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import io.ssafy.p.j14c103.homerun.api.service.game.start.response.DistrictListResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.start.response.RegionListResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.start.response.TargetPropertyListResponse;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
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
class GameStartLocationServiceTest {

    @Autowired
    private GameStartLocationService gameStartLocationService;

    @Autowired
    private HousingRegionRepository housingRegionRepository;

    @Autowired
    private HousingDistrictRepository housingDistrictRepository;

    @Autowired
    private RealEstatePropertyRepository realEstatePropertyRepository;

    @AfterEach
    void tearDown() {
        realEstatePropertyRepository.deleteAllInBatch();
        housingDistrictRepository.deleteAllInBatch();
        housingRegionRepository.deleteAllInBatch();
    }

    @DisplayName("지역 목록을 regionCode 오름차순으로 반환한다.")
    @Test
    void getRegions() {
        // given
        housingRegionRepository.save(HousingRegion.create("24", "광주광역시"));
        housingRegionRepository.save(HousingRegion.create("11", "서울특별시"));

        // when
        final RegionListResponse response = gameStartLocationService.getRegions();

        // then
        assertThat(response.regions())
            .extracting(
                RegionListResponse.RegionResponse::regionCode,
                RegionListResponse.RegionResponse::name
            )
            .containsExactly(
                tuple("11", "서울특별시"),
                tuple("24", "광주광역시")
            );
    }

    @DisplayName("지역별 구 목록을 districtCode 오름차순으로 반환한다.")
    @Test
    void getDistricts() {
        // given
        housingRegionRepository.save(HousingRegion.create("11", "서울특별시"));
        housingRegionRepository.save(HousingRegion.create("24", "광주광역시"));
        housingDistrictRepository.save(HousingDistrict.create("11710", "11", "송파구", "1171000000"));
        housingDistrictRepository.save(HousingDistrict.create("11680", "11", "강남구", "1168000000"));
        housingDistrictRepository.save(HousingDistrict.create("24110", "24", "동구", "2411000000"));

        // when
        final DistrictListResponse response = gameStartLocationService.getDistricts("11");

        // then
        assertThat(response.regionCode()).isEqualTo("11");
        assertThat(response.districts())
            .extracting(
                DistrictListResponse.DistrictResponse::districtCode,
                DistrictListResponse.DistrictResponse::name
            )
            .containsExactly(
                tuple("11680", "강남구"),
                tuple("11710", "송파구")
            );
    }

    @DisplayName("목표 매물 목록을 propertyId 오름차순으로 반환한다.")
    @Test
    void getTargetProperties() {
        // given
        housingRegionRepository.save(HousingRegion.create("11", "서울특별시"));
        housingDistrictRepository.save(HousingDistrict.create("11710", "11", "송파구", "1171000000"));

        final List<RealEstateProperty> properties = realEstatePropertyRepository.saveAll(List.of(
            RealEstateProperty.create(
                "provider-2",
                "헬리오시티",
                "서울특별시 송파구 가락동 913",
                "11",
                "11710",
                Money.of(1_550_000_000L),
                BigDecimal.valueOf(37.4979512),
                BigDecimal.valueOf(127.1127134),
                HousingType.OWNED_APT,
                List.of()
            ),
            RealEstateProperty.create(
                "provider-1",
                "잠실엘스",
                "서울특별시 송파구 잠실동 35",
                "11",
                "11710",
                Money.of(2_300_000_000L),
                BigDecimal.valueOf(37.5133012),
                BigDecimal.valueOf(127.1029384),
                HousingType.OWNED_APT,
                List.of()
            )
        ));

        // when
        final TargetPropertyListResponse response = gameStartLocationService.getTargetProperties("11", "11710");

        // then
        assertThat(response.properties())
            .extracting(
                TargetPropertyListResponse.TargetPropertyResponse::propertyId,
                TargetPropertyListResponse.TargetPropertyResponse::name,
                TargetPropertyListResponse.TargetPropertyResponse::recentPrice
            )
            .containsExactly(
                tuple(properties.get(0).getPropertyId(), "헬리오시티", 1_550_000_000L),
                tuple(properties.get(1).getPropertyId(), "잠실엘스", 2_300_000_000L)
            );
    }

    @DisplayName("유효한 지역과 구에 매물이 없으면 빈 목록을 반환한다.")
    @Test
    void getTargetPropertiesWithEmptyList() {
        // given
        housingRegionRepository.save(HousingRegion.create("11", "서울특별시"));
        housingDistrictRepository.save(HousingDistrict.create("11710", "11", "송파구", "1171000000"));

        // when
        final TargetPropertyListResponse response = gameStartLocationService.getTargetProperties("11", "11710");

        // then
        assertThat(response.properties()).isEmpty();
    }

    @DisplayName("존재하지 않는 지역 코드는 HOUSING_REGION_NOT_FOUND 예외가 발생한다.")
    @Test
    void getDistrictsWithUnknownRegion() {
        // when & then
        assertThatThrownBy(() -> gameStartLocationService.getDistricts("99"))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.HOUSING_REGION_NOT_FOUND);
    }

    @DisplayName("지역과 맞지 않는 구 코드는 HOUSING_DISTRICT_NOT_FOUND 예외가 발생한다.")
    @Test
    void getTargetPropertiesWithMismatchedDistrict() {
        // given
        housingRegionRepository.save(HousingRegion.create("11", "서울특별시"));
        housingRegionRepository.save(HousingRegion.create("24", "광주광역시"));
        housingDistrictRepository.save(HousingDistrict.create("24110", "24", "동구", "2411000000"));

        // when & then
        assertThatThrownBy(() -> gameStartLocationService.getTargetProperties("11", "24110"))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.HOUSING_DISTRICT_NOT_FOUND);
    }
}
