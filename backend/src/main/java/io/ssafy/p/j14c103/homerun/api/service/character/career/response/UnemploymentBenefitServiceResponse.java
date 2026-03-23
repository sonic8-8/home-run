package io.ssafy.p.j14c103.homerun.api.service.character.career.response;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;

public record UnemploymentBenefitServiceResponse(
    boolean benefitGranted,
    int benefitAmount,
    int remainingUnemploymentBenefitTurns,
    String message
) {

    public UnemploymentBenefitServiceResponse {
        if (benefitAmount < 0 || remainingUnemploymentBenefitTurns < 0) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        if (benefitGranted && benefitAmount == 0) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        if (!benefitGranted && benefitAmount != 0) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        if (message == null || message.isBlank()) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
    }

    public static UnemploymentBenefitServiceResponse of(
        final boolean benefitGranted,
        final int benefitAmount,
        final int remainingUnemploymentBenefitTurns,
        final String message
    ) {
        return new UnemploymentBenefitServiceResponse(
            benefitGranted,
            benefitAmount,
            remainingUnemploymentBenefitTurns,
            message
        );
    }
}
