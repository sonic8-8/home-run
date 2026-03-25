package io.ssafy.p.j14c103.homerun.api.service.game.loan.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoanApplyServiceRequest {

    private String productId;
    private Long propertyId;

    @Builder(access = AccessLevel.PRIVATE)
    private LoanApplyServiceRequest(final String productId, final Long propertyId) {
        this.productId = productId;
        this.propertyId = propertyId;
    }

    public static LoanApplyServiceRequest of(final String productId, final Long propertyId) {
        return LoanApplyServiceRequest.builder()
            .productId(productId)
            .propertyId(propertyId)
            .build();
    }
}
