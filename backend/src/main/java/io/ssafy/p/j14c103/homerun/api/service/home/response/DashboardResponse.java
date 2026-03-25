package io.ssafy.p.j14c103.homerun.api.service.home.response;

import io.ssafy.p.j14c103.homerun.domain.money.Money;

public class DashboardResponse {

    private final Money totalAssets;
    private final Money monthlyIncome;
    private final Money monthlyExpense;
    private final Money incomeChangeFromLastMonth;
    private final Money expenseChangeFromLastMonth;
    private final Integer nextPaydayDays;
    private final Money mainAccountBalance;
    private final Money seedmoneyBalance;

    private DashboardResponse(
            final Money totalAssets,
            final Money monthlyIncome,
            final Money monthlyExpense,
            final Money incomeChangeFromLastMonth,
            final Money expenseChangeFromLastMonth,
            final Integer nextPaydayDays,
            final Money mainAccountBalance,
            final Money seedmoneyBalance) {
        this.totalAssets = totalAssets;
        this.monthlyIncome = monthlyIncome;
        this.monthlyExpense = monthlyExpense;
        this.incomeChangeFromLastMonth = incomeChangeFromLastMonth;
        this.expenseChangeFromLastMonth = expenseChangeFromLastMonth;
        this.nextPaydayDays = nextPaydayDays;
        this.mainAccountBalance = mainAccountBalance;
        this.seedmoneyBalance = seedmoneyBalance;
    }

    public static DashboardResponse of(
            final Money totalAssets,
            final Money monthlyIncome,
            final Money monthlyExpense,
            final Money incomeChangeFromLastMonth,
            final Money expenseChangeFromLastMonth,
            final Integer nextPaydayDays) {
        if (totalAssets == null) {
            throw new IllegalArgumentException("총자산은 null일 수 없습니다.");
        }
        if (monthlyIncome == null) {
            throw new IllegalArgumentException("월 수입은 null일 수 없습니다.");
        }
        if (monthlyExpense == null) {
            throw new IllegalArgumentException("월 지출은 null일 수 없습니다.");
        }
        return new DashboardResponse(totalAssets, monthlyIncome, monthlyExpense,
                incomeChangeFromLastMonth, expenseChangeFromLastMonth, nextPaydayDays,
                Money.zero(), Money.zero());
    }

    public static DashboardResponse of(
            final Money totalAssets,
            final Money monthlyIncome,
            final Money monthlyExpense,
            final Money incomeChangeFromLastMonth,
            final Money expenseChangeFromLastMonth,
            final Integer nextPaydayDays,
            final Money mainAccountBalance,
            final Money seedmoneyBalance) {
        if (totalAssets == null) {
            throw new IllegalArgumentException("총자산은 null일 수 없습니다.");
        }
        if (monthlyIncome == null) {
            throw new IllegalArgumentException("월 수입은 null일 수 없습니다.");
        }
        if (monthlyExpense == null) {
            throw new IllegalArgumentException("월 지출은 null일 수 없습니다.");
        }
        return new DashboardResponse(totalAssets, monthlyIncome, monthlyExpense,
                incomeChangeFromLastMonth, expenseChangeFromLastMonth, nextPaydayDays,
                mainAccountBalance, seedmoneyBalance);
    }

    public Money getTotalAssets() { return totalAssets; }
    public Money getMonthlyIncome() { return monthlyIncome; }
    public Money getMonthlyExpense() { return monthlyExpense; }
    public Money getIncomeChangeFromLastMonth() { return incomeChangeFromLastMonth; }
    public Money getExpenseChangeFromLastMonth() { return expenseChangeFromLastMonth; }
    public Integer getNextPaydayDays() { return nextPaydayDays; }
    public Money getMainAccountBalance() { return mainAccountBalance; }
    public Money getSeedmoneyBalance() { return seedmoneyBalance; }
}
