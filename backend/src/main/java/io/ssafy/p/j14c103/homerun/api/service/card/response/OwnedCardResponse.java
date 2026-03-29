package io.ssafy.p.j14c103.homerun.api.service.card.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OwnedCardResponse {

    private final Long ownedCardId;
    private final Long cardProductId;
    private final String cardName;
    private final String cardIssuerName;
    private final String cardDescription;
    private final Long baselinePerformanceAmount;
    private final Long maxBenefitLimitAmount;
    private final List<CardBenefitResponse> activeBenefits;
    private final String cardAlias;
    private final String maskedCardNo;
    private final String cardImageUrl;
    private final LocalDateTime openedAt;

    @Builder
    private OwnedCardResponse(
            final Long ownedCardId,
            final Long cardProductId,
            final String cardName,
            final String cardIssuerName,
            final String cardDescription,
            final Long baselinePerformanceAmount,
            final Long maxBenefitLimitAmount,
            final List<CardBenefitResponse> activeBenefits,
            final String cardAlias,
            final String maskedCardNo,
            final String cardImageUrl,
            final LocalDateTime openedAt
    ) {
        this.ownedCardId = ownedCardId;
        this.cardProductId = cardProductId;
        this.cardName = cardName;
        this.cardIssuerName = cardIssuerName;
        this.cardDescription = cardDescription;
        this.baselinePerformanceAmount = baselinePerformanceAmount;
        this.maxBenefitLimitAmount = maxBenefitLimitAmount;
        this.activeBenefits = List.copyOf(activeBenefits);
        this.cardAlias = cardAlias;
        this.maskedCardNo = maskedCardNo;
        this.cardImageUrl = cardImageUrl;
        this.openedAt = openedAt;
    }

    public static OwnedCardResponse of(
            final Long ownedCardId,
            final Long cardProductId,
            final String cardName,
            final String cardIssuerName,
            final String cardDescription,
            final Long baselinePerformanceAmount,
            final Long maxBenefitLimitAmount,
            final List<CardBenefitResponse> activeBenefits,
            final String cardAlias,
            final String maskedCardNo,
            final String cardImageUrl,
            final LocalDateTime openedAt
    ) {
        return OwnedCardResponse.builder()
                .ownedCardId(ownedCardId)
                .cardProductId(cardProductId)
                .cardName(cardName)
                .cardIssuerName(cardIssuerName)
                .cardDescription(cardDescription)
                .baselinePerformanceAmount(baselinePerformanceAmount)
                .maxBenefitLimitAmount(maxBenefitLimitAmount)
                .activeBenefits(activeBenefits)
                .cardAlias(cardAlias)
                .maskedCardNo(maskedCardNo)
                .cardImageUrl(cardImageUrl)
                .openedAt(openedAt)
                .build();
    }
}
