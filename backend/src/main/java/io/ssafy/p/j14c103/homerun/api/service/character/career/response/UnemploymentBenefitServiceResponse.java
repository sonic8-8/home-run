package io.ssafy.p.j14c103.homerun.api.service.character.career.response;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UnemploymentBenefitServiceResponse {

    private final boolean benefitGranted;
    private final int benefitAmount;
    private final int remainingUnemploymentBenefitTurns;
    private final String message;

    @Builder(access = AccessLevel.PRIVATE)
    private UnemploymentBenefitServiceResponse(
        final boolean benefitGranted,
        final int benefitAmount,
        final int remainingUnemploymentBenefitTurns,
        final String message
    ) {
        validateRequest(
            benefitGranted,
            benefitAmount,
            remainingUnemploymentBenefitTurns,
            message
        );

        this.benefitGranted = benefitGranted;
        this.benefitAmount = benefitAmount;
        this.remainingUnemploymentBenefitTurns = remainingUnemploymentBenefitTurns;
        this.message = message;
    }

    public static UnemploymentBenefitServiceResponse of(
        final boolean benefitGranted,
        final int benefitAmount,
        final int remainingUnemploymentBenefitTurns,
        final String message
    ) {
        return UnemploymentBenefitServiceResponse.builder()
            .benefitGranted(benefitGranted)
            .benefitAmount(benefitAmount)
            .remainingUnemploymentBenefitTurns(remainingUnemploymentBenefitTurns)
            .message(message)
            .build();
    }

    private void validateRequest(
        final boolean benefitGranted,
        final int benefitAmount,
        final int remainingUnemploymentBenefitTurns,
        final String message
    ) {
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
}
