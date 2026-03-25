package io.ssafy.p.j14c103.homerun.domain.world.housing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class RealEstateDocumentRepositoryTest {

    @Autowired
    private RealEstateDocumentRepository realEstateDocumentRepository;

    @DisplayName("RealEstateDocument를 저장하면 registry section과 quiz sample payload를 다시 조회할 수 있다")
    @Test
    void saveRealEstateDocument() {
        // given
        final RealEstateDocument document = RealEstateDocument.create(
            RealEstateRegistrySection.GAPGU,
            createGapguSample("위험")
        );

        // when
        final RealEstateDocument saved = realEstateDocumentRepository.saveAndFlush(document);
        final RealEstateDocument found = realEstateDocumentRepository.findById(
            saved.getRealEstateDocumentId()
        ).orElseThrow();

        // then
        assertThat(found.getRegistrySection()).isEqualTo(RealEstateRegistrySection.GAPGU);
        assertThat(found.getQuizSamplePayload().getQuizVerdict()).isEqualTo("위험");
        assertThat(found.getQuizSamplePayload().getRows()).hasSize(1);
        assertThat(found.getQuizSamplePayload().getRows().get(0).getDetails())
            .isEqualTo("청구금액 {claim_amount} 가압류");
        assertThat(found.getQuizSamplePayload().getRows().get(0).getRendering())
            .containsKey("claim_amount");
    }

    @DisplayName("registry section으로 공용 등기부 샘플을 정렬 조회할 수 있다")
    @Test
    void findAllByRegistrySectionOrderByRealEstateDocumentIdAsc() {
        // given
        final RealEstateDocument gapgu = RealEstateDocument.create(
            RealEstateRegistrySection.GAPGU,
            createGapguSample("정상")
        );
        final RealEstateDocument eulgu = RealEstateDocument.create(
            RealEstateRegistrySection.EULGU,
            createEulguSample("위험")
        );

        realEstateDocumentRepository.saveAllAndFlush(List.of(gapgu, eulgu));

        // when
        final List<RealEstateDocument> result = realEstateDocumentRepository
            .findAllByRegistrySectionOrderByRealEstateDocumentIdAsc(RealEstateRegistrySection.GAPGU);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRegistrySection()).isEqualTo(RealEstateRegistrySection.GAPGU);
        assertThat(result.get(0).getQuizSamplePayload().getQuizVerdict()).isEqualTo("정상");
    }

    private RealEstateRegistryQuizSample createGapguSample(final String verdict) {
        return RealEstateRegistryQuizSample.create(
            verdict,
            List.of(
                RealEstateRegistryRow.create(
                    "1",
                    "가압류",
                    "2025년 2월 7일",
                    "가압류결정",
                    "청구금액 {claim_amount} 가압류",
                    Map.of(
                        "claim_amount",
                        RealEstateMoneyRenderingRule.create("sale_price_ratio", 12, "만원")
                    )
                )
            ),
            "갑구 해설",
            List.of("갑구 포인트"),
            "갑구 정답 해설",
            "갑구 오답 해설"
        );
    }

    private RealEstateRegistryQuizSample createEulguSample(final String verdict) {
        return RealEstateRegistryQuizSample.create(
            verdict,
            List.of(
                RealEstateRegistryRow.create(
                    "1",
                    "근저당권설정",
                    "2025년 1월 17일",
                    "2025년 1월 10일 설정계약",
                    "채권최고액 {max_claim_amount}",
                    Map.of(
                        "max_claim_amount",
                        RealEstateMoneyRenderingRule.create("sale_price_ratio", 15, "만원")
                    )
                )
            ),
            "을구 해설",
            List.of("을구 포인트"),
            "을구 정답 해설",
            "을구 오답 해설"
        );
    }
}
