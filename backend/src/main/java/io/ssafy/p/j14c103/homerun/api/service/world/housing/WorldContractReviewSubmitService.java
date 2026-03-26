package io.ssafy.p.j14c103.homerun.api.service.world.housing;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.WorldContractReviewSubmitServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.response.WorldContractReviewSubmitServiceResponse;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.ContractReviewCalculationResult;
import io.ssafy.p.j14c103.homerun.domain.world.housing.ContractReviewPolicy;
import io.ssafy.p.j14c103.homerun.domain.world.housing.ContractResult;
import io.ssafy.p.j14c103.homerun.domain.world.housing.GameContractReview;
import io.ssafy.p.j14c103.homerun.domain.world.housing.GameContractReviewRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocument;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocumentChecklistItem;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateDocumentRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateProperty;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class WorldContractReviewSubmitService {

    private static final String SAFE_MESSAGE = "서류 검토를 통과했습니다.";
    private static final String WARNING_MESSAGE = "주의가 필요한 항목이 있습니다.";
    private static final String FAIL_MESSAGE = "위험한 계약입니다.";

    private final GameSessionRepository gameSessionRepository;
    private final RealEstatePropertyRepository realEstatePropertyRepository;
    private final RealEstateDocumentRepository realEstateDocumentRepository;
    private final GameContractReviewRepository gameContractReviewRepository;

    public WorldContractReviewSubmitServiceResponse submit(
        final WorldContractReviewSubmitServiceRequest request
    ) {
        validateRequest(request);

        final GameSession gameSession = gameSessionRepository.findById(request.getSessionId())
            .orElseThrow(() -> new HomerunException(ErrorCode.WORLD_SESSION_NOT_FOUND));
        validateSessionTargetProperty(gameSession, request.getPropertyId());

        final RealEstateProperty property = realEstatePropertyRepository.findById(request.getPropertyId())
            .orElseThrow(() -> new HomerunException(ErrorCode.HOUSING_PROPERTY_NOT_FOUND));
        final List<String> exposedChecklistTrapIds = loadChecklistTrapUniverse(property.getPropertyId());
        final ContractReviewCalculationResult calculationResult = new ContractReviewPolicy().calculate(
            request.getCheckedTraps(),
            exposedChecklistTrapIds,
            property.getContractTraps() == null ? List.of() : property.getContractTraps()
        );

        gameContractReviewRepository.save(
            GameContractReview.create(
                gameSession.getGameSessionId(),
                property.getPropertyId(),
                calculationResult.getReviewStatus(),
                calculationResult.getCheckedTraps(),
                calculationResult.getDetectedTraps(),
                calculationResult.getContractResult(),
                LocalDateTime.now()
            )
        );

        return WorldContractReviewSubmitServiceResponse.of(
            calculationResult.getTrapsDetected(),
            calculationResult.getTrapsCorrectlyIdentified(),
            calculationResult.getContractResult(),
            mapMessage(calculationResult.getContractResult())
        );
    }

    private void validateRequest(final WorldContractReviewSubmitServiceRequest request) {
        if (request == null) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateSessionTargetProperty(
        final GameSession gameSession,
        final Long propertyId
    ) {
        if (gameSession.getTargetPropertyId().equals(propertyId)) {
            return;
        }

        throw new HomerunException(ErrorCode.HOUSING_CONTRACT_REVIEW_TARGET_MISMATCH);
    }

    private List<String> loadChecklistTrapUniverse(final Long propertyId) {
        final LinkedHashSet<String> exposedChecklistTrapIds = realEstateDocumentRepository
            .findAllByPropertyIdOrderByRealEstateDocumentIdAsc(propertyId)
            .stream()
            .map(RealEstateDocument::getChecklist)
            .filter(checklist -> checklist != null && !checklist.isEmpty())
            .flatMap(List::stream)
            .map(RealEstateDocumentChecklistItem::getTrapId)
            .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        if (exposedChecklistTrapIds.isEmpty()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }

        return List.copyOf(exposedChecklistTrapIds);
    }

    private String mapMessage(final ContractResult contractResult) {
        if (contractResult == ContractResult.SAFE) {
            return SAFE_MESSAGE;
        }
        if (contractResult == ContractResult.WARNING) {
            return WARNING_MESSAGE;
        }
        if (contractResult == ContractResult.FAIL) {
            return FAIL_MESSAGE;
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }
}
