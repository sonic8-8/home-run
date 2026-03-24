package io.ssafy.p.j14c103.homerun.api.service.character.career.response;

import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ForcedResignationServiceResponse {

    private final boolean forcedResigned;
    private final EmploymentStatus employmentStatus;
    private final Integer rehireAvailableTurn;
    private final int remainingUnemploymentBenefitTurns;
    private final int salaryBeforeResignation;
    private final String message;

    @Builder(access = AccessLevel.PRIVATE)
    private ForcedResignationServiceResponse(
        final boolean forcedResigned,
        final EmploymentStatus employmentStatus,
        final Integer rehireAvailableTurn,
        final int remainingUnemploymentBenefitTurns,
        final int salaryBeforeResignation,
        final String message
    ) {
        validateRequest(
            forcedResigned,
            employmentStatus,
            rehireAvailableTurn,
            remainingUnemploymentBenefitTurns,
            salaryBeforeResignation,
            message
        );

        this.forcedResigned = forcedResigned;
        this.employmentStatus = employmentStatus;
        this.rehireAvailableTurn = rehireAvailableTurn;
        this.remainingUnemploymentBenefitTurns = remainingUnemploymentBenefitTurns;
        this.salaryBeforeResignation = salaryBeforeResignation;
        this.message = message;
    }

    public static ForcedResignationServiceResponse of(
        final boolean forcedResigned,
        final EmploymentStatus employmentStatus,
        final Integer rehireAvailableTurn,
        final int remainingUnemploymentBenefitTurns,
        final int salaryBeforeResignation,
        final String message
    ) {
        return ForcedResignationServiceResponse.builder()
            .forcedResigned(forcedResigned)
            .employmentStatus(employmentStatus)
            .rehireAvailableTurn(rehireAvailableTurn)
            .remainingUnemploymentBenefitTurns(remainingUnemploymentBenefitTurns)
            .salaryBeforeResignation(salaryBeforeResignation)
            .message(message)
            .build();
    }

    private void validateRequest(
        final boolean forcedResigned,
        final EmploymentStatus employmentStatus,
        final Integer rehireAvailableTurn,
        final int remainingUnemploymentBenefitTurns,
        final int salaryBeforeResignation,
        final String message
    ) {
        if (!forcedResigned) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        if (employmentStatus != EmploymentStatus.UNEMPLOYED) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        if (rehireAvailableTurn == null || rehireAvailableTurn < 1) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        if (remainingUnemploymentBenefitTurns < 0) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        if (salaryBeforeResignation <= 0) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        if (message == null || message.isBlank()) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
    }
}
