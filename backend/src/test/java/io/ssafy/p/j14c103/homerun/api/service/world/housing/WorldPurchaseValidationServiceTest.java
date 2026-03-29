package io.ssafy.p.j14c103.homerun.api.service.world.housing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.WorldPurchaseValidationServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.PurchaseValidationFailureCode;
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
import io.ssafy.p.j14c103.homerun.domain.world.housing.GameContractReview;
import io.ssafy.p.j14c103.homerun.domain.world.housing.GameContractReviewRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateProperty;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class WorldPurchaseValidationServiceTest extends IntegrationTestSupport {

    @Autowired
    private WorldPurchaseValidationService worldPurchaseValidationService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private RealEstatePropertyRepository realEstatePropertyRepository;

    @Autowired
    private GameContractReviewRepository gameContractReviewRepository;

    @Autowired
    private GameHousingRepository gameHousingRepository;

    @DisplayName("존재하지 않는 매물이면 PROPERTY_NOT_FOUND 실패를 반환한다.")
    @Test
    void validateWithUnknownProperty() {
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(101L));

        final WorldPurchaseValidationServiceResponse response = worldPurchaseValidationService.validate(
            WorldPurchaseValidationServiceRequest.of(gameSession.getGameSessionId(), 9999L)
        );

        assertThat(response.isPassed()).isFalse();
        assertThat(response.getFailureCode()).isEqualTo(PurchaseValidationFailureCode.PROPERTY_NOT_FOUND);
    }

    @DisplayName("최신 계약 검토가 없으면 CONTRACT_REVIEW_REQUIRED 실패를 반환한다.")
    @Test
    void validateWithoutReview() {
        final RealEstateProperty property = realEstatePropertyRepository.saveAndFlush(createProperty("PROP-PURCHASE-001", 101L));
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(property.getPropertyId()));

        final WorldPurchaseValidationServiceResponse response = worldPurchaseValidationService.validate(
            WorldPurchaseValidationServiceRequest.of(gameSession.getGameSessionId(), property.getPropertyId())
        );

        assertThat(response.isPassed()).isFalse();
        assertThat(response.getFailureCode()).isEqualTo(PurchaseValidationFailureCode.CONTRACT_REVIEW_REQUIRED);
    }

    @DisplayName("최신 계약 검토가 FAILED면 CONTRACT_REVIEW_FAILED 실패를 반환한다.")
    @Test
    void validateWithFailedReview() {
        final RealEstateProperty property = realEstatePropertyRepository.saveAndFlush(createProperty("PROP-PURCHASE-002", 102L));
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(property.getPropertyId()));
        gameContractReviewRepository.saveAndFlush(createReview(
            gameSession.getGameSessionId(),
            property.getPropertyId(),
            ContractReviewStatus.FAILED,
            ContractResult.FAIL,
            LocalDateTime.of(2026, 3, 26, 10, 0)
        ));

        final WorldPurchaseValidationServiceResponse response = worldPurchaseValidationService.validate(
            WorldPurchaseValidationServiceRequest.of(gameSession.getGameSessionId(), property.getPropertyId())
        );

        assertThat(response.isPassed()).isFalse();
        assertThat(response.getFailureCode()).isEqualTo(PurchaseValidationFailureCode.CONTRACT_REVIEW_FAILED);
    }

    @DisplayName("목표 매물과 요청 매물이 다르면 TARGET_PROPERTY_MISMATCH 실패를 반환한다.")
    @Test
    void validateWithTargetPropertyMismatch() {
        final RealEstateProperty targetProperty = realEstatePropertyRepository.saveAndFlush(createProperty("PROP-PURCHASE-TARGET", 103L));
        final RealEstateProperty requestedProperty = realEstatePropertyRepository.saveAndFlush(createProperty("PROP-PURCHASE-REQ", 104L));
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(targetProperty.getPropertyId()));

        final WorldPurchaseValidationServiceResponse response = worldPurchaseValidationService.validate(
            WorldPurchaseValidationServiceRequest.of(gameSession.getGameSessionId(), requestedProperty.getPropertyId())
        );

        assertThat(response.isPassed()).isFalse();
        assertThat(response.getFailureCode()).isEqualTo(PurchaseValidationFailureCode.TARGET_PROPERTY_MISMATCH);
    }

    @DisplayName("이미 다른 자가 실매물을 보유 중이면 ACTUAL_PROPERTY_CONFLICT 실패를 반환한다.")
    @Test
    void validateWithActualPropertyConflict() {
        final RealEstateProperty targetProperty = realEstatePropertyRepository.saveAndFlush(createProperty("PROP-PURCHASE-105", 105L));
        final RealEstateProperty ownedProperty = realEstatePropertyRepository.saveAndFlush(createProperty("PROP-PURCHASE-106", 106L));
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(targetProperty.getPropertyId()));
        gameHousingRepository.saveAndFlush(
            GameHousing.create(
                gameSession.getGameSessionId(),
                HousingType.OWNED_APT,
                Money.of(0L),
                Money.zero(),
                Money.of(200_000L),
                ownedProperty.getPropertyId()
            )
        );
        gameContractReviewRepository.saveAndFlush(createReview(
            gameSession.getGameSessionId(),
            targetProperty.getPropertyId(),
            ContractReviewStatus.PASSED,
            ContractResult.WARNING,
            LocalDateTime.of(2026, 3, 26, 10, 0)
        ));

        final WorldPurchaseValidationServiceResponse response = worldPurchaseValidationService.validate(
            WorldPurchaseValidationServiceRequest.of(gameSession.getGameSessionId(), targetProperty.getPropertyId())
        );

        assertThat(response.isPassed()).isFalse();
        assertThat(response.getFailureCode()).isEqualTo(PurchaseValidationFailureCode.ACTUAL_PROPERTY_CONFLICT);
    }

    @DisplayName("최신 리뷰가 PASSED면 WARNING 결과여도 구매 검증을 통과한다.")
    @Test
    void validateSuccessWhenReviewPassed() {
        final RealEstateProperty property = realEstatePropertyRepository.saveAndFlush(createProperty("PROP-PURCHASE-107", 107L));
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
        gameContractReviewRepository.saveAndFlush(createReview(
            gameSession.getGameSessionId(),
            property.getPropertyId(),
            ContractReviewStatus.PASSED,
            ContractResult.WARNING,
            LocalDateTime.of(2026, 3, 26, 10, 0)
        ));

        final WorldPurchaseValidationServiceResponse response = worldPurchaseValidationService.validate(
            WorldPurchaseValidationServiceRequest.of(gameSession.getGameSessionId(), property.getPropertyId())
        );

        assertThat(response.isPassed()).isTrue();
        assertThat(response.getFailureCode()).isNull();
        assertThat(response.getPropertyId()).isEqualTo(property.getPropertyId());
        assertThat(response.getPurchasePrice()).isEqualTo(375_000_000L);
        assertThat(response.getHousingType()).isEqualTo(HousingType.OWNED_APT);
    }

    @DisplayName("존재하지 않는 세션이면 WORLD_SESSION_NOT_FOUND 예외가 발생한다.")
    @Test
    void validateWithUnknownSession() {
        assertThatThrownBy(() -> worldPurchaseValidationService.validate(
            WorldPurchaseValidationServiceRequest.of(9999L, 101L)
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.WORLD_SESSION_NOT_FOUND);
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

    private RealEstateProperty createProperty(final String providerId, final long providerSuffix) {
        return RealEstateProperty.create(
            providerId,
            "구매 검증 테스트 매물-" + providerSuffix,
            "서울특별시 강남구 테헤란로 " + providerSuffix,
            "11",
            "11680",
            Money.of(375_000_000L),
            BigDecimal.valueOf(37.5172),
            BigDecimal.valueOf(127.0473),
            HousingType.OWNED_APT,
            List.of()
        );
    }

    private GameContractReview createReview(
        final Long sessionId,
        final Long propertyId,
        final ContractReviewStatus reviewStatus,
        final ContractResult contractResult,
        final LocalDateTime reviewedAt
    ) {
        return GameContractReview.create(
            sessionId,
            propertyId,
            reviewStatus,
            List.of("TRAP-01"),
            List.of("TRAP-01"),
            contractResult,
            reviewedAt
        );
    }
}
