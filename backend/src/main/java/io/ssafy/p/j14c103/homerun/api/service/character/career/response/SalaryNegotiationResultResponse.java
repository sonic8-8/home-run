package io.ssafy.p.j14c103.homerun.api.service.character.career.response;

import io.ssafy.p.j14c103.homerun.domain.character.career.SalaryNegotiationPolicy;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SalaryNegotiationResultResponse {

    private final boolean success;
    private final int previousSalary;
    private final int newSalary;
    private final int raiseRate;
    private final int lastNegotiatedTurn;
    private final String message;

    @Builder(access = AccessLevel.PRIVATE)
    private SalaryNegotiationResultResponse(
        final boolean success,
        final int previousSalary,
        final int newSalary,
        final int raiseRate,
        final int lastNegotiatedTurn,
        final String message
    ) {
        validateRequest(previousSalary, newSalary, raiseRate, lastNegotiatedTurn, message);

        this.success = success;
        this.previousSalary = previousSalary;
        this.newSalary = newSalary;
        this.raiseRate = raiseRate;
        this.lastNegotiatedTurn = lastNegotiatedTurn;
        this.message = message;
    }

    public static SalaryNegotiationResultResponse from(
        final SalaryNegotiationPolicy.NegotiationResult negotiationResult
    ) {
        if (negotiationResult == null) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }

        return SalaryNegotiationResultResponse.builder()
            .success(true)
            .previousSalary(negotiationResult.previousSalary())
            .newSalary(negotiationResult.newSalary())
            .raiseRate(negotiationResult.raiseRate())
            .lastNegotiatedTurn(negotiationResult.lastNegotiatedTurn())
            .message(negotiationResult.message())
            .build();
    }

    private void validateRequest(
        final int previousSalary,
        final int newSalary,
        final int raiseRate,
        final int lastNegotiatedTurn,
        final String message
    ) {
        validateNonNegative(previousSalary);
        validateNonNegative(newSalary);
        validateNonNegative(raiseRate);
        validateNonNegative(lastNegotiatedTurn);
        if (message == null || message.isBlank()) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
    }

    private void validateNonNegative(final int value) {
        if (value < 0) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
    }
}
