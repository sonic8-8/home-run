package io.ssafy.p.j14c103.homerun.api.dto.home;

import io.ssafy.p.j14c103.homerun.domain.money.Money;

import java.math.BigDecimal;

public class DashboardResponse {

    private final Money totalAsset;
    private final Money monthlyIncome;
    private final Money monthlyExpense;
    private final BigDecimal lowestLoanRate;

    private DashboardResponse(
            final Money totalAsset,
            final Money monthlyIncome,
            final Money monthlyExpense,
            final BigDecimal lowestLoanRate) {
        this.totalAsset = totalAsset;
        this.monthlyIncome = monthlyIncome;
        this.monthlyExpense = monthlyExpense;
        this.lowestLoanRate = lowestLoanRate;
    }

    public static DashboardResponse of(
            final Money totalAsset,
            final Money monthlyIncome,
            final Money monthlyExpense,
            final BigDecimal lowestLoanRate) {
        if (totalAsset == null) {
            throw new IllegalArgumentException("총자산은 null일 수 없습니다.");
        }
        if (monthlyIncome == null) {
            throw new IllegalArgumentException("월 수입은 null일 수 없습니다.");
        }
        if (monthlyExpense == null) {
            throw new IllegalArgumentException("월 지출은 null일 수 없습니다.");
        }
        return new DashboardResponse(totalAsset, monthlyIncome, monthlyExpense, lowestLoanRate);
    }

    public Money getTotalAsset() {
        return totalAsset;
    }

    public Money getMonthlyIncome() {
        return monthlyIncome;
    }

    public Money getMonthlyExpense() {
        return monthlyExpense;
    }

    public BigDecimal getLowestLoanRate() {
        return lowestLoanRate;
    }
}
