package io.ssafy.p.j14c103.homerun.api.service.game.loan.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoanConfirmServiceRequest {

    private Integer applicationId;
    private int requestedAmount;
    private boolean agreed;

    @Builder(access = AccessLevel.PRIVATE)
    private LoanConfirmServiceRequest(
        final Integer applicationId,
        final int requestedAmount,
        final boolean agreed
    ) {
        this.applicationId = applicationId;
        this.requestedAmount = requestedAmount;
        this.agreed = agreed;
    }

    public static LoanConfirmServiceRequest of(
        final Integer applicationId,
        final int requestedAmount,
        final boolean agreed
    ) {
        return LoanConfirmServiceRequest.builder()
            .applicationId(applicationId)
            .requestedAmount(requestedAmount)
            .agreed(agreed)
            .build();
    }
}
