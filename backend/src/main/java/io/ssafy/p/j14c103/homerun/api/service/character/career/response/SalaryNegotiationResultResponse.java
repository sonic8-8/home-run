package io.ssafy.p.j14c103.homerun.api.service.character.career.response;

import io.ssafy.p.j14c103.homerun.domain.character.career.SalaryNegotiationPolicy;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;

public record SalaryNegotiationResultResponse(
    boolean success,
    int previousSalary,
    int newSalary,
    int raiseRate,
    int lastNegotiatedTurn,
    String message
) {

    public SalaryNegotiationResultResponse {
        validateNonNegative(previousSalary);
        validateNonNegative(newSalary);
        validateNonNegative(raiseRate);
        validateNonNegative(lastNegotiatedTurn);
        if (message == null || message.isBlank()) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
    }

    public static SalaryNegotiationResultResponse from(
        final SalaryNegotiationPolicy.NegotiationResult negotiationResult
    ) {
        if (negotiationResult == null) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }

        return new SalaryNegotiationResultResponse(
            true,
            negotiationResult.previousSalary(),
            negotiationResult.newSalary(),
            negotiationResult.raiseRate(),
            negotiationResult.lastNegotiatedTurn(),
            negotiationResult.message()
        );
    }

    private static void validateNonNegative(final int value) {
        if (value < 0) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
    }
}
