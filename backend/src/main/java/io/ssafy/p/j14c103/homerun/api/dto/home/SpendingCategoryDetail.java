package io.ssafy.p.j14c103.homerun.api.dto.home;

import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.spending.SpendingCategory;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SpendingCategoryDetail {

    private final SpendingCategory category;
    private final Money amount;
    private final BigDecimal ratio;

    private SpendingCategoryDetail(
            final SpendingCategory category,
            final Money amount,
            final BigDecimal ratio) {
        this.category = category;
        this.amount = amount;
        this.ratio = ratio;
    }

    public static SpendingCategoryDetail of(
            final SpendingCategory category,
            final Money amount,
            final Money totalExpense) {
        if (category == null) {
            throw new IllegalArgumentException("카테고리는 필수입니다.");
        }
        if (amount == null) {
            throw new IllegalArgumentException("금액은 필수입니다.");
        }

        final BigDecimal ratio;
        if (totalExpense == null || totalExpense.isZero()) {
            ratio = BigDecimal.ZERO;
        } else {
            ratio = amount.getAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalExpense.getAmount(), 1, RoundingMode.HALF_UP);
        }

        return new SpendingCategoryDetail(category, amount, ratio);
    }

    public String getCategory() {
        return category.name();
    }

    public String getCategoryName() {
        return category.getDisplayName();
    }

    public Money getAmount() {
        return amount;
    }

    public BigDecimal getRatio() {
        return ratio;
    }
}
