package io.ssafy.p.j14c103.homerun.api.controller.game.loan.request;

/**
 * 이자 계산기 요청.
 */
public record LoanCalculateRequest(
        String repaymentMethod,
        int termMonths,
        int principal,
        double annualRate
) {
}
