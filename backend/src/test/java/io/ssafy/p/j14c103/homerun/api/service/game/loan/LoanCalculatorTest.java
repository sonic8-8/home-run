package io.ssafy.p.j14c103.homerun.api.service.game.loan;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.api.service.game.loan.LoanCalculator.CalculationResult;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.RepaymentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoanCalculatorTest {

    @Test
    @DisplayName("원리금균등 - 2억, 3.49%, 360개월 계산")
    void equalPrincipalInterest() {
        // given
        final int principal = 200_000_000;
        final double annualRate = 3.49;
        final int termMonths = 360;

        // when
        final CalculationResult result = LoanCalculator.calculate(
                principal, annualRate, termMonths, RepaymentType.EQUAL_PRINCIPAL_INTEREST);

        // then
        assertThat(result.monthlyPayment()).isGreaterThan(0);
        assertThat(result.totalInterest()).isGreaterThan(0);
        assertThat(result.totalPayment()).isEqualTo(result.monthlyPayment() * termMonths);
        assertThat(result.totalPayment()).isGreaterThan(principal);
    }

    @Test
    @DisplayName("원금균등 - 1억, 4.0%, 120개월 계산")
    void equalPrincipal() {
        // given
        final int principal = 100_000_000;
        final double annualRate = 4.0;
        final int termMonths = 120;

        // when
        final CalculationResult result = LoanCalculator.calculate(
                principal, annualRate, termMonths, RepaymentType.EQUAL_PRINCIPAL);

        // then
        assertThat(result.monthlyPayment()).isGreaterThan(0);
        assertThat(result.totalInterest()).isGreaterThan(0);
        assertThat(result.totalPayment()).isEqualTo(principal + result.totalInterest());
    }

    @Test
    @DisplayName("만기일시 - 5천만, 3.0%, 60개월 계산")
    void bulletRepayment() {
        // given
        final int principal = 50_000_000;
        final double annualRate = 3.0;
        final int termMonths = 60;

        // when
        final CalculationResult result = LoanCalculator.calculate(
                principal, annualRate, termMonths, RepaymentType.BULLET);

        // then
        final int expectedMonthlyInterest = (int) Math.round(principal * 0.03 / 12);
        assertThat(result.monthlyPayment()).isEqualTo(expectedMonthlyInterest);
        assertThat(result.totalInterest()).isEqualTo(expectedMonthlyInterest * termMonths);
        assertThat(result.totalPayment()).isEqualTo(principal + result.totalInterest());
    }

    @Test
    @DisplayName("금리 0%일 때 이자 없이 원금만 분할")
    void zeroInterestRate() {
        // given
        final int principal = 12_000_000;
        final double annualRate = 0;
        final int termMonths = 12;

        // when
        final CalculationResult result = LoanCalculator.calculate(
                principal, annualRate, termMonths, RepaymentType.EQUAL_PRINCIPAL_INTEREST);

        // then
        assertThat(result.monthlyPayment()).isEqualTo(1_000_000);
        assertThat(result.totalInterest()).isEqualTo(0);
        assertThat(result.totalPayment()).isEqualTo(principal);
    }
}
