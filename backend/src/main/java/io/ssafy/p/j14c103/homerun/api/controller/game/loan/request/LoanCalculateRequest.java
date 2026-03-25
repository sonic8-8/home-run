package io.ssafy.p.j14c103.homerun.api.controller.game.loan.request;

import io.ssafy.p.j14c103.homerun.api.service.game.loan.request.LoanCalculateServiceRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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

    @NotBlank(message = "{validation.loan.calculate.repaymentMethod.notBlank}")
    private String repaymentMethod;

    @Positive(message = "{validation.loan.calculate.termMonths.positive}")
    private int termMonths;

    @Positive(message = "{validation.loan.calculate.principal.positive}")
    private int principal;

    @PositiveOrZero(message = "{validation.loan.calculate.annualRate.positiveOrZero}")
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

    public LoanCalculateServiceRequest toServiceRequest() {
        return LoanCalculateServiceRequest.of(
            repaymentMethod,
            termMonths,
            principal,
            annualRate
        );
    }
}
