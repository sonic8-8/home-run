package io.ssafy.p.j14c103.homerun.api.service.world.housing.response;

import io.ssafy.p.j14c103.homerun.domain.world.housing.ContractResult;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.Getter;

@Getter
public class WorldContractReviewSubmitServiceResponse {

    private final int trapsDetected;
    private final int trapsCorrectlyIdentified;
    private final ContractResult contractResult;
    private final String message;

    private WorldContractReviewSubmitServiceResponse(
        final int trapsDetected,
        final int trapsCorrectlyIdentified,
        final ContractResult contractResult,
        final String message
    ) {
        validateCounts(trapsDetected, trapsCorrectlyIdentified);
        validateContractResult(contractResult);
        validateMessage(message);

        this.trapsDetected = trapsDetected;
        this.trapsCorrectlyIdentified = trapsCorrectlyIdentified;
        this.contractResult = contractResult;
        this.message = message;
    }

    public static WorldContractReviewSubmitServiceResponse of(
        final int trapsDetected,
        final int trapsCorrectlyIdentified,
        final ContractResult contractResult,
        final String message
    ) {
        return new WorldContractReviewSubmitServiceResponse(
            trapsDetected,
            trapsCorrectlyIdentified,
            contractResult,
            message
        );
    }

    private void validateCounts(final int trapsDetected, final int trapsCorrectlyIdentified) {
        if (trapsDetected < 0 || trapsCorrectlyIdentified < 0) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private void validateContractResult(final ContractResult contractResult) {
        if (contractResult == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private void validateMessage(final String message) {
        if (message == null || message.isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }
}
