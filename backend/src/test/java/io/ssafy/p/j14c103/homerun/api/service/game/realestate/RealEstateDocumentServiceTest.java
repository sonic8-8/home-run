package io.ssafy.p.j14c103.homerun.api.service.game.realestate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import io.ssafy.p.j14c103.homerun.api.service.game.realestate.response.RealEstateDocumentResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocument;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocumentRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocumentType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateMoneyRenderingRule;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class RealEstateDocumentServiceTest {

    @Autowired
    private RealEstateDocumentService realEstateDocumentService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private RealEstatePropertyRepository realEstatePropertyRepository;

    @Autowired
    private RealEstateDocumentRepository realEstateDocumentRepository;

    @MockitoBean
    private RealEstateRegistryRandomService realEstateRegistryRandomService;

    @DisplayName("고정된 문서 인덱스로 등기부등본 조회 시 금액 토큰을 치환한 응답을 반환한다")
    @Test
    void getDocumentWithRenderedMoneyText() {
        // given
        GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession());
        RealEstateProperty property = realEstatePropertyRepository.saveAndFlush(createProperty());
        realEstateDocumentRepository.saveAllAndFlush(List.of(
            createGapguDocument(property.getPropertyId(), "위험"),
            createEulguDocument(property.getPropertyId(), "정상")
        ));
        given(realEstateRegistryRandomService.nextGapguIndex(1)).willReturn(0);
        given(realEstateRegistryRandomService.nextEulguIndex(1)).willReturn(0);

        // when
        RealEstateDocumentResponse response = realEstateDocumentService.getDocument(
            gameSession.getGameSessionId(),
            property.getPropertyId()
        );

        // then
        assertThat(response.getPropertyName()).isEqualTo("서초아트자이");
        assertThat(response.getSalePrice()).isEqualTo(1_300_000_000L);
        assertThat(response.getGapguRows()).hasSize(1);
        assertThat(response.getGapguRows().get(0).getDetails())
            .isEqualTo("청구금액 금156,000,000원 가압류권자 주식회사 한빛자산관리 서울중앙지방법원의 가압류결정");
        assertThat(response.getEulguRows()).hasSize(1);
        assertThat(response.getEulguRows().get(0).getDetails())
            .isEqualTo("채권최고액 금195,000,000원 채무자 김도윤 근저당권자 주식회사 한울저축은행");
        assertThat(response.getSolution().getGapgu().getVerdict()).isEqualTo("위험");
        assertThat(response.getSolution().getEulgu().getVerdict()).isEqualTo("정상");
        assertThat(response.getSolution().getVerdict()).isEqualTo("위험");
    }

    @DisplayName("갑구와 을구가 모두 정상 샘플이면 전체 verdict도 정상이다")
    @Test
    void getDocumentWithNormalVerdict() {
        // given
        GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession());
        RealEstateProperty property = realEstatePropertyRepository.saveAndFlush(createProperty());
        realEstateDocumentRepository.saveAllAndFlush(List.of(
            createGapguDocument(property.getPropertyId(), "정상"),
            createEmptyEulguDocument(property.getPropertyId(), "정상")
        ));
        given(realEstateRegistryRandomService.nextGapguIndex(1)).willReturn(0);
        given(realEstateRegistryRandomService.nextEulguIndex(1)).willReturn(0);

        // when
        RealEstateDocumentResponse response = realEstateDocumentService.getDocument(
            gameSession.getGameSessionId(),
            property.getPropertyId()
        );

        // then
        assertThat(response.getSolution().getGapgu().getVerdict()).isEqualTo("정상");
        assertThat(response.getSolution().getEulgu().getVerdict()).isEqualTo("정상");
        assertThat(response.getSolution().getVerdict()).isEqualTo("정상");
        assertThat(response.getEulguRows()).isEmpty();
    }

    @DisplayName("등기부 샘플 풀이 비어 있으면 샘플 데이터 구성 에러를 던진다")
    @Test
    void getDocumentWithEmptySamplePool() {
        // given
        GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession());
        RealEstateProperty property = realEstatePropertyRepository.saveAndFlush(createProperty());
        realEstateDocumentRepository.saveAndFlush(createGapguDocument(property.getPropertyId(), "정상"));

        // when & then
        assertThatThrownBy(() -> realEstateDocumentService.getDocument(
            gameSession.getGameSessionId(),
            property.getPropertyId()
        ))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.HOUSING_REGISTRY_SAMPLE_INVALID);
    }

    @DisplayName("존재하지 않는 세션이면 world 세션 조회 에러를 던진다")
    @Test
    void getDocumentWithUnknownSession() {
        // given
        RealEstateProperty property = realEstatePropertyRepository.saveAndFlush(createProperty());

        // when & then
        assertThatThrownBy(() -> realEstateDocumentService.getDocument(9999L, property.getPropertyId()))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.WORLD_SESSION_NOT_FOUND);
    }

    @DisplayName("존재하지 않는 매물이면 housing 매물 조회 에러를 던진다")
    @Test
    void getDocumentWithUnknownProperty() {
        // given
        GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession());

        // when & then
        assertThatThrownBy(() -> realEstateDocumentService.getDocument(gameSession.getGameSessionId(), 9999L))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.HOUSING_PROPERTY_NOT_FOUND);
    }

    private RealEstateProperty createProperty() {
        return RealEstateProperty.create(
            "SEOCHO-ART-XI",
            "서초아트자이",
            "서울특별시 서초구 반포대로 58",
            "SEOUL",
            "SEOCHO",
            Money.of(1_300_000_000L),
            BigDecimal.valueOf(37.485551),
            BigDecimal.valueOf(127.011500),
            HousingType.OWNED_APT,
            null
        );
    }

    private GameSession createGameSession() {
        GameSession gameSession = GameSession.create(
            1L,
            1,
            "윤서",
            CharacterType.FEMALE,
            JobType.STARTUP,
            HousingType.STUDIO,
            "SEOUL",
            "SEOCHO",
            101L,
            DataSourceType.PROFILE
        );
        gameSession.initializeCapital(
            Money.of(2_000_000L),
            Money.of(2_000_000L),
            java.time.LocalDate.of(2026, 1, 1),
            CyclePhase.BOOM
        );
        return gameSession;
    }

    private RealEstateDocument createGapguDocument(final Long propertyId, final String verdict) {
        return RealEstateDocument.create(
            propertyId,
            RealEstateDocumentType.REGISTRY,
            RealEstateRegistrySection.GAPGU,
            RealEstateRegistryQuizSample.create(
                verdict,
                List.of(
                    RealEstateRegistryRow.create(
                        "3",
                        "가압류",
                        "2025년 2월 7일",
                        "가압류결정",
                        "청구금액 {claim_amount} 가압류권자 주식회사 한빛자산관리 서울중앙지방법원의 가압류결정",
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
            )
        );
    }

    private RealEstateDocument createEulguDocument(final Long propertyId, final String verdict) {
        return RealEstateDocument.create(
            propertyId,
            RealEstateDocumentType.REGISTRY,
            RealEstateRegistrySection.EULGU,
            RealEstateRegistryQuizSample.create(
                verdict,
                List.of(
                    RealEstateRegistryRow.create(
                        "1",
                        "근저당권설정",
                        "2025년 1월 17일",
                        "2025년 1월 10일 설정계약",
                        "채권최고액 {max_claim_amount} 채무자 김도윤 근저당권자 주식회사 한울저축은행",
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
            )
        );
    }

    private RealEstateDocument createEmptyEulguDocument(final Long propertyId, final String verdict) {
        return RealEstateDocument.create(
            propertyId,
            RealEstateDocumentType.REGISTRY,
            RealEstateRegistrySection.EULGU,
            RealEstateRegistryQuizSample.create(
                verdict,
                List.of(),
                "을구 해설",
                List.of("을구 포인트"),
                "을구 정답 해설",
                "을구 오답 해설"
            )
        );
    }
}
