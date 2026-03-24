package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;

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
    private RealEstatePropertyRepository realEstatePropertyRepository;

    @Autowired
    private RealEstateDocumentRepository realEstateDocumentRepository;

    @AfterEach
    void tearDown() {
        realEstateDocumentRepository.deleteAllInBatch();
        realEstatePropertyRepository.deleteAllInBatch();
    }

    @DisplayName("seed를 실행하면 서울과 광주 지역을 커버하는 목표 매물과 문서가 적재된다")
    @Test
    void seedHousingContents() {
        // when
        worldHousingSeedService.seed();

        // then
        final List<RealEstateProperty> properties = realEstatePropertyRepository.findAll();
        final List<RealEstateDocument> documents = realEstateDocumentRepository.findAll();

        assertThat(properties).isNotEmpty();
        assertThat(documents).isNotEmpty();
        assertThat(properties)
            .extracting(RealEstateProperty::getRegionCode)
            .contains("SEOUL", "GWANGJU");
        assertThat(properties.stream()
            .filter(property -> property.getRegionCode().equals("SEOUL"))
            .map(RealEstateProperty::getDistrictCode)
            .distinct()
            .toList())
            .containsExactlyInAnyOrder("GANGNAM", "SONGPA", "MAPO", "GWANGJIN");
        assertThat(documents).hasSize(properties.size() * 2);
        assertThat(documents)
            .extracting(RealEstateDocument::getPropertyId)
            .allMatch(propertyId -> properties.stream()
                .anyMatch(property -> property.getPropertyId().equals(propertyId)));
    }

    @DisplayName("같은 seed를 다시 실행해도 매물과 문서 수가 증가하지 않는다")
    @Test
    void reseedDoesNotDuplicateHousingData() {
        // given
        worldHousingSeedService.seed();
        final long propertyCountBefore = realEstatePropertyRepository.count();
        final long documentCountBefore = realEstateDocumentRepository.count();

        // when
        worldHousingSeedService.seed();

        // then
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
        final List<RealEstateDocument> documents =
            realEstateDocumentRepository.findAllByPropertyIdOrderByRealEstateDocumentIdAsc(
                property.getPropertyId()
            );

        // then
        assertThat(property.getRegionCode()).isEqualTo("SEOUL");
        assertThat(property.getDistrictCode()).isEqualTo("GANGNAM");
        assertThat(property.getPropertyName()).isEqualTo("하남3지구 모아엘가 더 퍼스트");
        assertThat(property.getBasePrice().getAmount()).isEqualByComparingTo("375000000");
        assertThat(property.getLatitude()).isNotNull();
        assertThat(property.getLongitude()).isNotNull();
        assertThat(documents)
            .extracting(RealEstateDocument::getRegistrySection)
            .containsExactlyInAnyOrder(RealEstateRegistrySection.GAPGU, RealEstateRegistrySection.EULGU);
        assertThat(documents)
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
