package io.ssafy.p.j14c103.homerun.api.service.card.response;

import java.util.List;
import lombok.Getter;

@Getter
public class CardResponse {

    private final Long cardProductId;
    private final String cardName;
    private final String cardIssuerName;
    private final String cardDescription;
    private final Long baselinePerformanceAmount;
    private final Long maxBenefitLimitAmount;
    private final String cardImageUrl;
    private final List<CardBenefitResponse> activeBenefits;

    private CardResponse(
            final Long cardProductId,
            final String cardName,
            final String cardIssuerName,
            final String cardDescription,
            final Long baselinePerformanceAmount,
            final Long maxBenefitLimitAmount,
            final String cardImageUrl,
            final List<CardBenefitResponse> activeBenefits
    ) {
        this.cardProductId = cardProductId;
        this.cardName = cardName;
        this.cardIssuerName = cardIssuerName;
        this.cardDescription = cardDescription;
        this.baselinePerformanceAmount = baselinePerformanceAmount;
        this.maxBenefitLimitAmount = maxBenefitLimitAmount;
        this.cardImageUrl = cardImageUrl;
        this.activeBenefits = List.copyOf(activeBenefits);
    }

    public static CardResponse of(
            final Long cardProductId,
            final String cardName,
            final String cardIssuerName,
            final String cardDescription,
            final long baselinePerformanceAmount,
            final long maxBenefitLimitAmount,
            final String cardImageUrl,
            final List<CardBenefitResponse> activeBenefits
    ) {
        return of(
                cardProductId,
                cardName,
                cardIssuerName,
                cardDescription,
                Long.valueOf(baselinePerformanceAmount),
                Long.valueOf(maxBenefitLimitAmount),
                cardImageUrl,
                activeBenefits
        );
    }

    public static CardResponse of(
            final Long cardProductId,
            final String cardName,
            final String cardIssuerName,
            final String cardDescription,
            final Long baselinePerformanceAmount,
            final Long maxBenefitLimitAmount,
            final String cardImageUrl,
            final List<CardBenefitResponse> activeBenefits
    ) {
        return new CardResponse(
                cardProductId,
                cardName,
                cardIssuerName,
                cardDescription,
                baselinePerformanceAmount,
                maxBenefitLimitAmount,
                cardImageUrl,
                activeBenefits
        );
    }
}
