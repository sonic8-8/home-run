package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.world.response.DistrictsProviderResponse;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingDistrictRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingRegion;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingRegionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocumentRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class WorldDistrictProviderServiceTest {

    @Autowired
    private WorldDistrictProviderService worldDistrictProviderService;

    @Autowired
    private WorldHousingSeedService worldHousingSeedService;

    @Autowired
    private RealEstatePropertyRepository realEstatePropertyRepository;

    @Autowired
    private RealEstateDocumentRepository realEstateDocumentRepository;

    @Autowired
    private HousingDistrictRepository housingDistrictRepository;

    @Autowired
    private HousingRegionRepository housingRegionRepository;

    @AfterEach
    void tearDown() {
        realEstateDocumentRepository.deleteAllInBatch();
        realEstatePropertyRepository.deleteAllInBatch();
        housingDistrictRepository.deleteAllInBatch();
        housingRegionRepository.deleteAllInBatch();
    }

    @DisplayName("정상 regionCode를 입력하면 해당 지역의 district 목록만 반환한다")
    @Test
    void getDistricts() {
        // given
        worldHousingSeedService.seed();

        // when
        final DistrictsProviderResponse response = worldDistrictProviderService.getDistricts("11");

        // then
        assertThat(response.getRegionCode()).isEqualTo("11");
        assertThat(response.getDistricts())
            .extracting(DistrictsProviderResponse.DistrictItem::getDistrictCode)
            .containsExactly("11215", "11440", "11680", "11710");
        assertThat(response.getDistricts())
            .extracting(DistrictsProviderResponse.DistrictItem::getName)
            .containsExactly("광진구", "마포구", "강남구", "송파구");
    }

    @DisplayName("다른 지역 district는 섞이지 않고 해당 regionCode의 목록만 반환한다")
    @Test
    void getDistrictsWithoutOtherRegionMixing() {
        // given
        worldHousingSeedService.seed();

        // when
        final DistrictsProviderResponse response = worldDistrictProviderService.getDistricts("24");

        // then
        assertThat(response.getRegionCode()).isEqualTo("24");
        assertThat(response.getDistricts())
            .extracting(DistrictsProviderResponse.DistrictItem::getDistrictCode)
            .containsExactly("24170");
        assertThat(response.getDistricts())
            .extracting(DistrictsProviderResponse.DistrictItem::getName)
            .containsExactly("북구");
    }

    @DisplayName("존재하지 않는 regionCode를 입력하면 입력값 예외가 발생한다")
    @Test
    void getDistrictsWithInvalidRegionCode() {
        // when & then
        assertThatThrownBy(() -> worldDistrictProviderService.getDistricts("99"))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @DisplayName("유효한 regionCode라도 district 데이터가 비어 있으면 빈 배열 계약을 반환한다")
    @Test
    void getDistrictsWithEmptyDistricts() {
        // given
        housingRegionRepository.save(HousingRegion.create("11", "서울특별시"));

        // when
        final DistrictsProviderResponse response = worldDistrictProviderService.getDistricts("11");

        // then
        assertThat(response.getRegionCode()).isEqualTo("11");
        assertThat(response.getDistricts()).isEmpty();
    }
}
