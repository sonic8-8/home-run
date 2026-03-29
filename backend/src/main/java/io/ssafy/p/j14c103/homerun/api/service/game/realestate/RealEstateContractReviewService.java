package io.ssafy.p.j14c103.homerun.api.service.game.realestate;

import io.ssafy.p.j14c103.homerun.api.service.game.realestate.request.SubmitContractReviewServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.realestate.response.ContractReviewResponse;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.WorldContractReviewSubmitService;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.WorldPurchaseValidationService;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.WorldContractReviewSubmitServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.WorldPurchaseValidationServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.PurchaseValidationFailureCode;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.WorldContractReviewSubmitServiceResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.WorldPurchaseValidationServiceResponse;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.ContractResult;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RealEstateContractReviewService {

    private static final String CONTRACT_RESULT_SAFE = "SAFE";
    private static final String CONTRACT_RESULT_PARTIAL = "PARTIAL";
    private static final String CONTRACT_RESULT_TRAPPED = "TRAPPED";
    private static final String ACTUAL_PROPERTY_CONFLICT_MESSAGE = "이미 다른 자가 실매물을 보유 중입니다.";

    private final GameSessionRepository gameSessionRepository;
    private final UserAuthContextService userAuthContextService;
    private final WorldContractReviewSubmitService worldContractReviewSubmitService;
    private final WorldPurchaseValidationService worldPurchaseValidationService;

    @Transactional
    public ContractReviewResponse review(
        final Long userId,
        final Long sessionId,
        final Long propertyId,
        final SubmitContractReviewServiceRequest request
    ) {
        userAuthContextService.getContext(userId);
        final GameSession gameSession = getOwnedGameSession(userId, sessionId);

        final WorldContractReviewSubmitServiceResponse reviewResponse = worldContractReviewSubmitService.submit(
            WorldContractReviewSubmitServiceRequest.of(
                gameSession.getGameSessionId(),
                propertyId,
                request.getCheckedTraps()
            )
        );
        final WorldPurchaseValidationServiceResponse validationResponse = worldPurchaseValidationService.validate(
            WorldPurchaseValidationServiceRequest.of(gameSession.getGameSessionId(), propertyId)
        );

        if (validationResponse.isPassed()) {
            return ContractReviewResponse.of(
                true,
                reviewResponse.getTrapsDetected(),
                reviewResponse.getTrapsCorrectlyIdentified(),
                mapContractResult(reviewResponse.getContractResult()),
                reviewResponse.getMessage()
            );
        }
        if (validationResponse.getFailureCode() == PurchaseValidationFailureCode.CONTRACT_REVIEW_FAILED) {
            return ContractReviewResponse.of(
                false,
                reviewResponse.getTrapsDetected(),
                reviewResponse.getTrapsCorrectlyIdentified(),
                mapContractResult(reviewResponse.getContractResult()),
                reviewResponse.getMessage()
            );
        }
        if (validationResponse.getFailureCode() == PurchaseValidationFailureCode.ACTUAL_PROPERTY_CONFLICT) {
            return ContractReviewResponse.of(
                false,
                reviewResponse.getTrapsDetected(),
                reviewResponse.getTrapsCorrectlyIdentified(),
                mapContractResult(reviewResponse.getContractResult()),
                ACTUAL_PROPERTY_CONFLICT_MESSAGE
            );
        }

        throw new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
    }

    private GameSession getOwnedGameSession(final Long userId, final Long sessionId) {
        final GameSession gameSession = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.GAME_SESSION_NOT_FOUND));
        gameSession.assertOwner(userId);
        gameSession.assertInProgress();
        return gameSession;
    }

    private String mapContractResult(final ContractResult contractResult) {
        if (contractResult == ContractResult.SAFE) {
            return CONTRACT_RESULT_SAFE;
        }
        if (contractResult == ContractResult.WARNING) {
            return CONTRACT_RESULT_PARTIAL;
        }
        if (contractResult == ContractResult.FAIL) {
            return CONTRACT_RESULT_TRAPPED;
        }

        throw new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
    }
}
