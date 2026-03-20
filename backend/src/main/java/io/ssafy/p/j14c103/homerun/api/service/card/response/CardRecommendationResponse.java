package io.ssafy.p.j14c103.homerun.api.service.card.response;

import java.util.List;
import lombok.Getter;

@Getter
public class CardRecommendationResponse {

    private final List<CardResponse> recommendations;

    private CardRecommendationResponse(final List<CardResponse> recommendations) {
        this.recommendations = List.copyOf(recommendations);
    }

    public static CardRecommendationResponse of(final List<CardResponse> recommendations) {
        return new CardRecommendationResponse(recommendations);
    }
}
