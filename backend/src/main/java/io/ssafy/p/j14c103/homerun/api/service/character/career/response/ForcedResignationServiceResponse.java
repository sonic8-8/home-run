package io.ssafy.p.j14c103.homerun.api.service.character.career.response;

import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;

public record ForcedResignationServiceResponse(
    boolean forcedResigned,
    EmploymentStatus employmentStatus,
    Integer rehireAvailableTurn,
    int remainingUnemploymentBenefitTurns,
    int salaryBeforeResignation,
    String message
) {

    public ForcedResignationServiceResponse {
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

    public static ForcedResignationServiceResponse of(
        final boolean forcedResigned,
        final EmploymentStatus employmentStatus,
        final Integer rehireAvailableTurn,
        final int remainingUnemploymentBenefitTurns,
        final int salaryBeforeResignation,
        final String message
    ) {
        return new ForcedResignationServiceResponse(
            forcedResigned,
            employmentStatus,
            rehireAvailableTurn,
            remainingUnemploymentBenefitTurns,
            salaryBeforeResignation,
            message
        );
    }
}
