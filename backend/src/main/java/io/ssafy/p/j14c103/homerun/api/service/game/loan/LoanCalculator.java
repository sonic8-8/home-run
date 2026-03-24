package io.ssafy.p.j14c103.homerun.api.service.game.loan;

import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.RepaymentType;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 대출 이자 계산 유틸리티.
 * 3가지 상환방식(원리금균등, 원금균등, 만기일시)별 월 상환액과 총 이자를 계산한다.
 */
public final class LoanCalculator {

    private LoanCalculator() {
    }

    public static CalculationResult calculate(
            final int principal,
            final double annualRate,
            final int termMonths,
            final RepaymentType repaymentType
    ) {
        return switch (repaymentType) {
            case EQUAL_PRINCIPAL_INTEREST -> equalPrincipalInterest(principal, annualRate, termMonths);
            case EQUAL_PRINCIPAL -> equalPrincipal(principal, annualRate, termMonths);
            case BULLET -> bullet(principal, annualRate, termMonths);
        };
    }

    /**
     * 원리금균등: M = P × r(1+r)^n / ((1+r)^n - 1)
     */
    private static CalculationResult equalPrincipalInterest(
            final int principal, final double annualRate, final int termMonths
    ) {
        if (annualRate == 0) {
            final int monthly = principal / termMonths;
            return new CalculationResult(monthly, 0, principal);
        }

        final double r = annualRate / 100.0 / 12.0;
        final double rPowN = Math.pow(1 + r, termMonths);
        final double monthly = principal * r * rPowN / (rPowN - 1);
        final int monthlyPayment = (int) Math.round(monthly);
        final int totalPayment = monthlyPayment * termMonths;
        final int totalInterest = totalPayment - principal;

        return new CalculationResult(monthlyPayment, totalInterest, totalPayment);
    }

    /**
     * 원금균등: 월 원금 = P/n, k번째 이자 = (P - P×(k-1)/n) × r
     * 첫 달 상환액(최대값)을 monthlyPayment로 반환.
     */
    private static CalculationResult equalPrincipal(
            final int principal, final double annualRate, final int termMonths
    ) {
        final double r = annualRate / 100.0 / 12.0;
        final BigDecimal principalPerMonth = BigDecimal.valueOf(principal)
                .divide(BigDecimal.valueOf(termMonths), 4, RoundingMode.HALF_UP);

        BigDecimal totalInterest = BigDecimal.ZERO;
        for (int k = 1; k <= termMonths; k++) {
            final BigDecimal remaining = BigDecimal.valueOf(principal)
                    .subtract(principalPerMonth.multiply(BigDecimal.valueOf(k - 1)));
            totalInterest = totalInterest.add(remaining.multiply(BigDecimal.valueOf(r)));
        }

        final int firstMonthPayment = principalPerMonth.intValue()
                + BigDecimal.valueOf(principal).multiply(BigDecimal.valueOf(r))
                .setScale(0, RoundingMode.HALF_UP).intValue();
        final int totalInterestInt = totalInterest.setScale(0, RoundingMode.HALF_UP).intValue();
        final int totalPayment = principal + totalInterestInt;

        return new CalculationResult(firstMonthPayment, totalInterestInt, totalPayment);
    }

    /**
     * 만기일시: 월 이자만 납부, 만기 시 원금 일시 상환.
     */
    private static CalculationResult bullet(
            final int principal, final double annualRate, final int termMonths
    ) {
        final double r = annualRate / 100.0 / 12.0;
        final int monthlyInterest = (int) Math.round(principal * r);
        final int totalInterest = monthlyInterest * termMonths;
        final int totalPayment = principal + totalInterest;

        return new CalculationResult(monthlyInterest, totalInterest, totalPayment);
    }

    /**
     * 계산 결과.
     */
    public record CalculationResult(
            int monthlyPayment,
            int totalInterest,
            int totalPayment
    ) {
    }
}
