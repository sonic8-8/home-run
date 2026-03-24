package io.ssafy.p.j14c103.homerun.api.service.game.loan.response;

import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.GameLoan;
import lombok.Getter;

/**
 * 대출 최종 확정 응답.
 */
@Getter
public class LoanConfirmResponse {

    private final Integer loanId;
    private final int amount;
    private final double annualRate;
    private final int monthlyPayment;
    private final String status;

    private LoanConfirmResponse(
        final Integer loanId,
        final int amount,
        final double annualRate,
        final int monthlyPayment,
        final String status
    ) {
        this.loanId = loanId;
        this.amount = amount;
        this.annualRate = annualRate;
        this.monthlyPayment = monthlyPayment;
        this.status = status;
    }

    public static LoanConfirmResponse from(final GameLoan loan) {
        return new LoanConfirmResponse(
                loan.getGameLoanId(),
                loan.getPrincipalAmount(),
                loan.getInterestRate().doubleValue(),
                loan.getMonthlyPaymentAmount(),
                loan.getLoanStatus().name()
        );
    }

    public Integer loanId() {
        return loanId;
    }

    public int amount() {
        return amount;
    }

    public double annualRate() {
        return annualRate;
    }

    public int monthlyPayment() {
        return monthlyPayment;
    }

    public String status() {
        return status;
    }
}
