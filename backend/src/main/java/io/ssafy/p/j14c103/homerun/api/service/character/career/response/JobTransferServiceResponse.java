package io.ssafy.p.j14c103.homerun.api.service.character.career.response;

import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class JobTransferServiceResponse {

    private final JobType previousJobType;
    private final JobType newJobType;
    private final String newJobTitle;
    private final int newSalary;
    private final Integer probationEndTurn;
    private final boolean tenureReset;
    private final String message;

    @Builder(access = AccessLevel.PRIVATE)
    private JobTransferServiceResponse(
        final JobType previousJobType,
        final JobType newJobType,
        final String newJobTitle,
        final int newSalary,
        final Integer probationEndTurn,
        final boolean tenureReset,
        final String message
    ) {
        validateRequest(
            previousJobType,
            newJobType,
            newJobTitle,
            newSalary,
            probationEndTurn,
            message
        );

        this.previousJobType = previousJobType;
        this.newJobType = newJobType;
        this.newJobTitle = newJobTitle;
        this.newSalary = newSalary;
        this.probationEndTurn = probationEndTurn;
        this.tenureReset = tenureReset;
        this.message = message;
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
        return JobTransferServiceResponse.builder()
            .previousJobType(previousJobType)
            .newJobType(newJobType)
            .newJobTitle(newJobTitle)
            .newSalary(newSalary)
            .probationEndTurn(probationEndTurn)
            .tenureReset(tenureReset)
            .message(message)
            .build();
    }

    private void validateRequest(
        final JobType previousJobType,
        final JobType newJobType,
        final String newJobTitle,
        final int newSalary,
        final Integer probationEndTurn,
        final String message
    ) {
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
}
