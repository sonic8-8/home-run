package io.ssafy.p.j14c103.homerun.api.service.card.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;

@Getter
public class CardBenefitResponse {

    private final String categoryId;
    private final String categoryName;
    private final String categoryDescription;
    private final BigDecimal discountRate;
    private final List<String> exampleMerchants;

    private CardBenefitResponse(
            final String categoryId,
            final String categoryName,
            final String categoryDescription,
            final BigDecimal discountRate,
            final List<String> exampleMerchants
    ) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryDescription = categoryDescription;
        this.discountRate = discountRate;
        this.exampleMerchants = List.copyOf(exampleMerchants);
    }

    public static CardBenefitResponse of(
            final String categoryId,
            final String categoryName,
            final String categoryDescription,
            final BigDecimal discountRate,
            final List<String> exampleMerchants
    ) {
        return new CardBenefitResponse(
                categoryId,
                categoryName,
                categoryDescription,
                discountRate,
                exampleMerchants
        );
    }
}
