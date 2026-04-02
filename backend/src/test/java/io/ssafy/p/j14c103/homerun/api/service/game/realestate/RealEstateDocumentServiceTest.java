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
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocument;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocumentChecklistItem;
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
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class RealEstateDocumentServiceTest extends IntegrationTestSupport {

    @Autowired
    private RealEstateDocumentService realEstateDocumentService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private UserRepository userRepository;

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
        final User user = saveUser("document-rendered-user@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(user.getId()));
        final RealEstateProperty property = realEstatePropertyRepository.saveAndFlush(createProperty());
        realEstateDocumentRepository.saveAllAndFlush(List.of(
            createGapguDocument(property.getPropertyId(), "위험"),
            createEulguDocument(property.getPropertyId(), "정상")
        ));
        given(realEstateRegistryRandomService.nextGapguIndex(1)).willReturn(0);
        given(realEstateRegistryRandomService.nextEulguIndex(1)).willReturn(0);

        // when
        final RealEstateDocumentResponse response = realEstateDocumentService.getDocument(
            user.getId(),
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
        assertThat(response.getChecklistItems())
            .extracting(RealEstateDocumentResponse.ChecklistItemResponse::getTrapId)
            .containsExactly("TRAP-HN-001", "CHECK-HN-001");
        assertThat(response.getChecklistItems())
            .extracting(RealEstateDocumentResponse.ChecklistItemResponse::getLabel)
            .containsExactly("소유권 변동 이력 확인", "가등기 말소 여부 확인");
        assertThat(response.getSolution().getGapgu().getVerdict()).isEqualTo("위험");
        assertThat(response.getSolution().getEulgu().getVerdict()).isEqualTo("정상");
        assertThat(response.getSolution().getVerdict()).isEqualTo("위험");
    }

    @DisplayName("갑구와 을구가 모두 정상 샘플이면 전체 verdict도 정상이다")
    @Test
    void getDocumentWithNormalVerdict() {
        // given
        final User user = saveUser("document-normal-user@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(user.getId()));
        final RealEstateProperty property = realEstatePropertyRepository.saveAndFlush(createProperty());
        realEstateDocumentRepository.saveAllAndFlush(List.of(
            createGapguDocument(property.getPropertyId(), "정상"),
            createEmptyEulguDocument(property.getPropertyId(), "정상")
        ));
        given(realEstateRegistryRandomService.nextGapguIndex(1)).willReturn(0);
        given(realEstateRegistryRandomService.nextEulguIndex(1)).willReturn(0);

        // when
        final RealEstateDocumentResponse response = realEstateDocumentService.getDocument(
            user.getId(),
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
        final User user = saveUser("document-empty-sample-user@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(user.getId()));
        final RealEstateProperty property = realEstatePropertyRepository.saveAndFlush(createProperty());
        realEstateDocumentRepository.saveAndFlush(createGapguDocument(property.getPropertyId(), "정상"));

        // when & then
        assertThatThrownBy(() -> realEstateDocumentService.getDocument(
            user.getId(),
            gameSession.getGameSessionId(),
            property.getPropertyId()
        ))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.HOUSING_REGISTRY_SAMPLE_INVALID);
    }

    @DisplayName("property별 문서에 샘플 payload가 없으면 global 샘플 row를 fallback으로 사용한다")
    @Test
    void getDocumentWithGlobalSampleFallback() {
        // given
        final User user = saveUser("document-global-fallback-user@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(user.getId()));
        final RealEstateProperty property = realEstatePropertyRepository.saveAndFlush(createProperty());
        realEstateDocumentRepository.saveAllAndFlush(List.of(
            createChecklistOnlyGapguDocument(property.getPropertyId()),
            createChecklistOnlyEulguDocument(property.getPropertyId()),
            createGlobalGapguSample(),
            createGlobalEulguSample()
        ));
        given(realEstateRegistryRandomService.nextGapguIndex(1)).willReturn(0);
        given(realEstateRegistryRandomService.nextEulguIndex(1)).willReturn(0);

        // when
        final RealEstateDocumentResponse response = realEstateDocumentService.getDocument(
            user.getId(),
            gameSession.getGameSessionId(),
            property.getPropertyId()
        );

        // then
        assertThat(response.getGapguRows()).hasSize(1);
        assertThat(response.getGapguRows().get(0).getDetails()).contains("global gapgu sample");
        assertThat(response.getEulguRows()).hasSize(1);
        assertThat(response.getEulguRows().get(0).getDetails()).contains("global eulgu sample");
        assertThat(response.getChecklistItems())
            .extracting(RealEstateDocumentResponse.ChecklistItemResponse::getTrapId)
            .containsExactly("TRAP-GLOBAL-001");
        assertThat(response.getSolution().getVerdict()).isEqualTo("정상");
    }

    @DisplayName("property별 문서 payload가 불완전해도 global 샘플 row를 fallback으로 사용한다")
    @Test
    void getDocumentWithMalformedPropertyPayloadUsesGlobalFallback() {
        // given
        final User user = saveUser("document-global-fallback-malformed-user@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(user.getId()));
        final RealEstateProperty property = realEstatePropertyRepository.saveAndFlush(createProperty());
        realEstateDocumentRepository.saveAllAndFlush(List.of(
            createMalformedGapguDocument(property.getPropertyId()),
            createMalformedEulguDocument(property.getPropertyId()),
            createGlobalGapguSample(),
            createGlobalEulguSample()
        ));
        given(realEstateRegistryRandomService.nextGapguIndex(1)).willReturn(0);
        given(realEstateRegistryRandomService.nextEulguIndex(1)).willReturn(0);

        // when
        final RealEstateDocumentResponse response = realEstateDocumentService.getDocument(
            user.getId(),
            gameSession.getGameSessionId(),
            property.getPropertyId()
        );

        // then
        assertThat(response.getGapguRows()).hasSize(1);
        assertThat(response.getGapguRows().get(0).getDetails()).contains("global gapgu sample");
        assertThat(response.getEulguRows()).hasSize(1);
        assertThat(response.getEulguRows().get(0).getDetails()).contains("global eulgu sample");
        assertThat(response.getChecklistItems())
            .extracting(RealEstateDocumentResponse.ChecklistItemResponse::getTrapId)
            .containsExactly("TRAP-MALFORMED-001");
    }

    @DisplayName("존재하지 않는 세션이면 game 세션 조회 에러를 던진다")
    @Test
    void getDocumentWithUnknownSession() {
        // given
        final User user = saveUser("document-unknown-session-user@example.com");
        final RealEstateProperty property = realEstatePropertyRepository.saveAndFlush(createProperty());

        // when & then
        assertThatThrownBy(() -> realEstateDocumentService.getDocument(user.getId(), 9999L, property.getPropertyId()))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.GAME_SESSION_NOT_FOUND);
    }

    @DisplayName("존재하지 않는 매물이면 housing 매물 조회 에러를 던진다")
    @Test
    void getDocumentWithUnknownProperty() {
        // given
        final User user = saveUser("document-unknown-property-user@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(user.getId()));

        // when & then
        assertThatThrownBy(() -> realEstateDocumentService.getDocument(
            user.getId(),
            gameSession.getGameSessionId(),
            9999L
        ))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.HOUSING_PROPERTY_NOT_FOUND);
    }

    @DisplayName("다른 사용자의 세션이면 GAME_SESSION_FORBIDDEN을 던진다")
    @Test
    void getDocumentWithForbiddenSession() {
        // given
        final User owner = saveUser("document-owner@example.com");
        final User requester = saveUser("document-requester@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(owner.getId()));
        final RealEstateProperty property = realEstatePropertyRepository.saveAndFlush(createProperty());

        // when & then
        assertThatThrownBy(() -> realEstateDocumentService.getDocument(
            requester.getId(),
            gameSession.getGameSessionId(),
            property.getPropertyId()
        ))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.GAME_SESSION_FORBIDDEN);
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

    private User saveUser(final String email) {
        return userRepository.save(User.register(Email.of(email), "tester", "hashed-password"));
    }

    private GameSession createGameSession(final Long userId) {
        final GameSession gameSession = GameSession.create(
            userId,
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
            null,
            List.of(
                RealEstateDocumentChecklistItem.create(
                    "TRAP-HN-001",
                    "소유권 변동 이력 확인",
                    false
                )
            ),
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

    private RealEstateDocument createChecklistOnlyGapguDocument(final Long propertyId) {
        return RealEstateDocument.create(
            propertyId,
            RealEstateDocumentType.REGISTRY,
            RealEstateRegistrySection.GAPGU,
            null,
            List.of(
                RealEstateDocumentChecklistItem.create(
                    "TRAP-GLOBAL-001",
                    "글로벌 샘플 체크리스트",
                    false
                )
            ),
            null
        );
    }

    private RealEstateDocument createMalformedGapguDocument(final Long propertyId) {
        return RealEstateDocument.create(
            propertyId,
            RealEstateDocumentType.REGISTRY,
            RealEstateRegistrySection.GAPGU,
            null,
            List.of(
                RealEstateDocumentChecklistItem.create(
                    "TRAP-MALFORMED-001",
                    "불완전 payload 체크리스트",
                    false
                )
            ),
            RealEstateRegistryQuizSample.create(
                "정상",
                List.of(
                    RealEstateRegistryRow.create(
                        "1",
                        "소유권보존",
                        "2024년 1월 1일",
                        "보존",
                        "malformed gapgu sample",
                        Map.of()
                    )
                ),
                "갑구 해설",
                null,
                "갑구 정답 해설",
                "갑구 오답 해설"
            )
        );
    }

    private RealEstateDocument createChecklistOnlyEulguDocument(final Long propertyId) {
        return RealEstateDocument.create(
            propertyId,
            RealEstateDocumentType.REGISTRY,
            RealEstateRegistrySection.EULGU,
            null,
            List.of(),
            null
        );
    }

    private RealEstateDocument createMalformedEulguDocument(final Long propertyId) {
        return RealEstateDocument.create(
            propertyId,
            RealEstateDocumentType.REGISTRY,
            RealEstateRegistrySection.EULGU,
            null,
            List.of(),
            RealEstateRegistryQuizSample.create(
                "정상",
                List.of(
                    RealEstateRegistryRow.create(
                        "1",
                        "근저당권설정",
                        "2024년 1월 1일",
                        "설정",
                        "malformed eulgu sample",
                        Map.of()
                    )
                ),
                "을구 해설",
                List.of("을구 포인트"),
                "을구 정답 해설",
                null
            )
        );
    }

    private RealEstateDocument createGlobalGapguSample() {
        return RealEstateDocument.create(
            null,
            RealEstateDocumentType.REGISTRY,
            RealEstateRegistrySection.GAPGU,
            null,
            List.of(),
            RealEstateRegistryQuizSample.create(
                "정상",
                List.of(
                    RealEstateRegistryRow.create(
                        "1",
                        "소유권보존",
                        "2024년 1월 1일",
                        "보존",
                        "global gapgu sample",
                        Map.of()
                    )
                ),
                "갑구 해설",
                List.of("갑구 포인트"),
                "갑구 정답 해설",
                "갑구 오답 해설"
            )
        );
    }

    private RealEstateDocument createGlobalEulguSample() {
        return RealEstateDocument.create(
            null,
            RealEstateDocumentType.REGISTRY,
            RealEstateRegistrySection.EULGU,
            null,
            List.of(),
            RealEstateRegistryQuizSample.create(
                "정상",
                List.of(
                    RealEstateRegistryRow.create(
                        "1",
                        "근저당권설정",
                        "2024년 1월 1일",
                        "설정",
                        "global eulgu sample",
                        Map.of()
                    )
                ),
                "을구 해설",
                List.of("을구 포인트"),
                "을구 정답 해설",
                "을구 오답 해설"
            )
        );
    }

    private RealEstateDocument createEulguDocument(final Long propertyId, final String verdict) {
        return RealEstateDocument.create(
            propertyId,
            RealEstateDocumentType.REGISTRY,
            RealEstateRegistrySection.EULGU,
            null,
            List.of(
                RealEstateDocumentChecklistItem.create(
                    "CHECK-HN-001",
                    "가등기 말소 여부 확인",
                    true
                )
            ),
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
            null,
            List.of(
                RealEstateDocumentChecklistItem.create(
                    "CHECK-HN-001",
                    "가등기 말소 여부 확인",
                    true
                )
            ),
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
