package io.ssafy.p.j14c103.homerun.api.service.game.loan.response;

import io.ssafy.p.j14c103.homerun.api.service.game.loan.LoanCalculator;
import lombok.Getter;

/**
 * 이자 계산기 응답.
 */
@Getter
public class LoanCalculateResponse {

    private final int monthlyPayment;
    private final int totalInterest;
    private final int totalPayment;

    private LoanCalculateResponse(
        final int monthlyPayment,
        final int totalInterest,
        final int totalPayment
    ) {
        this.monthlyPayment = monthlyPayment;
        this.totalInterest = totalInterest;
        this.totalPayment = totalPayment;
    }

    public static LoanCalculateResponse from(final LoanCalculator.CalculationResult result) {
        return new LoanCalculateResponse(
                result.monthlyPayment(), result.totalInterest(), result.totalPayment());
    }

    public int monthlyPayment() {
        return monthlyPayment;
    }

    public int totalInterest() {
        return totalInterest;
    }

    public int totalPayment() {
        return totalPayment;
    }
}
