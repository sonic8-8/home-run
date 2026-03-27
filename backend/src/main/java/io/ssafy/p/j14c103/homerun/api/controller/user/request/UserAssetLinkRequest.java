package io.ssafy.p.j14c103.homerun.api.controller.user.request;

import io.ssafy.p.j14c103.homerun.api.service.user.request.UserAssetLinkServiceRequest;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.spending.SpendingCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserAssetLinkRequest {

    @NotNull(message = "{validation.user.assetLink.mainAccountBalanceAmount.notNull}")
    @Min(value = 0, message = "{validation.user.assetLink.mainAccountBalanceAmount.min}")
    private Integer mainAccountBalanceAmount;

    @NotNull(message = "{validation.user.assetLink.salaryDayOfMonth.notNull}")
    @Min(value = 1, message = "{validation.user.assetLink.salaryDayOfMonth.min}")
    @Max(value = 28, message = "{validation.user.assetLink.salaryDayOfMonth.max}")
    private Integer salaryDayOfMonth;

    @NotNull(message = "{validation.user.assetLink.monthlySalaryAmount.notNull}")
    @Min(value = 0, message = "{validation.user.assetLink.monthlySalaryAmount.min}")
    private Integer monthlySalaryAmount;

    @NotNull(message = "{validation.user.assetLink.monthlyFixedExpenseAmount.notNull}")
    @Min(value = 0, message = "{validation.user.assetLink.monthlyFixedExpenseAmount.min}")
    private Integer monthlyFixedExpenseAmount;

    @NotNull(message = "{validation.user.assetLink.jobType.notNull}")
    private JobType jobType;

    @Valid
    private List<NamedAmountItemRequest> depositItems;

    @Valid
    private List<NamedAmountItemRequest> loanItems;

    @Valid
    private List<NamedAmountItemRequest> otherIncomeItems;

    @Valid
    private List<CardSpendItemRequest> cardSpendItems;

    @Builder
    private UserAssetLinkRequest(
            final Integer mainAccountBalanceAmount,
            final Integer salaryDayOfMonth,
            final Integer monthlySalaryAmount,
            final Integer monthlyFixedExpenseAmount,
            final JobType jobType,
            final List<NamedAmountItemRequest> depositItems,
            final List<NamedAmountItemRequest> loanItems,
            final List<NamedAmountItemRequest> otherIncomeItems,
            final List<CardSpendItemRequest> cardSpendItems
    ) {
        this.mainAccountBalanceAmount = mainAccountBalanceAmount;
        this.salaryDayOfMonth = salaryDayOfMonth;
        this.monthlySalaryAmount = monthlySalaryAmount;
        this.monthlyFixedExpenseAmount = monthlyFixedExpenseAmount;
        this.jobType = jobType;
        this.depositItems = depositItems;
        this.loanItems = loanItems;
        this.otherIncomeItems = otherIncomeItems;
        this.cardSpendItems = cardSpendItems;
    }

    public UserAssetLinkServiceRequest toServiceRequest() {
        return UserAssetLinkServiceRequest.builder()
                .mainAccountBalanceAmount(mainAccountBalanceAmount)
                .salaryDayOfMonth(salaryDayOfMonth)
                .monthlySalaryAmount(monthlySalaryAmount)
                .monthlyFixedExpenseAmount(monthlyFixedExpenseAmount)
                .jobType(jobType)
                .depositItems(toNamedAmountItems(depositItems))
                .loanItems(toNamedAmountItems(loanItems))
                .otherIncomeItems(toNamedAmountItems(otherIncomeItems))
                .cardSpendItems(toCardSpendItems(cardSpendItems))
                .build();
    }

    @AssertTrue(message = "{validation.user.assetLink.cardSpendItems.size}")
    public boolean isCardSpendItemsWithinLimit() {
        return cardSpendItems == null || cardSpendItems.size() <= 3;
    }

    @AssertTrue(message = "{validation.user.assetLink.cardSpendItems.unique}")
    public boolean isCardSpendItemsUnique() {
        if (cardSpendItems == null) {
            return true;
        }

        return cardSpendItems.stream()
                .map(CardSpendItemRequest::getCategory)
                .distinct()
                .count() == cardSpendItems.size();
    }

    @AssertTrue(message = "{validation.user.assetLink.cardSpendItems.allowed}")
    public boolean isCardSpendItemsAllowed() {
        if (cardSpendItems == null) {
            return true;
        }

        return cardSpendItems.stream()
                .map(CardSpendItemRequest::getCategory)
                .allMatch(this::isAllowedCardCategory);
    }

    private List<UserAssetLinkServiceRequest.NamedAmountItem> toNamedAmountItems(
            final List<NamedAmountItemRequest> items
    ) {
        if (items == null) {
            return List.of();
        }

        return items.stream()
                .map(item -> UserAssetLinkServiceRequest.NamedAmountItem.builder()
                        .name(item.getName())
                        .amount(item.getAmount())
                        .build())
                .toList();
    }

    private List<UserAssetLinkServiceRequest.CardSpendItem> toCardSpendItems(
            final List<CardSpendItemRequest> items
    ) {
        if (items == null) {
            return List.of();
        }

        return items.stream()
                .map(item -> UserAssetLinkServiceRequest.CardSpendItem.builder()
                        .category(SpendingCategory.fromCode(item.getCategory()))
                        .amount(item.getAmount())
                        .build())
                .toList();
    }

    private boolean isAllowedCardCategory(final String categoryCode) {
        try {
            return SpendingCategory.fromCode(categoryCode).isUserSelectable();
        } catch (final IllegalArgumentException exception) {
            return false;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class NamedAmountItemRequest {

        @NotBlank(message = "{validation.user.assetLink.item.name.notBlank}")
        private String name;

        @NotNull(message = "{validation.user.assetLink.item.amount.notNull}")
        @Min(value = 0, message = "{validation.user.assetLink.item.amount.min}")
        private Integer amount;

        @Builder
        private NamedAmountItemRequest(final String name, final Integer amount) {
            this.name = name;
            this.amount = amount;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class CardSpendItemRequest {

        @NotBlank(message = "{validation.user.assetLink.cardSpendItems.category.notBlank}")
        private String category;

        @NotNull(message = "{validation.user.assetLink.cardSpendItems.amount.notNull}")
        @Min(value = 0, message = "{validation.user.assetLink.cardSpendItems.amount.min}")
        private Integer amount;

        @Builder
        private CardSpendItemRequest(final String category, final Integer amount) {
            this.category = category;
            this.amount = amount;
        }
    }
}
