package io.ssafy.p.j14c103.homerun.api.controller.game.loan.request;

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

    private Integer loanId;
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
}
