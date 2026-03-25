package io.ssafy.p.j14c103.homerun.api.service.game.loan.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoanRepayServiceRequest {

    private Integer loanId;
    private int amount;

    @Builder(access = AccessLevel.PRIVATE)
    private LoanRepayServiceRequest(final Integer loanId, final int amount) {
        this.loanId = loanId;
        this.amount = amount;
    }

    public static LoanRepayServiceRequest of(final Integer loanId, final int amount) {
        return LoanRepayServiceRequest.builder()
            .loanId(loanId)
            .amount(amount)
            .build();
    }
}
