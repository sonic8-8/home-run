package io.ssafy.p.j14c103.homerun.api.service.game.loan.response;

import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.GameLoan;

/**
 * 중도 상환 응답.
 */
public record LoanRepayResponse(
        Integer loanId,
        int repaidAmount,
        int remainingPrincipal,
        int updatedMonthlyPayment
) {

    public static LoanRepayResponse from(final GameLoan loan, final int repaidAmount) {
        return new LoanRepayResponse(
                loan.getGameLoanId(),
                repaidAmount,
                loan.getPrincipalAmount(),
                loan.getMonthlyPaymentAmount()
        );
    }
}
