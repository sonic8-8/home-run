package io.ssafy.p.j14c103.homerun.api.service.home.response;

import io.ssafy.p.j14c103.homerun.domain.money.Money;

import java.util.List;

public class SpendingResponse {

    private final String month;
    private final Money totalExpense;
    private final List<SpendingCategoryDetail> categories;

    private SpendingResponse(
            final String month,
            final Money totalExpense,
            final List<SpendingCategoryDetail> categories) {
        this.month = month;
        this.totalExpense = totalExpense;
        this.categories = categories;
    }

    public static SpendingResponse of(
            final String month,
            final Money totalExpense,
            final List<SpendingCategoryDetail> categories) {
        if (month == null || month.isBlank()) {
            throw new IllegalArgumentException("조회 월은 필수입니다.");
        }
        if (totalExpense == null) {
            throw new IllegalArgumentException("총 지출은 필수입니다.");
        }
        if (categories == null) {
            throw new IllegalArgumentException("카테고리 목록은 필수입니다.");
        }
        return new SpendingResponse(month, totalExpense, categories);
    }

    public String getMonth() {
        return month;
    }

    public Money getTotalExpense() {
        return totalExpense;
    }

    public List<SpendingCategoryDetail> getCategories() {
        return categories;
    }
}
