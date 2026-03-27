package io.ssafy.p.j14c103.homerun.api.service.user.request;

import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.spending.SpendingCategory;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserAssetLinkServiceRequest {

    private Integer mainAccountBalanceAmount;
    private Integer salaryDayOfMonth;
    private Integer monthlySalaryAmount;
    private Integer monthlyFixedExpenseAmount;
    private JobType jobType;
    private List<NamedAmountItem> depositItems;
    private List<NamedAmountItem> loanItems;
    private List<NamedAmountItem> otherIncomeItems;
    private List<CardSpendItem> cardSpendItems;

    @Builder
    private UserAssetLinkServiceRequest(
            final Integer mainAccountBalanceAmount,
            final Integer salaryDayOfMonth,
            final Integer monthlySalaryAmount,
            final Integer monthlyFixedExpenseAmount,
            final JobType jobType,
            final List<NamedAmountItem> depositItems,
            final List<NamedAmountItem> loanItems,
            final List<NamedAmountItem> otherIncomeItems,
            final List<CardSpendItem> cardSpendItems
    ) {
        this.mainAccountBalanceAmount = mainAccountBalanceAmount;
        this.salaryDayOfMonth = salaryDayOfMonth;
        this.monthlySalaryAmount = monthlySalaryAmount;
        this.monthlyFixedExpenseAmount = monthlyFixedExpenseAmount;
        this.jobType = jobType;
        this.depositItems = normalizeNamedItems(depositItems);
        this.loanItems = normalizeNamedItems(loanItems);
        this.otherIncomeItems = normalizeNamedItems(otherIncomeItems);
        this.cardSpendItems = normalizeCardSpendItems(cardSpendItems);
    }

    public int totalDepositAmount() {
        return depositItems.stream()
                .mapToInt(NamedAmountItem::getAmount)
                .sum();
    }

    public int totalLoanAmount() {
        return loanItems.stream()
                .mapToInt(NamedAmountItem::getAmount)
                .sum();
    }

    public int totalOtherIncomeAmount() {
        return otherIncomeItems.stream()
                .mapToInt(NamedAmountItem::getAmount)
                .sum();
    }

    public int totalCardSpendAmount() {
        return cardSpendItems.stream()
                .mapToInt(CardSpendItem::getAmount)
                .sum();
    }

    private List<NamedAmountItem> normalizeNamedItems(final List<NamedAmountItem> items) {
        if (items == null) {
            return List.of();
        }
        return List.copyOf(items);
    }

    private List<CardSpendItem> normalizeCardSpendItems(final List<CardSpendItem> items) {
        if (items == null) {
            return List.of();
        }
        return List.copyOf(items);
    }

    @Getter
    @NoArgsConstructor
    public static class NamedAmountItem {

        private String name;
        private Integer amount;

        @Builder
        private NamedAmountItem(final String name, final Integer amount) {
            this.name = name;
            this.amount = amount;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class CardSpendItem {

        private SpendingCategory category;
        private Integer amount;

        @Builder
        private CardSpendItem(final SpendingCategory category, final Integer amount) {
            this.category = category;
            this.amount = amount;
        }
    }
}
