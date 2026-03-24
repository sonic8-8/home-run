package io.ssafy.p.j14c103.homerun.api.service.card.response;

import io.ssafy.p.j14c103.homerun.domain.card.OwnedCard;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class OwnedCardResponse {

    private final Long ownedCardId;
    private final Long cardProductId;
    private final String cardName;
    private final String cardIssuerName;
    private final String cardAlias;
    private final String maskedCardNo;
    private final String cardImageUrl;
    private final LocalDateTime openedAt;

    private OwnedCardResponse(
            final Long ownedCardId,
            final Long cardProductId,
            final String cardName,
            final String cardIssuerName,
            final String cardAlias,
            final String maskedCardNo,
            final String cardImageUrl,
            final LocalDateTime openedAt
    ) {
        this.ownedCardId = ownedCardId;
        this.cardProductId = cardProductId;
        this.cardName = cardName;
        this.cardIssuerName = cardIssuerName;
        this.cardAlias = cardAlias;
        this.maskedCardNo = maskedCardNo;
        this.cardImageUrl = cardImageUrl;
        this.openedAt = openedAt;
    }

    public static OwnedCardResponse from(final OwnedCard ownedCard) {
        return new OwnedCardResponse(
                ownedCard.getId(),
                ownedCard.getCardProduct().getId(),
                ownedCard.getCardProduct().getCardName(),
                ownedCard.getCardProduct().getCardIssuerName(),
                ownedCard.getCardAlias(),
                ownedCard.getMaskedCardNo(),
                ownedCard.getCardProduct().getCardImageUrl(),
                ownedCard.getOpenedAt()
        );
    }
}
