package io.ssafy.p.j14c103.homerun.domain.world.housing;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.Getter;

@Getter
public class ContractReviewCalculationResult {

    private final List<String> checkedTraps;
    private final List<String> detectedTraps;
    private final int trapsDetected;
    private final int trapsCorrectlyIdentified;
    private final ContractResult contractResult;
    private final ContractReviewStatus reviewStatus;
    private final List<ContractTrap> matchedActualTraps;

    private ContractReviewCalculationResult(
        final List<String> checkedTraps,
        final List<String> detectedTraps,
        final int trapsDetected,
        final int trapsCorrectlyIdentified,
        final ContractResult contractResult,
        final ContractReviewStatus reviewStatus,
        final List<ContractTrap> matchedActualTraps
    ) {
        validateLists(checkedTraps, detectedTraps, matchedActualTraps);
        validateCounts(trapsDetected, trapsCorrectlyIdentified);
        validateEnums(contractResult, reviewStatus);

        this.checkedTraps = List.copyOf(checkedTraps);
        this.detectedTraps = List.copyOf(detectedTraps);
        this.trapsDetected = trapsDetected;
        this.trapsCorrectlyIdentified = trapsCorrectlyIdentified;
        this.contractResult = contractResult;
        this.reviewStatus = reviewStatus;
        this.matchedActualTraps = List.copyOf(matchedActualTraps);
    }

    public static ContractReviewCalculationResult of(
        final List<String> checkedTraps,
        final List<String> detectedTraps,
        final int trapsDetected,
        final int trapsCorrectlyIdentified,
        final ContractResult contractResult,
        final ContractReviewStatus reviewStatus,
        final List<ContractTrap> matchedActualTraps
    ) {
        return new ContractReviewCalculationResult(
            checkedTraps,
            detectedTraps,
            trapsDetected,
            trapsCorrectlyIdentified,
            contractResult,
            reviewStatus,
            matchedActualTraps
        );
    }

    private void validateLists(
        final List<String> checkedTraps,
        final List<String> detectedTraps,
        final List<ContractTrap> matchedActualTraps
    ) {
        if (checkedTraps == null || detectedTraps == null || matchedActualTraps == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private void validateCounts(final int trapsDetected, final int trapsCorrectlyIdentified) {
        if (trapsDetected < 0 || trapsCorrectlyIdentified < 0) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private void validateEnums(
        final ContractResult contractResult,
        final ContractReviewStatus reviewStatus
    ) {
        if (contractResult == null || reviewStatus == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }
}
