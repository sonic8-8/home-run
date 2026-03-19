package io.ssafy.p.j14c103.homerun.api.controller.pass.request;

import jakarta.validation.constraints.NotNull;

public class PassSaveRequest {

    @NotNull(message = "{validation.pass.save.subscriptionId.notNull}")
    private Long subscriptionId;

    @NotNull(message = "{validation.pass.save.sourceAccountId.notNull}")
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
