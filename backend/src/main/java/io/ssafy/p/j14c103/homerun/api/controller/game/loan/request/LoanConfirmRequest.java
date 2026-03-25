package io.ssafy.p.j14c103.homerun.api.controller.game.loan.request;

import io.ssafy.p.j14c103.homerun.api.service.game.loan.request.LoanConfirmServiceRequest;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    @NotNull(message = "{validation.loan.confirm.applicationId.notNull}")
    private Integer applicationId;

    @Positive(message = "{validation.loan.confirm.requestedAmount.positive}")
    private int requestedAmount;

    @AssertTrue(message = "{validation.loan.confirm.agreed.assertTrue}")
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

    public LoanConfirmServiceRequest toServiceRequest() {
        return LoanConfirmServiceRequest.of(applicationId, requestedAmount, agreed);
    }
}
