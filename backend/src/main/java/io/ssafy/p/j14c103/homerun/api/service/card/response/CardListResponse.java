package io.ssafy.p.j14c103.homerun.api.service.card.response;

import java.util.List;
import lombok.Getter;

@Getter
public class CardListResponse {

    private final List<CardResponse> cards;

    private CardListResponse(final List<CardResponse> cards) {
        this.cards = List.copyOf(cards);
    }

    public static CardListResponse of(final List<CardResponse> cards) {
        return new CardListResponse(cards);
    }
}
