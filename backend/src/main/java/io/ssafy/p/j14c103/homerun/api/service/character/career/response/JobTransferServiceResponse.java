package io.ssafy.p.j14c103.homerun.api.service.character.career.response;

import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
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

    private JobTransferServiceResponse(
        final JobType previousJobType,
        final JobType newJobType,
        final String newJobTitle,
        final int newSalary,
        final Integer probationEndTurn,
        final boolean tenureReset,
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

    public JobType previousJobType() {
        return previousJobType;
    }

    public JobType newJobType() {
        return newJobType;
    }

    public String newJobTitle() {
        return newJobTitle;
    }

    public int newSalary() {
        return newSalary;
    }

    public Integer probationEndTurn() {
        return probationEndTurn;
    }

    public boolean tenureReset() {
        return tenureReset;
    }

    public String message() {
        return message;
    }
}
