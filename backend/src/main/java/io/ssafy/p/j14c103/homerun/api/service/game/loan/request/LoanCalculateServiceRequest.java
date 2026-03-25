package io.ssafy.p.j14c103.homerun.api.service.game.loan.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoanCalculateServiceRequest {

    private String repaymentMethod;
    private int termMonths;
    private int principal;
    private double annualRate;

    @Builder(access = AccessLevel.PRIVATE)
    private LoanCalculateServiceRequest(
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

    public static LoanCalculateServiceRequest of(
        final String repaymentMethod,
        final int termMonths,
        final int principal,
        final double annualRate
    ) {
        return LoanCalculateServiceRequest.builder()
            .repaymentMethod(repaymentMethod)
            .termMonths(termMonths)
            .principal(principal)
            .annualRate(annualRate)
            .build();
    }
}
