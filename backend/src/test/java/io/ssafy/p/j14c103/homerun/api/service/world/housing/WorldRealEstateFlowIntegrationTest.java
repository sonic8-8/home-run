package io.ssafy.p.j14c103.homerun.api.service.world.housing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.RealEstatePropertyBoundsServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.WorldContractReviewSubmitServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.WorldPurchaseValidationServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.PurchaseValidationFailureCode;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.RealEstateDocumentsQueryResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.RealEstatePropertyDetailProviderResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.RealEstatePropertyListProviderResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.WorldContractReviewSubmitServiceResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.WorldPurchaseValidationServiceResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousing;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousingRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.housing.ContractResult;
import io.ssafy.p.j14c103.homerun.domain.world.housing.ContractReviewStatus;
import io.ssafy.p.j14c103.homerun.domain.world.housing.ContractTrap;
import io.ssafy.p.j14c103.homerun.domain.world.housing.ContractTrapPenalty;
import io.ssafy.p.j14c103.homerun.domain.world.housing.GameContractReview;
import io.ssafy.p.j14c103.homerun.domain.world.housing.GameContractReviewRepository;
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
import jakarta.persistence.EntityManager;
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
class WorldRealEstateFlowIntegrationTest {

    @Autowired
    private WorldRealEstatePropertyProviderService worldRealEstatePropertyProviderService;

    @Autowired
    private WorldRealEstateDocumentQueryService worldRealEstateDocumentQueryService;

    @Autowired
    private WorldContractReviewSubmitService worldContractReviewSubmitService;

    @Autowired
    private WorldPurchaseValidationService worldPurchaseValidationService;

    @Autowired
    private RealEstatePropertyRepository realEstatePropertyRepository;

    @Autowired
    private RealEstateDocumentRepository realEstateDocumentRepository;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private GameHousingRepository gameHousingRepository;

    @Autowired
    private GameContractReviewRepository gameContractReviewRepository;

    @Autowired
    private EntityManager entityManager;

    @DisplayName("조회부터 계약 검토와 구매 검증까지 정상 플로우를 end-to-end로 검증한다.")
    @Test
    void successFlow() {
        // given
        final RealEstateProperty property = realEstatePropertyRepository.saveAndFlush(createProperty(
            "PROP-FLOW-SUCCESS",
            "성공 플로우 매물",
            BigDecimal.valueOf(37.5000),
            BigDecimal.valueOf(127.0300),
            List.of(
                createTrap("TRAP-01", "-5000000", 20),
                createTrap("TRAP-02", "-1000000", 10)
            )
        ));
        realEstateDocumentRepository.saveAndFlush(createDocument(
            property.getPropertyId(),
            RealEstateDocumentType.CONTRACT,
            "/images/docs/contract-success.png",
            List.of(
                RealEstateDocumentChecklistItem.create("TRAP-01", "가압류 확인", true),
                RealEstateDocumentChecklistItem.create("TRAP-02", "근저당 확인", true)
            )
        ));
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(property.getPropertyId()));
        gameHousingRepository.saveAndFlush(
            GameHousing.create(
                gameSession.getGameSessionId(),
                HousingType.STUDIO,
                Money.of(10_000_000L),
                Money.of(500_000L),
                Money.of(80_000L),
                201L
            )
        );

        // when
        final RealEstatePropertyListProviderResponse listResponse =
            worldRealEstatePropertyProviderService.getPropertiesInBounds(boundsAround(property));
        final RealEstatePropertyDetailProviderResponse detailResponse =
            worldRealEstatePropertyProviderService.getPropertyDetail(property.getPropertyId());
        final RealEstateDocumentsQueryResponse documentsResponse =
            worldRealEstateDocumentQueryService.getDocuments(property.getPropertyId());
        final WorldContractReviewSubmitServiceResponse reviewResponse =
            worldContractReviewSubmitService.submit(
                WorldContractReviewSubmitServiceRequest.of(
                    gameSession.getGameSessionId(),
                    property.getPropertyId(),
                    List.of("TRAP-01", "TRAP-02")
                )
            );
        final WorldPurchaseValidationServiceResponse validationResponse =
            worldPurchaseValidationService.validate(
                WorldPurchaseValidationServiceRequest.of(
                    gameSession.getGameSessionId(),
                    property.getPropertyId()
                )
            );

        // then
        assertThat(listResponse.getProperties())
            .extracting(RealEstatePropertyListProviderResponse.PropertySummary::getPropertyId)
            .contains(property.getPropertyId());
        assertThat(detailResponse.getPropertyId()).isEqualTo(property.getPropertyId());
        assertThat(detailResponse.getRecentPrice()).isEqualTo(375_000_000L);
        assertThat(documentsResponse.getDocuments()).hasSize(1);
        assertThat(documentsResponse.getDocuments().get(0).getType()).isEqualTo("계약서");
        assertThat(reviewResponse.getContractResult()).isEqualTo(ContractResult.SAFE);
        assertThat(reviewResponse.getTrapsDetected()).isEqualTo(2);
        assertThat(reviewResponse.getTrapsCorrectlyIdentified()).isEqualTo(2);
        assertThat(validationResponse.isPassed()).isTrue();
        assertThat(validationResponse.getPropertyId()).isEqualTo(property.getPropertyId());
        assertThat(validationResponse.getPurchasePrice()).isEqualTo(375_000_000L);
        assertThat(validationResponse.getHousingType()).isEqualTo(HousingType.OWNED_APT);
    }

    @DisplayName("critical trap을 놓치면 계약 검토가 실패하고 구매 검증이 막힌다.")
    @Test
    void failedReviewBlocksPurchaseValidation() {
        // given
        final RealEstateProperty property = realEstatePropertyRepository.saveAndFlush(createProperty(
            "PROP-FLOW-FAIL",
            "실패 플로우 매물",
            BigDecimal.valueOf(37.5100),
            BigDecimal.valueOf(127.0400),
            List.of(
                createTrap("TRAP-CRITICAL", "ALL_DEPOSIT_LOST", 50),
                createTrap("TRAP-NORMAL", "-1000000", 10)
            )
        ));
        realEstateDocumentRepository.saveAndFlush(createDocument(
            property.getPropertyId(),
            RealEstateDocumentType.CONTRACT,
            "/images/docs/contract-fail.png",
            List.of(
                RealEstateDocumentChecklistItem.create("TRAP-CRITICAL", "보증금 전액 손실 위험 확인", true),
                RealEstateDocumentChecklistItem.create("TRAP-NORMAL", "근저당 확인", true)
            )
        ));
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(property.getPropertyId()));

        // when
        final WorldContractReviewSubmitServiceResponse reviewResponse =
            worldContractReviewSubmitService.submit(
                WorldContractReviewSubmitServiceRequest.of(
                    gameSession.getGameSessionId(),
                    property.getPropertyId(),
                    List.of("TRAP-NORMAL")
                )
            );
        entityManager.flush();
        entityManager.clear();

        final GameContractReview latestReview = gameContractReviewRepository
            .findTopByGameSessionIdAndPropertyIdOrderByReviewedAtDescIdDesc(
                gameSession.getGameSessionId(),
                property.getPropertyId()
            )
            .orElseThrow();
        final WorldPurchaseValidationServiceResponse validationResponse =
            worldPurchaseValidationService.validate(
                WorldPurchaseValidationServiceRequest.of(
                    gameSession.getGameSessionId(),
                    property.getPropertyId()
                )
            );

        // then
        assertThat(reviewResponse.getContractResult()).isEqualTo(ContractResult.FAIL);
        assertThat(latestReview.getReviewStatus()).isEqualTo(ContractReviewStatus.FAILED);
        assertThat(latestReview.getDetectedTraps()).containsExactly("TRAP-NORMAL");
        assertThat(validationResponse.isPassed()).isFalse();
        assertThat(validationResponse.getFailureCode()).isEqualTo(PurchaseValidationFailureCode.CONTRACT_REVIEW_FAILED);
    }

    @DisplayName("존재하지 않는 propertyId면 상세 조회 단계에서 플로우가 즉시 중단된다.")
    @Test
    void nonexistentPropertyStopsFlowAtDetailLookup() {
        // when & then
        assertThatThrownBy(() -> worldRealEstatePropertyProviderService.getPropertyDetail(9999L))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.HOUSING_PROPERTY_NOT_FOUND);
    }

    private RealEstatePropertyBoundsServiceRequest boundsAround(final RealEstateProperty property) {
        return RealEstatePropertyBoundsServiceRequest.of(
            property.getLatitude().subtract(BigDecimal.valueOf(0.0100)),
            property.getLongitude().subtract(BigDecimal.valueOf(0.0100)),
            property.getLatitude().add(BigDecimal.valueOf(0.0100)),
            property.getLongitude().add(BigDecimal.valueOf(0.0100))
        );
    }

    private GameSession createGameSession(final Long targetPropertyId) {
        return GameSession.create(
            1L,
            1,
            "홍길동",
            CharacterType.MALE,
            JobType.SMALL_BIZ,
            HousingType.STUDIO,
            "11",
            "11680",
            targetPropertyId,
            DataSourceType.MY_DATA
        );
    }

    private RealEstateProperty createProperty(
        final String providerId,
        final String propertyName,
        final BigDecimal latitude,
        final BigDecimal longitude,
        final List<ContractTrap> contractTraps
    ) {
        return RealEstateProperty.create(
            providerId,
            propertyName,
            "서울특별시 강남구 테헤란로 101",
            "11",
            "11680",
            Money.of(375_000_000L),
            latitude,
            longitude,
            HousingType.OWNED_APT,
            contractTraps
        );
    }

    private RealEstateDocument createDocument(
        final Long propertyId,
        final RealEstateDocumentType documentType,
        final String imageUrl,
        final List<RealEstateDocumentChecklistItem> checklist
    ) {
        return RealEstateDocument.create(
            propertyId,
            documentType,
            RealEstateRegistrySection.GAPGU,
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
                        "부동산 플로우 테스트용 문서",
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

    private ContractTrap createTrap(
        final String trapId,
        final String cash,
        final int stress
    ) {
        return ContractTrap.create(
            trapId,
            "TYPE",
            "CONTRACT",
            "설명",
            ContractTrapPenalty.create(cash, stress)
        );
    }
}
