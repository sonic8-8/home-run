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

    private final ForcedResignationPolicy forcedResignationPolicy = new ForcedResignationPolicy();

    public ForcedResignationServiceResponse forceResign(
        final ForcedResignationServiceRequest request
    ) {
        if (request == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }

        final ForcedResignationPolicy.ForcedResignationResult result =
            forcedResignationPolicy.apply(
                request.getGameCareer(),
                request.getGameStat(),
                request.getCurrentTurn()
            );

        request.getGameCareer().forceResign(result);
        return ForcedResignationServiceResponse.of(
            true,
            request.getGameCareer().getEmploymentStatus(),
            request.getGameCareer().getRehireAvailableTurn(),
            request.getGameCareer().getRemainingUnemploymentBenefitTurns(),
            request.getGameCareer().getSalaryBeforeResignation(),
            FORCED_RESIGNATION_MESSAGE
        );
    }
}
