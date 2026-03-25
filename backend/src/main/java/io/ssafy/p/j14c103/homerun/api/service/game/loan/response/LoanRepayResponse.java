package io.ssafy.p.j14c103.homerun.api.service.game.loan.response;

import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.GameLoan;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

/**
 * 중도 상환 응답.
 */
@Getter
public class LoanRepayResponse {

    private final Integer loanId;
    private final int repaidAmount;
    private final int remainingPrincipal;
    private final int updatedMonthlyPayment;

    @Builder(access = AccessLevel.PRIVATE)
    private LoanRepayResponse(
        final Integer loanId,
        final int repaidAmount,
        final int remainingPrincipal,
        final int updatedMonthlyPayment
    ) {
        this.loanId = loanId;
        this.repaidAmount = repaidAmount;
        this.remainingPrincipal = remainingPrincipal;
        this.updatedMonthlyPayment = updatedMonthlyPayment;
    }

    public static LoanRepayResponse from(final GameLoan loan, final int repaidAmount) {
        return LoanRepayResponse.builder()
            .loanId(loan.getGameLoanId())
            .repaidAmount(repaidAmount)
            .remainingPrincipal(loan.getPrincipalAmount())
            .updatedMonthlyPayment(loan.getMonthlyPaymentAmount())
            .build();
    }

    public Integer loanId() {
        return loanId;
    }

    public int repaidAmount() {
        return repaidAmount;
    }

    public int remainingPrincipal() {
        return remainingPrincipal;
    }

    public int updatedMonthlyPayment() {
        return updatedMonthlyPayment;
    }
}
