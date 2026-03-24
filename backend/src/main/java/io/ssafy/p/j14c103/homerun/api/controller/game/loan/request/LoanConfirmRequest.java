package io.ssafy.p.j14c103.homerun.api.controller.game.loan.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 대출 최종 확정 요청.
 */
@Getter
@NoArgsConstructor
public class LoanConfirmRequest {

    private Integer applicationId;
    private int requestedAmount;
    private boolean agreed;

    @Builder(access = AccessLevel.PRIVATE)
    private LoanConfirmRequest(
        final Integer applicationId,
        final int requestedAmount,
        final boolean agreed
    ) {
        this.applicationId = applicationId;
        this.requestedAmount = requestedAmount;
        this.agreed = agreed;
    }

    public static LoanConfirmRequest of(
        final Integer applicationId,
        final int requestedAmount,
        final boolean agreed
    ) {
        return LoanConfirmRequest.builder()
            .applicationId(applicationId)
            .requestedAmount(requestedAmount)
            .agreed(agreed)
            .build();
    }

    public Integer applicationId() {
        return applicationId;
    }

    public int requestedAmount() {
        return requestedAmount;
    }

    public boolean agreed() {
        return agreed;
    }
}
