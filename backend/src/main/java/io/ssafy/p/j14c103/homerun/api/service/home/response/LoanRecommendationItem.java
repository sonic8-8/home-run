package io.ssafy.p.j14c103.homerun.api.service.home.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoanRecommendationItem {

    private final String productId;
    private final String bankName;
    private final String bankLogoUrl;
    private final String productName;
    private final String productType;
    private final double minRate;
    private final double maxRate;
    private final double estimatedRate;  // CSS 등급 기반 예상금리
    private final String joinWay;
    private final String creditProductTypeName;
    private final Double averageRate;
    private final String rateTypeName;
    private final String repaymentTypeName;
    private final String loanLimit;
    private final String mortgageTypeName;
}
