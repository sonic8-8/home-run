package io.ssafy.p.j14c103.homerun.api.service.card.response;

import io.ssafy.p.j14c103.homerun.domain.card.CardTransaction;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class CardTransactionDetailResponse {

    private final Long cardTransactionId;
    private final Long ownedCardId;
    private final String cardName;
    private final String categoryId;
    private final String categoryName;
    private final String merchantName;
    private final Long paymentAmount;
    private final LocalDate paymentDate;

    private CardTransactionDetailResponse(
            final Long cardTransactionId,
            final Long ownedCardId,
            final String cardName,
            final String categoryId,
            final String categoryName,
            final String merchantName,
            final Long paymentAmount,
            final LocalDate paymentDate
    ) {
        this.cardTransactionId = cardTransactionId;
        this.ownedCardId = ownedCardId;
        this.cardName = cardName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.merchantName = merchantName;
        this.paymentAmount = paymentAmount;
        this.paymentDate = paymentDate;
    }

    public static CardTransactionDetailResponse from(final CardTransaction transaction) {
        return new CardTransactionDetailResponse(
                transaction.getId(),
                transaction.getOwnedCard().getId(),
                transaction.getOwnedCard().getCardProduct().getCardName(),
                transaction.getCategoryId(),
                transaction.getCategoryName(),
                transaction.getMerchantName(),
                transaction.getPaymentAmount(),
                transaction.getPaymentDate()
        );
    }
}
