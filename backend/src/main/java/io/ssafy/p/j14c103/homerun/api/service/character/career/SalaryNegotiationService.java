package io.ssafy.p.j14c103.homerun.api.service.character.career;

import io.ssafy.p.j14c103.homerun.api.service.character.career.request.SalaryNegotiationServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.career.response.SalaryNegotiationResultResponse;
import io.ssafy.p.j14c103.homerun.domain.character.career.SalaryNegotiationPolicy;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.springframework.stereotype.Service;

@Service
public class SalaryNegotiationService {

    private final SalaryNegotiationPolicy salaryNegotiationPolicy;

    public SalaryNegotiationService() {
        this(new SalaryNegotiationPolicy());
    }

    SalaryNegotiationService(final SalaryNegotiationPolicy salaryNegotiationPolicy) {
        if (salaryNegotiationPolicy == null) {
            throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
        }

        this.salaryNegotiationPolicy = salaryNegotiationPolicy;
    }

    public SalaryNegotiationResultResponse negotiate(final SalaryNegotiationServiceRequest request) {
        if (request == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }

        return SalaryNegotiationResultResponse.from(
            salaryNegotiationPolicy.negotiate(
                request.gameCareer(),
                request.gameStat(),
                request.currentTurn(),
                request.toCareerCycleEffect()
            )
        );
    }
}
