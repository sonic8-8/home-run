package io.ssafy.p.j14c103.homerun.api.service.game.loan.response;

import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.GameLoan;

/**
 * 대출 최종 확정 응답.
 */
public record LoanConfirmResponse(
        Integer loanId,
        int amount,
        double annualRate,
        int monthlyPayment,
        String status
) {

    public static LoanConfirmResponse from(final GameLoan loan) {
        return new LoanConfirmResponse(
                loan.getGameLoanId(),
                loan.getPrincipalAmount(),
                loan.getInterestRate().doubleValue(),
                loan.getMonthlyPaymentAmount(),
                loan.getLoanStatus().name()
        );
    }
}
