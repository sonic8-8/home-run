package io.ssafy.p.j14c103.homerun.api.service.game.career.response;

import io.ssafy.p.j14c103.homerun.api.service.character.career.response.JobTransferServiceResponse;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class JobTransferResponse {

    private final JobType previousJobType;
    private final JobType newJobType;
    private final String newJobTitle;
    private final int newSalary;
    private final Integer probationEndTurn;
    private final boolean tenureReset;
    private final String message;

    @Builder(access = AccessLevel.PRIVATE)
    private JobTransferResponse(
        final JobType previousJobType,
        final JobType newJobType,
        final String newJobTitle,
        final int newSalary,
        final Integer probationEndTurn,
        final boolean tenureReset,
        final String message
    ) {
        this.previousJobType = previousJobType;
        this.newJobType = newJobType;
        this.newJobTitle = newJobTitle;
        this.newSalary = newSalary;
        this.probationEndTurn = probationEndTurn;
        this.tenureReset = tenureReset;
        this.message = message;
    }

    public static JobTransferResponse of(
        final JobType previousJobType,
        final JobType newJobType,
        final String newJobTitle,
        final int newSalary,
        final Integer probationEndTurn,
        final boolean tenureReset,
        final String message
    ) {
        return JobTransferResponse.builder()
            .previousJobType(previousJobType)
            .newJobType(newJobType)
            .newJobTitle(newJobTitle)
            .newSalary(newSalary)
            .probationEndTurn(probationEndTurn)
            .tenureReset(tenureReset)
            .message(message)
            .build();
    }

    public static JobTransferResponse from(final JobTransferServiceResponse response) {
        if (response == null) {
            throw new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
        }
        return of(
            response.previousJobType(),
            response.newJobType(),
            response.newJobTitle(),
            response.newSalary(),
            response.probationEndTurn(),
            response.tenureReset(),
            response.message()
        );
    }
}
