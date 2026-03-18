package io.ssafy.p.j14c103.homerun.domain.world.housing;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.domain.money.Money;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class RealEstateDocumentRepositoryTest {

    @Autowired
    private RealEstatePropertyRepository realEstatePropertyRepository;

    @Autowired
    private RealEstateDocumentRepository realEstateDocumentRepository;

    @DisplayName("RealEstateDocument를 저장하면 문서 타입과 체크리스트를 다시 조회할 수 있다.")
    @Test
    void saveRealEstateDocument() {
        // given
        RealEstateProperty property = createProperty("PROP-SEOUL-101", "GANGNAM");
        RealEstateProperty savedProperty = realEstatePropertyRepository.saveAndFlush(property);

        RealEstateDocument document = RealEstateDocument.create(
            savedProperty.getPropertyId(),
            RealEstateDocumentType.REGISTRY,
            "/images/docs/registry.png",
            List.of(
                RealEstateChecklistItem.create("TRAP-01", "근저당 설정 확인", true),
                RealEstateChecklistItem.create("TRAP-02", "소유자 일치 확인", false)
            )
        );

        // when
        RealEstateDocument saved = realEstateDocumentRepository.saveAndFlush(document);
        RealEstateDocument found = realEstateDocumentRepository.findById(saved.getRealEstateDocumentId())
            .orElseThrow();

        // then
        assertThat(found.getPropertyId()).isEqualTo(savedProperty.getPropertyId());
        assertThat(found.getDocumentType()).isEqualTo(RealEstateDocumentType.REGISTRY);
        assertThat(found.getImageUrl()).isEqualTo("/images/docs/registry.png");
        assertThat(found.getChecklist()).hasSize(2);
        assertThat(found.getChecklist())
            .extracting(RealEstateChecklistItem::getTrapId)
            .containsExactly("TRAP-01", "TRAP-02");
        assertThat(found.getChecklist())
            .extracting(RealEstateChecklistItem::getLabel)
            .containsExactly("근저당 설정 확인", "소유자 일치 확인");
        assertThat(found.getChecklist())
            .extracting(RealEstateChecklistItem::getIsTrapped)
            .containsExactly(true, false);
    }

    @DisplayName("propertyId로 해당 매물의 문서를 정렬 조회할 수 있다.")
    @Test
    void findAllByPropertyIdOrderByRealEstateDocumentIdAsc() {
        // given
        RealEstateProperty property = createProperty("PROP-SEOUL-102", "MAPO");
        RealEstateProperty savedProperty = realEstatePropertyRepository.saveAndFlush(property);

        RealEstateDocument first = RealEstateDocument.create(
            savedProperty.getPropertyId(),
            RealEstateDocumentType.REGISTRY,
            "/images/docs/registry.png",
            List.of(RealEstateChecklistItem.create("TRAP-01", "근저당 설정 확인", true))
        );

        RealEstateDocument second = RealEstateDocument.create(
            savedProperty.getPropertyId(),
            RealEstateDocumentType.CONTRACT,
            "/images/docs/contract.png",
            List.of(RealEstateChecklistItem.create("TRAP-02", "소유자 일치 확인", false))
        );

        realEstateDocumentRepository.saveAllAndFlush(List.of(first, second));

        // when
        List<RealEstateDocument> result =
            realEstateDocumentRepository.findAllByPropertyIdOrderByRealEstateDocumentIdAsc(
                savedProperty.getPropertyId()
            );

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
            .extracting(RealEstateDocument::getDocumentType)
            .containsExactly(
                RealEstateDocumentType.REGISTRY,
                RealEstateDocumentType.CONTRACT
            );
    }

    private RealEstateProperty createProperty(
        String providerId,
        String districtCode
    ) {
        return RealEstateProperty.create(
            providerId,
            "문서 테스트용 매물",
            "서울시 어딘가",
            "SEOUL",
            districtCode,
            Money.of(320_000_000L),
            BigDecimal.valueOf(37.5665),
            BigDecimal.valueOf(126.9780),
            HousingType.JEONSE_APT,
            List.of()
        );
    }
}
