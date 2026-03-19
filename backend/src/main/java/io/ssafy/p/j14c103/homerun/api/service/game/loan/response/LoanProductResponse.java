package io.ssafy.p.j14c103.homerun.api.service.game.loan.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 대출 상품 목록 응답 항목.
 */
@Getter
@Builder
public class LoanProductResponse {

    private final String productId;
    private final String bankName;
    private final String bankLogoUrl;
    private final String productName;
    private final String productType;
    private final double minRate;
    private final double maxRate;
}
