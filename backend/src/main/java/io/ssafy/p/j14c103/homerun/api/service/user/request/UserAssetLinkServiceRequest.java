package io.ssafy.p.j14c103.homerun.api.service.user.request;

import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.spending.SpendingCategory;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
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
        validate(
                mainAccountBalanceAmount,
                salaryDayOfMonth,
                monthlySalaryAmount,
                monthlyFixedExpenseAmount,
                jobType,
                cardSpendItems
        );
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

    private void validate(
            final Integer mainAccountBalanceAmount,
            final Integer salaryDayOfMonth,
            final Integer monthlySalaryAmount,
            final Integer monthlyFixedExpenseAmount,
            final JobType jobType,
            final List<CardSpendItem> cardSpendItems
    ) {
        if (mainAccountBalanceAmount == null || mainAccountBalanceAmount < 0) {
            throw new IllegalArgumentException("수시입출금 계좌 잔액은 0 이상이어야 합니다.");
        }
        if (salaryDayOfMonth == null || salaryDayOfMonth < 1 || salaryDayOfMonth > 28) {
            throw new IllegalArgumentException("급여일은 1일부터 28일 사이여야 합니다.");
        }
        if (monthlySalaryAmount == null || monthlySalaryAmount < 0) {
            throw new IllegalArgumentException("월 급여액은 0 이상이어야 합니다.");
        }
        if (monthlyFixedExpenseAmount == null || monthlyFixedExpenseAmount < 0) {
            throw new IllegalArgumentException("고정 지출은 0 이상이어야 합니다.");
        }
        if (jobType == null) {
            throw new IllegalArgumentException("직장 유형은 필수입니다.");
        }
        if (cardSpendItems != null && cardSpendItems.size() > 3) {
            throw new IllegalArgumentException("카드 지출 항목은 최대 3개까지 입력할 수 있습니다.");
        }
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
        if (items.stream().map(CardSpendItem::getCategory).distinct().count() != items.size()) {
            throw new IllegalArgumentException("카드 지출 카테고리는 중복될 수 없습니다.");
        }
        final boolean hasInvalidCategory = items.stream()
                .map(CardSpendItem::getCategory)
                .anyMatch(category -> !category.isUserSelectable());
        if (hasInvalidCategory) {
            throw new IllegalArgumentException("허용되지 않은 카드 지출 카테고리입니다.");
        }
        return List.copyOf(items);
    }

    @Getter
    public static class NamedAmountItem {

        private String name;
        private Integer amount;

        @Builder
        private NamedAmountItem(final String name, final Integer amount) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("항목명은 필수입니다.");
            }
            if (amount == null || amount < 0) {
                throw new IllegalArgumentException("금액은 0 이상이어야 합니다.");
            }
            this.name = name;
            this.amount = amount;
        }
    }

    @Getter
    public static class CardSpendItem {

        private SpendingCategory category;
        private Integer amount;

        @Builder
        private CardSpendItem(final SpendingCategory category, final Integer amount) {
            if (category == null) {
                throw new IllegalArgumentException("카드 지출 카테고리는 필수입니다.");
            }
            if (amount == null || amount < 0) {
                throw new IllegalArgumentException("카드 지출 금액은 0 이상이어야 합니다.");
            }
            this.category = category;
            this.amount = amount;
        }
    }
}
