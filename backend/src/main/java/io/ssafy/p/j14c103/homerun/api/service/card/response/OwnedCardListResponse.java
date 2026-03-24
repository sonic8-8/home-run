package io.ssafy.p.j14c103.homerun.api.service.card.response;

import java.util.List;
import lombok.Getter;

@Getter
public class OwnedCardListResponse {

    private final List<OwnedCardResponse> cards;

    private OwnedCardListResponse(final List<OwnedCardResponse> cards) {
        this.cards = List.copyOf(cards);
    }

    public static OwnedCardListResponse of(final List<OwnedCardResponse> cards) {
        return new OwnedCardListResponse(cards);
    }
}
