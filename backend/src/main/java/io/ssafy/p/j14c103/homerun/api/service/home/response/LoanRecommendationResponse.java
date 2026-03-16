package io.ssafy.p.j14c103.homerun.api.service.home.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LoanRecommendationResponse {

    private final int cssScore;
    private final int cssGrade;
    private final String cssGradeLabel;
    private final double estimatedMinRate;

    private final List<LoanRecommendationItem> creditLoans;      // 개인신용대출
    private final List<LoanRecommendationItem> jeonseLoans;      // 전세자금대출
    private final List<LoanRecommendationItem> mortgageLoans;    // 주택담보대출

    public static LoanRecommendationResponse empty() {
        return LoanRecommendationResponse.builder()
                .cssScore(0)
                .cssGrade(5)
                .cssGradeLabel("N/A")
                .estimatedMinRate(0)
                .creditLoans(List.of())
                .jeonseLoans(List.of())
                .mortgageLoans(List.of())
                .build();
    }
}
