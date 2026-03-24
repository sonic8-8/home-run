package io.ssafy.p.j14c103.homerun.api.service.card.response;

import java.util.List;
import lombok.Getter;

@Getter
public class CardTransactionListResponse {

    private final List<CardTransactionDetailResponse> transactions;

    private CardTransactionListResponse(final List<CardTransactionDetailResponse> transactions) {
        this.transactions = List.copyOf(transactions);
    }

    public static CardTransactionListResponse of(final List<CardTransactionDetailResponse> transactions) {
        return new CardTransactionListResponse(transactions);
    }
}
