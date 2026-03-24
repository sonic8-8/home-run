package io.ssafy.p.j14c103.homerun.api.service.character.career;

import io.ssafy.p.j14c103.homerun.api.service.character.career.request.ForcedResignationServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.career.response.ForcedResignationServiceResponse;
import io.ssafy.p.j14c103.homerun.domain.character.career.ForcedResignationPolicy;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.springframework.stereotype.Service;

@Service
public class ForcedResignationService {

    private static final String FORCED_RESIGNATION_MESSAGE = "건강 악화로 강제 퇴사했습니다.";

    private final ForcedResignationPolicy forcedResignationPolicy;

    public ForcedResignationService() {
        this(new ForcedResignationPolicy());
    }

    ForcedResignationService(final ForcedResignationPolicy forcedResignationPolicy) {
        if (forcedResignationPolicy == null) {
            throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
        }

        this.forcedResignationPolicy = forcedResignationPolicy;
    }

    public ForcedResignationServiceResponse forceResign(
        final ForcedResignationServiceRequest request
    ) {
        if (request == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }

        final ForcedResignationPolicy.ForcedResignationResult result =
            forcedResignationPolicy.apply(
                request.gameCareer(),
                request.gameStat(),
                request.currentTurn()
            );

        request.gameCareer().forceResign(result);
        return ForcedResignationServiceResponse.of(
            true,
            request.gameCareer().getEmploymentStatus(),
            request.gameCareer().getRehireAvailableTurn(),
            request.gameCareer().getRemainingUnemploymentBenefitTurns(),
            request.gameCareer().getSalaryBeforeResignation(),
            FORCED_RESIGNATION_MESSAGE
        );
    }
}
