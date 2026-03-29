package io.ssafy.p.j14c103.homerun.api.service.game.realestate.response;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.Getter;

@Getter
public class ContractReviewResponse {

    private final boolean success;
    private final int trapsDetected;
    private final int trapsCorrectlyIdentified;
    private final String contractResult;
    private final String message;

    private ContractReviewResponse(
        final boolean success,
        final int trapsDetected,
        final int trapsCorrectlyIdentified,
        final String contractResult,
        final String message
    ) {
        validateCounts(trapsDetected, trapsCorrectlyIdentified);
        validateText(contractResult);
        validateText(message);

        this.success = success;
        this.trapsDetected = trapsDetected;
        this.trapsCorrectlyIdentified = trapsCorrectlyIdentified;
        this.contractResult = contractResult;
        this.message = message;
    }

    public static ContractReviewResponse of(
        final boolean success,
        final int trapsDetected,
        final int trapsCorrectlyIdentified,
        final String contractResult,
        final String message
    ) {
        return new ContractReviewResponse(
            success,
            trapsDetected,
            trapsCorrectlyIdentified,
            contractResult,
            message
        );
    }

    public boolean isSuccess() {
        return success;
    }

    private void validateCounts(final int trapsDetected, final int trapsCorrectlyIdentified) {
        if (trapsDetected < 0 || trapsCorrectlyIdentified < 0) {
            throw new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
        }
    }

    private void validateText(final String value) {
        if (value == null || value.isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
        }
    }
}
