package io.ssafy.p.j14c103.homerun.api.service.home.response;

import lombok.Getter;

import java.util.List;

@Getter
public class LoanRecommendationResponse {

    private final List<LoanRecommendationItem> recommendations;

    private LoanRecommendationResponse(final List<LoanRecommendationItem> recommendations) {
        this.recommendations = recommendations;
    }

    public static LoanRecommendationResponse of(final List<LoanRecommendationItem> recommendations) {
        return new LoanRecommendationResponse(recommendations);
    }

    public static LoanRecommendationResponse empty() {
        return new LoanRecommendationResponse(List.of());
    }
}
