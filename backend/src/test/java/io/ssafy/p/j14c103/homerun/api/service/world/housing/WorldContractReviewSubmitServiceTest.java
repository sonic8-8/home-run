package io.ssafy.p.j14c103.homerun.api.service.world.housing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.WorldContractReviewSubmitServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.WorldContractReviewSubmitServiceResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
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
import java.time.LocalDateTime;
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
class WorldContractReviewSubmitServiceTest {

    @Autowired
    private WorldContractReviewSubmitService worldContractReviewSubmitService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private RealEstatePropertyRepository realEstatePropertyRepository;

    @Autowired
    private RealEstateDocumentRepository realEstateDocumentRepository;

    @Autowired
    private GameContractReviewRepository gameContractReviewRepository;

    @Autowired
    private EntityManager entityManager;

    @DisplayName("정상 제출 시 계약 검토 결과를 반환하고 리뷰를 저장한다.")
    @Test
    void submit() {
        final RealEstateProperty property = realEstatePropertyRepository.saveAndFlush(createProperty("PROP-REVIEW-001"));
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(property.getPropertyId()));
        realEstateDocumentRepository.saveAndFlush(createDocument(property.getPropertyId()));

        final WorldContractReviewSubmitServiceResponse response =
            worldContractReviewSubmitService.submit(
                WorldContractReviewSubmitServiceRequest.of(
                    gameSession.getGameSessionId(),
                    property.getPropertyId(),
                    List.of("TRAP-01")
                )
            );
        entityManager.flush();
        entityManager.clear();

        final GameContractReview savedReview = gameContractReviewRepository
            .findTopByGameSessionIdAndPropertyIdOrderByReviewedAtDescIdDesc(
                gameSession.getGameSessionId(),
                property.getPropertyId()
            )
            .orElseThrow();

        assertThat(response.getContractResult()).isEqualTo(ContractResult.WARNING);
        assertThat(response.getTrapsDetected()).isEqualTo(1);
        assertThat(response.getTrapsCorrectlyIdentified()).isEqualTo(1);
        assertThat(response.getMessage()).isEqualTo("주의가 필요한 항목이 있습니다.");
        assertThat(savedReview.getReviewStatus()).isEqualTo(ContractReviewStatus.PASSED);
        assertThat(savedReview.getCheckedTraps()).containsExactly("TRAP-01");
        assertThat(savedReview.getDetectedTraps()).containsExactly("TRAP-01");
    }

    @DisplayName("노출되지 않은 trapId를 제출하면 INVALID_INPUT_VALUE 예외가 발생한다.")
    @Test
    void submitWithUnknownTrapId() {
        final RealEstateProperty property = realEstatePropertyRepository.saveAndFlush(createProperty("PROP-REVIEW-002"));
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(property.getPropertyId()));
        realEstateDocumentRepository.saveAndFlush(createDocument(property.getPropertyId()));

        assertThatThrownBy(() -> worldContractReviewSubmitService.submit(
            WorldContractReviewSubmitServiceRequest.of(
                gameSession.getGameSessionId(),
                property.getPropertyId(),
                List.of("TRAP-99")
            )
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @DisplayName("존재하지 않는 세션이면 WORLD_SESSION_NOT_FOUND 예외가 발생한다.")
    @Test
    void submitWithUnknownSession() {
        final RealEstateProperty property = realEstatePropertyRepository.saveAndFlush(createProperty("PROP-REVIEW-UNKNOWN-SESSION"));

        assertThatThrownBy(() -> worldContractReviewSubmitService.submit(
            WorldContractReviewSubmitServiceRequest.of(
                9999L,
                property.getPropertyId(),
                List.of("TRAP-01")
            )
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.WORLD_SESSION_NOT_FOUND);
    }

    @DisplayName("세션의 목표 매물과 요청 매물이 다르면 HOUSING_CONTRACT_REVIEW_TARGET_MISMATCH 예외가 발생한다.")
    @Test
    void submitWithTargetPropertyMismatch() {
        final RealEstateProperty targetProperty = realEstatePropertyRepository.saveAndFlush(createProperty("PROP-REVIEW-TARGET"));
        final RealEstateProperty anotherProperty = realEstatePropertyRepository.saveAndFlush(createProperty("PROP-REVIEW-OTHER"));
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(targetProperty.getPropertyId()));
        realEstateDocumentRepository.saveAndFlush(createDocument(anotherProperty.getPropertyId()));

        assertThatThrownBy(() -> worldContractReviewSubmitService.submit(
            WorldContractReviewSubmitServiceRequest.of(
                gameSession.getGameSessionId(),
                anotherProperty.getPropertyId(),
                List.of("TRAP-01")
            )
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.HOUSING_CONTRACT_REVIEW_TARGET_MISMATCH);
    }

    @DisplayName("재제출을 허용하고 최신 리뷰는 reviewedAt desc, id desc 기준으로 조회된다.")
    @Test
    void submitAllowsResubmissionAndLatestReviewWins() {
        final RealEstateProperty property = realEstatePropertyRepository.saveAndFlush(createProperty("PROP-REVIEW-003"));
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(property.getPropertyId()));
        realEstateDocumentRepository.saveAndFlush(createDocument(property.getPropertyId()));

        worldContractReviewSubmitService.submit(
            WorldContractReviewSubmitServiceRequest.of(
                gameSession.getGameSessionId(),
                property.getPropertyId(),
                List.of("TRAP-01")
            )
        );
        worldContractReviewSubmitService.submit(
            WorldContractReviewSubmitServiceRequest.of(
                gameSession.getGameSessionId(),
                property.getPropertyId(),
                List.of("TRAP-01", "TRAP-02")
            )
        );
        entityManager.flush();
        entityManager.clear();

        final List<GameContractReview> reviews = gameContractReviewRepository.findAll();
        final GameContractReview latest = gameContractReviewRepository
            .findTopByGameSessionIdAndPropertyIdOrderByReviewedAtDescIdDesc(
                gameSession.getGameSessionId(),
                property.getPropertyId()
            )
            .orElseThrow();

        assertThat(reviews).hasSize(2);
        assertThat(latest.getCheckedTraps()).containsExactly("TRAP-01", "TRAP-02");
        assertThat(latest.getContractResult()).isEqualTo(ContractResult.SAFE);
    }

    private GameSession createGameSession(final Long targetPropertyId) {
        return GameSession.create(
            1L,
            1,
            "홍길동",
            CharacterType.MALE,
            JobType.SMALL_BIZ,
            HousingType.OWNED_APT,
            "11",
            "11680",
            targetPropertyId,
            DataSourceType.MY_DATA
        );
    }

    private RealEstateProperty createProperty(final String providerId) {
        return RealEstateProperty.create(
            providerId,
            "계약 검토 테스트용 매물",
            "서울특별시 강남구 테헤란로 101",
            "11",
            "11680",
            Money.of(620_000_000L),
            BigDecimal.valueOf(37.5172),
            BigDecimal.valueOf(127.0473),
            HousingType.OWNED_APT,
            List.of(
                createTrap("TRAP-01", "-5000000", 20),
                createTrap("TRAP-02", "-1000000", 10)
            )
        );
    }

    private RealEstateDocument createDocument(final Long propertyId) {
        return RealEstateDocument.create(
            propertyId,
            RealEstateDocumentType.CONTRACT,
            RealEstateRegistrySection.GAPGU,
            "/images/docs/contract.png",
            List.of(
                RealEstateDocumentChecklistItem.create("TRAP-01", "가압류 확인", true),
                RealEstateDocumentChecklistItem.create("TRAP-02", "근저당 확인", true)
            ),
            RealEstateRegistryQuizSample.create(
                "정상",
                List.of(
                    RealEstateRegistryRow.create(
                        "1",
                        "기록사항 없음",
                        "2025년 1월 20일",
                        "없음",
                        "계약 검토 샘플",
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
