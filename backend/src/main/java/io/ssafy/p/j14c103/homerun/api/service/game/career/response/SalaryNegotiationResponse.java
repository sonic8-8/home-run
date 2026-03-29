package io.ssafy.p.j14c103.homerun.api.service.game.career.response;

import io.ssafy.p.j14c103.homerun.api.service.character.career.response.SalaryNegotiationResultResponse;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SalaryNegotiationResponse {

    private final boolean success;
    private final int previousSalary;
    private final int newSalary;
    private final int raiseRate;
    private final int lastNegotiatedTurn;
    private final String message;

    @Builder(access = AccessLevel.PRIVATE)
    private SalaryNegotiationResponse(
        final boolean success,
        final int previousSalary,
        final int newSalary,
        final int raiseRate,
        final int lastNegotiatedTurn,
        final String message
    ) {
        this.success = success;
        this.previousSalary = previousSalary;
        this.newSalary = newSalary;
        this.raiseRate = raiseRate;
        this.lastNegotiatedTurn = lastNegotiatedTurn;
        this.message = message;
    }

    public static SalaryNegotiationResponse of(
        final boolean success,
        final int previousSalary,
        final int newSalary,
        final int raiseRate,
        final int lastNegotiatedTurn,
        final String message
    ) {
        return SalaryNegotiationResponse.builder()
            .success(success)
            .previousSalary(previousSalary)
            .newSalary(newSalary)
            .raiseRate(raiseRate)
            .lastNegotiatedTurn(lastNegotiatedTurn)
            .message(message)
            .build();
    }

    public static SalaryNegotiationResponse from(
        final SalaryNegotiationResultResponse response
    ) {
        if (response == null) {
            throw new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
        }
        return of(
            response.success(),
            response.previousSalary(),
            response.newSalary(),
            response.raiseRate(),
            response.lastNegotiatedTurn(),
            response.message()
        );
    }
}
