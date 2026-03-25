package io.ssafy.p.j14c103.homerun.api.service.world.housing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.RealEstateDocumentsQueryResponse;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocument;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocumentChecklistItem;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocumentRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocumentType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateProperty;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateRegistryQuizSample;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateRegistryRow;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateRegistrySection;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WorldRealEstateDocumentQueryServiceTest {

    @Autowired
    private WorldRealEstateDocumentQueryService worldRealEstateDocumentQueryService;

    @Autowired
    private RealEstatePropertyRepository realEstatePropertyRepository;

    @Autowired
    private RealEstateDocumentRepository realEstateDocumentRepository;

    @DisplayName("문서 조회는 checklist를 포함한 documents 배열을 documentId 오름차순으로 반환한다.")
    @Test
    void getDocuments() {
        // given
        final RealEstateProperty property = realEstatePropertyRepository.saveAndFlush(createProperty("PROP-DOC-001"));
        final RealEstateDocument firstDocument = realEstateDocumentRepository.saveAndFlush(createDocument(
            property.getPropertyId(),
            "/images/docs/registry-gapgu.png",
            List.of(
                RealEstateDocumentChecklistItem.create("CHECK-001", "소유권 변동 이력 확인", false),
                RealEstateDocumentChecklistItem.create("TRAP-001", "가등기 말소 여부 확인", true)
            ),
            RealEstateRegistrySection.GAPGU
        ));
        final RealEstateDocument laterDocument = realEstateDocumentRepository.saveAndFlush(createDocument(
            property.getPropertyId(),
            "/images/docs/registry-eulgu.png",
            List.of(
                RealEstateDocumentChecklistItem.create("TRAP-002", "근저당 설정 확인", true),
                RealEstateDocumentChecklistItem.create("CHECK-002", "압류 기록 확인", false)
            ),
            RealEstateRegistrySection.EULGU
        ));

        // when
        final RealEstateDocumentsQueryResponse response =
            worldRealEstateDocumentQueryService.getDocuments(property.getPropertyId());

        // then
        assertThat(response.getDocuments()).hasSize(2);
        assertThat(response.getDocuments().get(0).getDocumentId()).isEqualTo(firstDocument.getRealEstateDocumentId());
        assertThat(response.getDocuments().get(0).getType()).isEqualTo("등기사항전부증명서");
        assertThat(response.getDocuments().get(0).getImageUrl()).isEqualTo("/images/docs/registry-gapgu.png");
        assertThat(response.getDocuments().get(0).getChecklist())
            .extracting(
                RealEstateDocumentsQueryResponse.ChecklistItem::getTrapId,
                RealEstateDocumentsQueryResponse.ChecklistItem::getLabel,
                RealEstateDocumentsQueryResponse.ChecklistItem::isTrapped
            )
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple("CHECK-001", "소유권 변동 이력 확인", false),
                org.assertj.core.groups.Tuple.tuple("TRAP-001", "가등기 말소 여부 확인", true)
            );
        assertThat(response.getDocuments().get(1).getDocumentId()).isEqualTo(laterDocument.getRealEstateDocumentId());
    }

    @DisplayName("존재하지 않는 propertyId면 HOUSING_PROPERTY_NOT_FOUND 예외가 발생한다.")
    @Test
    void getDocumentsWithUnknownProperty() {
        // when & then
        assertThatThrownBy(() -> worldRealEstateDocumentQueryService.getDocuments(9999L))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.HOUSING_PROPERTY_NOT_FOUND);
    }

    @DisplayName("매물은 존재하지만 문서가 없으면 빈 documents 배열을 반환한다.")
    @Test
    void getDocumentsWithNoDocuments() {
        // given
        final RealEstateProperty property = realEstatePropertyRepository.saveAndFlush(createProperty("PROP-DOC-EMPTY"));

        // when
        final RealEstateDocumentsQueryResponse response =
            worldRealEstateDocumentQueryService.getDocuments(property.getPropertyId());

        // then
        assertThat(response.getDocuments()).isEmpty();
    }

    private RealEstateProperty createProperty(final String providerId) {
        return RealEstateProperty.create(
            providerId,
            "문서 조회 테스트용 매물",
            "서울특별시 강남구 테헤란로 101",
            "11",
            "11680",
            Money.of(375_000_000L),
            BigDecimal.valueOf(37.5172),
            BigDecimal.valueOf(127.0473),
            HousingType.OWNED_APT,
            List.of()
        );
    }

    private RealEstateDocument createDocument(
        final Long propertyId,
        final String imageUrl,
        final List<RealEstateDocumentChecklistItem> checklist,
        final RealEstateRegistrySection registrySection
    ) {
        return RealEstateDocument.create(
            propertyId,
            RealEstateDocumentType.REGISTRY,
            registrySection,
            imageUrl,
            checklist,
            RealEstateRegistryQuizSample.create(
                "정상",
                List.of(
                    RealEstateRegistryRow.create(
                        "1",
                        "기록사항 없음",
                        "2025년 1월 20일",
                        "없음",
                        "등기부상 특이사항 없음",
                        Map.of()
                    )
                ),
                "해설",
                List.of("포인트"),
                "정답 해설",
                "오답 해설"
            )
        );
    }
}
