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

    private Long mainAccountBalanceAmount;
    private Integer salaryDayOfMonth;
    private Long monthlySalaryAmount;
    private Long monthlyFixedExpenseAmount;
    private JobType jobType;
    private List<NamedAmountItem> depositItems;
    private List<NamedAmountItem> loanItems;
    private List<NamedAmountItem> otherIncomeItems;
    private List<CardSpendItem> cardSpendItems;
    private List<String> paymentTypes;

    @Builder
    private UserAssetLinkServiceRequest(
            final Long mainAccountBalanceAmount,
            final Integer salaryDayOfMonth,
            final Long monthlySalaryAmount,
            final Long monthlyFixedExpenseAmount,
            final JobType jobType,
            final List<NamedAmountItem> depositItems,
            final List<NamedAmountItem> loanItems,
            final List<NamedAmountItem> otherIncomeItems,
            final List<CardSpendItem> cardSpendItems,
            final List<String> paymentTypes
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
        this.paymentTypes = normalizePaymentTypes(paymentTypes);
    }

    public static class UserAssetLinkServiceRequestBuilder {

        public UserAssetLinkServiceRequestBuilder mainAccountBalanceAmount(final Long mainAccountBalanceAmount) {
            this.mainAccountBalanceAmount = mainAccountBalanceAmount;
            return this;
        }

        public UserAssetLinkServiceRequestBuilder mainAccountBalanceAmount(final int mainAccountBalanceAmount) {
            this.mainAccountBalanceAmount = Long.valueOf(mainAccountBalanceAmount);
            return this;
        }

        public UserAssetLinkServiceRequestBuilder monthlySalaryAmount(final Long monthlySalaryAmount) {
            this.monthlySalaryAmount = monthlySalaryAmount;
            return this;
        }

        public UserAssetLinkServiceRequestBuilder monthlySalaryAmount(final int monthlySalaryAmount) {
            this.monthlySalaryAmount = Long.valueOf(monthlySalaryAmount);
            return this;
        }

        public UserAssetLinkServiceRequestBuilder monthlyFixedExpenseAmount(final Long monthlyFixedExpenseAmount) {
            this.monthlyFixedExpenseAmount = monthlyFixedExpenseAmount;
            return this;
        }

        public UserAssetLinkServiceRequestBuilder monthlyFixedExpenseAmount(final int monthlyFixedExpenseAmount) {
            this.monthlyFixedExpenseAmount = Long.valueOf(monthlyFixedExpenseAmount);
            return this;
        }
    }

    public long totalDepositAmount() {
        return depositItems.stream()
                .mapToLong(NamedAmountItem::getAmount)
                .sum();
    }

    public long totalLoanAmount() {
        return loanItems.stream()
                .mapToLong(NamedAmountItem::getAmount)
                .sum();
    }

    public long totalOtherIncomeAmount() {
        return otherIncomeItems.stream()
                .mapToLong(NamedAmountItem::getAmount)
                .sum();
    }

    public long totalCardSpendAmount() {
        return cardSpendItems.stream()
                .mapToLong(CardSpendItem::getAmount)
                .sum();
    }

    public String joinedPaymentTypes() {
        return String.join(",", paymentTypes);
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

    private List<String> normalizePaymentTypes(final List<String> items) {
        if (items == null) {
            return List.of();
        }
        return List.copyOf(items);
    }

    @Getter
    @NoArgsConstructor
    public static class NamedAmountItem {

        private String name;
        private Long amount;

        @Builder
        private NamedAmountItem(final String name, final Long amount) {
            this.name = name;
            this.amount = amount;
        }

        public static class NamedAmountItemBuilder {

            public NamedAmountItemBuilder amount(final Long amount) {
                this.amount = amount;
                return this;
            }

            public NamedAmountItemBuilder amount(final int amount) {
                this.amount = Long.valueOf(amount);
                return this;
            }
        }
    }

    @Getter
    @NoArgsConstructor
    public static class CardSpendItem {

        private SpendingCategory category;
        private Long amount;

        @Builder
        private CardSpendItem(final SpendingCategory category, final Long amount) {
            this.category = category;
            this.amount = amount;
        }

        public static class CardSpendItemBuilder {

            public CardSpendItemBuilder amount(final Long amount) {
                this.amount = amount;
                return this;
            }

            public CardSpendItemBuilder amount(final int amount) {
                this.amount = Long.valueOf(amount);
                return this;
            }
        }
    }
}
