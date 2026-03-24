package io.ssafy.p.j14c103.homerun.api.controller.game.loan.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 대출 심사 신청 요청.
 */
@Getter
@NoArgsConstructor
public class LoanApplyRequest {

    private String productId;
    private Long propertyId;

    @Builder(access = AccessLevel.PRIVATE)
    private LoanApplyRequest(final String productId, final Long propertyId) {
        this.productId = productId;
        this.propertyId = propertyId;
    }

    public static LoanApplyRequest of(final String productId, final Long propertyId) {
        return LoanApplyRequest.builder()
            .productId(productId)
            .propertyId(propertyId)
            .build();
    }

    public String productId() {
        return productId;
    }

    public Long propertyId() {
        return propertyId;
    }
}
