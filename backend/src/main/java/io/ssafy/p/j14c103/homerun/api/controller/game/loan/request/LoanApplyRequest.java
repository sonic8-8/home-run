package io.ssafy.p.j14c103.homerun.api.controller.game.loan.request;

import io.ssafy.p.j14c103.homerun.api.service.game.loan.request.LoanApplyServiceRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
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

    @NotBlank(message = "{validation.loan.apply.productId.notBlank}")
    private String productId;

    @Positive(message = "{validation.loan.apply.propertyId.positive}")
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

    public LoanApplyServiceRequest toServiceRequest() {
        return LoanApplyServiceRequest.of(productId, propertyId);
    }
}
