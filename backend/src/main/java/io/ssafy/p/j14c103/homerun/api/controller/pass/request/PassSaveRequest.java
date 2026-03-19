package io.ssafy.p.j14c103.homerun.api.controller.pass.request;

import jakarta.validation.constraints.NotNull;

public class PassSaveRequest {

    @NotNull(message = "구독 ID는 필수입니다.")
    private Long subscriptionId;

    @NotNull(message = "출금 계좌 ID는 필수입니다.")
    private String sourceAccountId;

    protected PassSaveRequest() {
    }

    public PassSaveRequest(final Long subscriptionId, final String sourceAccountId) {
        this.subscriptionId = subscriptionId;
        this.sourceAccountId = sourceAccountId;
    }

    public Long getSubscriptionId() { return subscriptionId; }
    public String getSourceAccountId() { return sourceAccountId; }
}
