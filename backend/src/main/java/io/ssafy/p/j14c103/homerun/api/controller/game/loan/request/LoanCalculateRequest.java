package io.ssafy.p.j14c103.homerun.api.controller.game.loan.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이자 계산기 요청.
 */
@Getter
@NoArgsConstructor
public class LoanCalculateRequest {

    private String repaymentMethod;
    private int termMonths;
    private int principal;
    private double annualRate;

    @Builder(access = AccessLevel.PRIVATE)
    private LoanCalculateRequest(
        final String repaymentMethod,
        final int termMonths,
        final int principal,
        final double annualRate
    ) {
        this.repaymentMethod = repaymentMethod;
        this.termMonths = termMonths;
        this.principal = principal;
        this.annualRate = annualRate;
    }

    public static LoanCalculateRequest of(
        final String repaymentMethod,
        final int termMonths,
        final int principal,
        final double annualRate
    ) {
        return LoanCalculateRequest.builder()
            .repaymentMethod(repaymentMethod)
            .termMonths(termMonths)
            .principal(principal)
            .annualRate(annualRate)
            .build();
    }

    public String repaymentMethod() {
        return repaymentMethod;
    }

    public int termMonths() {
        return termMonths;
    }

    public int principal() {
        return principal;
    }

    public double annualRate() {
        return annualRate;
    }
}
