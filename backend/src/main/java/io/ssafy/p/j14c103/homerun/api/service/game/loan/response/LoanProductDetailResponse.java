package io.ssafy.p.j14c103.homerun.api.service.game.loan.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * 대출 상품 상세 응답.
 */
@Getter
@Builder
public class LoanProductDetailResponse {

    private final String productId;
    private final String bankName;
    private final String bankLogoUrl;
    private final String productName;
    private final String productType;
    private final double minRate;
    private final double maxRate;
    private final List<String> features;
}
