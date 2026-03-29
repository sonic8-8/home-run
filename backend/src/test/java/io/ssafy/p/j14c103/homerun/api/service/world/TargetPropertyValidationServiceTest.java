package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.world.response.TargetPropertyValidationResponse;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingDistrictRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingRegionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocumentRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateProperty;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TargetPropertyValidationServiceTest extends IntegrationTestSupport {

    @Autowired
    private TargetPropertyValidationService targetPropertyValidationService;

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

    @DisplayName("정상 targetPropertyId면 목표 가격 스냅샷과 주거 타입을 반환한다")
    @Test
    void validateTargetProperty() {
        // given
        worldHousingSeedService.seed();
        final RealEstateProperty property = realEstatePropertyRepository.findByProviderId("PROP-HN-001")
            .orElseThrow(() -> new AssertionError("PROP-HN-001 데이터를 찾지 못했습니다."));

        // when
        final TargetPropertyValidationResponse response =
            targetPropertyValidationService.validateTargetProperty(
                "11",
                "11680",
                property.getPropertyId()
            );

        // then
        assertThat(response.getPropertyId()).isEqualTo(property.getPropertyId());
        assertThat(response.getPriceSnapshot()).isEqualTo(375_000_000L);
        assertThat(response.getHousingType()).isEqualTo(HousingType.OWNED_APT);
    }

    @DisplayName("존재하지 않는 targetPropertyId는 HOUSING_PROPERTY_NOT_FOUND로 실패한다")
    @Test
    void validateTargetPropertyWithUnknownPropertyId() {
        // given
        worldHousingSeedService.seed();

        // when & then
        assertThatThrownBy(() -> targetPropertyValidationService.validateTargetProperty("11", "11680", 999_999L))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.HOUSING_PROPERTY_NOT_FOUND);
    }

    @DisplayName("다른 지역 또는 구군에 속한 targetPropertyId는 검증에 실패한다")
    @Test
    void validateTargetPropertyWithMismatchedLocation() {
        // given
        worldHousingSeedService.seed();
        final RealEstateProperty property = realEstatePropertyRepository.findByProviderId("PROP-SP-001")
            .orElseThrow(() -> new AssertionError("PROP-SP-001 데이터를 찾지 못했습니다."));

        // when & then
        assertThatThrownBy(() -> targetPropertyValidationService.validateTargetProperty(
                "11",
                "11680",
                property.getPropertyId()
            ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.HOUSING_PROPERTY_NOT_FOUND);
    }
}
