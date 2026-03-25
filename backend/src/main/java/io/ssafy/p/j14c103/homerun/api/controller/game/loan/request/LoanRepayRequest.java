package io.ssafy.p.j14c103.homerun.api.controller.game.loan.request;

import io.ssafy.p.j14c103.homerun.api.service.game.loan.request.LoanRepayServiceRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 중도 상환 요청.
 */
@Getter
@NoArgsConstructor
public class LoanRepayRequest {

    @NotNull(message = "{validation.loan.repay.loanId.notNull}")
    private Integer loanId;

    @Positive(message = "{validation.loan.repay.amount.positive}")
    private int amount;

    @Builder(access = AccessLevel.PRIVATE)
    private LoanRepayRequest(final Integer loanId, final int amount) {
        this.loanId = loanId;
        this.amount = amount;
    }

    public static LoanRepayRequest of(final Integer loanId, final int amount) {
        return LoanRepayRequest.builder()
            .loanId(loanId)
            .amount(amount)
            .build();
    }

    public Integer loanId() {
        return loanId;
    }

    public int amount() {
        return amount;
    }

    public LoanRepayServiceRequest toServiceRequest() {
        return LoanRepayServiceRequest.of(loanId, amount);
    }
}
