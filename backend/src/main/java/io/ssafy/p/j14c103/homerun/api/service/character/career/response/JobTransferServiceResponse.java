package io.ssafy.p.j14c103.homerun.api.service.character.career.response;

import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;

public record JobTransferServiceResponse(
    JobType previousJobType,
    JobType newJobType,
    String newJobTitle,
    int newSalary,
    Integer probationEndTurn,
    boolean tenureReset,
    String message
) {

    public JobTransferServiceResponse {
        if (previousJobType == null || newJobType == null) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        if (newJobTitle == null || newJobTitle.isBlank()) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        if (newSalary <= 0) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        if (probationEndTurn != null && probationEndTurn < 1) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
        if (message == null || message.isBlank()) {
            throw new HomerunException(ErrorCode.CHARACTER_RESPONSE_INVALID);
        }
    }

    public static JobTransferServiceResponse of(
        final JobType previousJobType,
        final JobType newJobType,
        final String newJobTitle,
        final int newSalary,
        final Integer probationEndTurn,
        final boolean tenureReset,
        final String message
    ) {
        return new JobTransferServiceResponse(
            previousJobType,
            newJobType,
            newJobTitle,
            newSalary,
            probationEndTurn,
            tenureReset,
            message
        );
    }
}
