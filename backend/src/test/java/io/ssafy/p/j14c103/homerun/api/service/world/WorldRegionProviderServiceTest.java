package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.api.service.world.response.RegionsProviderResponse;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocumentRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class WorldRegionProviderServiceTest {

    @Autowired
    private WorldRegionProviderService worldRegionProviderService;

    @Autowired
    private WorldHousingSeedService worldHousingSeedService;

    @Autowired
    private RealEstatePropertyRepository realEstatePropertyRepository;

    @Autowired
    private RealEstateDocumentRepository realEstateDocumentRepository;

    @AfterEach
    void tearDown() {
        realEstateDocumentRepository.deleteAllInBatch();
        realEstatePropertyRepository.deleteAllInBatch();
    }

    @DisplayName("seed 적재 후 DB 원천 기준의 목표 지역 목록 provider 응답을 반환한다")
    @Test
    void getRegions() {
        // given
        worldHousingSeedService.seed();

        // when
        final RegionsProviderResponse response = worldRegionProviderService.getRegions();

        // then
        assertThat(response.getRegions())
            .extracting(RegionsProviderResponse.RegionItem::getRegionCode)
            .containsExactly("SEOUL", "GWANGJU");
        assertThat(response.getRegions())
            .extracting(RegionsProviderResponse.RegionItem::getName)
            .containsExactly("서울", "광주");
    }

    @DisplayName("원천 지역 데이터가 비어 있어도 빈 배열 계약을 반환한다")
    @Test
    void getRegionsWithEmptyPlan() {
        // when
        final RegionsProviderResponse response = worldRegionProviderService.getRegions();

        // then
        assertThat(response.getRegions()).isEmpty();
    }

    @DisplayName("provider 응답은 regionCode와 name만 노출하는 지역 목록 계약을 유지한다")
    @Test
    void getRegionsContract() {
        // given
        worldHousingSeedService.seed();

        // when
        final RegionsProviderResponse response = worldRegionProviderService.getRegions();

        // then
        assertThat(response.getRegions()).hasSize(2);
        assertThat(response.getRegions().get(0).getRegionCode()).isEqualTo("SEOUL");
        assertThat(response.getRegions().get(0).getName()).isEqualTo("서울");
    }
}
