package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingDistrict;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingDistrictRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingRegion;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingRegionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocument;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocumentRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateProperty;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateRegistryQuizSample;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateRegistrySection;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class WorldHousingSeedServiceTest {

    @Autowired
    private WorldHousingSeedService worldHousingSeedService;

    @Autowired
    private HousingRegionRepository housingRegionRepository;

    @Autowired
    private HousingDistrictRepository housingDistrictRepository;

    @Autowired
    private RealEstatePropertyRepository realEstatePropertyRepository;

    @Autowired
    private RealEstateDocumentRepository realEstateDocumentRepository;

    @AfterEach
    void tearDown() {
        realEstateDocumentRepository.deleteAllInBatch();
        realEstatePropertyRepository.deleteAllInBatch();
        housingDistrictRepository.deleteAllInBatch();
        housingRegionRepository.deleteAllInBatch();
    }

    @DisplayName("seed를 실행하면 지역/구군 마스터와 목표 매물/문서가 함께 적재된다")
    @Test
    void seedHousingContents() {
        // when
        worldHousingSeedService.seed();

        // then
        final List<HousingRegion> regions = housingRegionRepository.findAllByOrderByRegionCodeAsc();
        final List<HousingDistrict> districts = housingDistrictRepository.findAll();
        final List<RealEstateProperty> properties = realEstatePropertyRepository.findAll();
        final List<RealEstateDocument> documents = realEstateDocumentRepository.findAll();

        assertThat(regions)
            .extracting(HousingRegion::getRegionCode, HousingRegion::getRegionName)
            .containsExactly(
                tuple("11", "서울특별시"),
                tuple("24", "광주광역시")
            );
        assertThat(districts)
            .extracting(HousingDistrict::getDistrictCode, HousingDistrict::getRegionCode)
            .containsExactlyInAnyOrder(
                tuple("11680", "11"),
                tuple("11710", "11"),
                tuple("11440", "11"),
                tuple("11215", "11"),
                tuple("24170", "24")
            );
        assertThat(properties).isNotEmpty();
        assertThat(documents).isNotEmpty();
        assertThat(properties)
            .extracting(RealEstateProperty::getRegionCode)
            .contains("11", "24");
        assertThat(properties.stream()
            .filter(property -> property.getRegionCode().equals("11"))
            .map(RealEstateProperty::getDistrictCode)
            .distinct()
            .toList())
            .containsExactlyInAnyOrder("11680", "11710", "11440", "11215");
        assertThat(documents)
            .extracting(
                RealEstateDocument::getRegistrySection,
                document -> document.getQuizSamplePayload().getIssueSummary()
            )
            .doesNotHaveDuplicates();
    }

    @DisplayName("같은 seed를 다시 실행해도 매물과 문서 수가 증가하지 않는다")
    @Test
    void reseedDoesNotDuplicateHousingData() {
        // given
        worldHousingSeedService.seed();
        final long regionCountBefore = housingRegionRepository.count();
        final long districtCountBefore = housingDistrictRepository.count();
        final long propertyCountBefore = realEstatePropertyRepository.count();
        final long documentCountBefore = realEstateDocumentRepository.count();

        // when
        worldHousingSeedService.seed();

        // then
        assertThat(housingRegionRepository.count()).isEqualTo(regionCountBefore);
        assertThat(housingDistrictRepository.count()).isEqualTo(districtCountBefore);
        assertThat(realEstatePropertyRepository.count()).isEqualTo(propertyCountBefore);
        assertThat(realEstateDocumentRepository.count()).isEqualTo(documentCountBefore);
    }

    @DisplayName("seed를 실행하면 목표 매물 검증과 문서 조회에 필요한 최소 필드가 저장된다")
    @Test
    void seedTargetPropertyContract() {
        // given
        worldHousingSeedService.seed();

        // when
        final RealEstateProperty property = realEstatePropertyRepository.findAll().stream()
            .filter(candidate -> candidate.getProviderId().equals("PROP-HN-001"))
            .findFirst()
            .orElseThrow(() -> new AssertionError("PROP-HN-001 데이터를 찾지 못했습니다."));
        final List<RealEstateDocument> gapguDocuments =
            realEstateDocumentRepository.findAllByRegistrySectionOrderByRealEstateDocumentIdAsc(
                RealEstateRegistrySection.GAPGU
            );
        final List<RealEstateDocument> eulguDocuments =
            realEstateDocumentRepository.findAllByRegistrySectionOrderByRealEstateDocumentIdAsc(
                RealEstateRegistrySection.EULGU
            );

        // then
        assertThat(property.getRegionCode()).isEqualTo("11");
        assertThat(property.getDistrictCode()).isEqualTo("11680");
        assertThat(property.getLegalDongCode()).isEqualTo("1168000000");
        assertThat(property.getPropertyName()).isEqualTo("하남3지구 모아엘가 더 퍼스트");
        assertThat(property.getBasePrice().getAmount()).isEqualByComparingTo("375000000");
        assertThat(property.getLatitude()).isNotNull();
        assertThat(property.getLongitude()).isNotNull();
        assertThat(gapguDocuments).isNotEmpty();
        assertThat(eulguDocuments).isNotEmpty();
        assertThat(List.of(gapguDocuments, eulguDocuments))
            .flatExtracting(documents -> documents)
            .extracting(RealEstateDocument::getQuizSamplePayload)
            .allSatisfy(payload -> {
                final RealEstateRegistryQuizSample quizSample = (RealEstateRegistryQuizSample) payload;
                assertThat(quizSample).isNotNull();
                assertThat(quizSample.getRows()).isNotEmpty();
                assertThat(quizSample.getIssueSummary()).isNotBlank();
                assertThat(quizSample.getKeyPoints()).isNotEmpty();
                assertThat(quizSample.getFeedbackCorrect()).isNotBlank();
                assertThat(quizSample.getFeedbackWrong()).isNotBlank();
            });
    }
}
