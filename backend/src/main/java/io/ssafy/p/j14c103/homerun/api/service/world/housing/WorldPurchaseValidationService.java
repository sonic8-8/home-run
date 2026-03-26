package io.ssafy.p.j14c103.homerun.api.service.world.housing;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.WorldPurchaseValidationServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.PurchaseValidationFailureCode;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.WorldPurchaseValidationServiceResponse;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousing;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousingRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.ContractReviewStatus;
import io.ssafy.p.j14c103.homerun.domain.world.housing.GameContractReview;
import io.ssafy.p.j14c103.homerun.domain.world.housing.GameContractReviewRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateProperty;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WorldPurchaseValidationService {

    private final GameSessionRepository gameSessionRepository;
    private final RealEstatePropertyRepository realEstatePropertyRepository;
    private final GameContractReviewRepository gameContractReviewRepository;
    private final GameHousingRepository gameHousingRepository;

    public WorldPurchaseValidationServiceResponse validate(
        final WorldPurchaseValidationServiceRequest request
    ) {
        validateRequest(request);

        final GameSession gameSession = gameSessionRepository.findById(request.getSessionId())
            .orElseThrow(() -> new HomerunException(ErrorCode.WORLD_SESSION_NOT_FOUND));
        final Optional<RealEstateProperty> property = realEstatePropertyRepository.findById(request.getPropertyId());
        if (property.isEmpty()) {
            return WorldPurchaseValidationServiceResponse.failure(PurchaseValidationFailureCode.PROPERTY_NOT_FOUND);
        }
        if (!gameSession.getTargetPropertyId().equals(request.getPropertyId())) {
            return WorldPurchaseValidationServiceResponse.failure(PurchaseValidationFailureCode.TARGET_PROPERTY_MISMATCH);
        }
        if (hasActualOwnedPropertyConflict(gameSession.getGameSessionId(), request.getPropertyId())) {
            return WorldPurchaseValidationServiceResponse.failure(PurchaseValidationFailureCode.ACTUAL_PROPERTY_CONFLICT);
        }

        final Optional<GameContractReview> latestReview = gameContractReviewRepository
            .findTopByGameSessionIdAndPropertyIdOrderByReviewedAtDescIdDesc(
                gameSession.getGameSessionId(),
                request.getPropertyId()
            );
        if (latestReview.isEmpty()) {
            return WorldPurchaseValidationServiceResponse.failure(PurchaseValidationFailureCode.CONTRACT_REVIEW_REQUIRED);
        }
        if (latestReview.orElseThrow().getReviewStatus() != ContractReviewStatus.PASSED) {
            return WorldPurchaseValidationServiceResponse.failure(PurchaseValidationFailureCode.CONTRACT_REVIEW_FAILED);
        }

        final RealEstateProperty foundProperty = property.orElseThrow();
        return WorldPurchaseValidationServiceResponse.success(
            foundProperty.getPropertyId(),
            extractPurchasePrice(foundProperty),
            foundProperty.getHousingType()
        );
    }

    private void validateRequest(final WorldPurchaseValidationServiceRequest request) {
        if (request == null) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private boolean hasActualOwnedPropertyConflict(final Long sessionId, final Long propertyId) {
        final Optional<GameHousing> gameHousing = gameHousingRepository.findByGameSessionId(sessionId);
        if (gameHousing.isEmpty()) {
            return false;
        }

        final GameHousing currentHousing = gameHousing.orElseThrow();
        if (currentHousing.getCurrentHousingType() != HousingType.OWNED_APT) {
            return false;
        }

        return currentHousing.getCurrentPropertyId() != null
            && !currentHousing.getCurrentPropertyId().equals(propertyId);
    }

    private long extractPurchasePrice(final RealEstateProperty property) {
        if (property.getBasePrice() != null) {
            return property.getBasePrice().getAmount().longValue();
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }
}
