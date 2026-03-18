package io.ssafy.p.j14c103.homerun.api.service.game.loan.response;

import io.ssafy.p.j14c103.homerun.api.service.game.loan.LoanCalculator;

/**
 * 이자 계산기 응답.
 */
public record LoanCalculateResponse(
        int monthlyPayment,
        int totalInterest,
        int totalPayment
) {

    public static LoanCalculateResponse from(final LoanCalculator.CalculationResult result) {
        return new LoanCalculateResponse(
                result.monthlyPayment(), result.totalInterest(), result.totalPayment());
    }
}
