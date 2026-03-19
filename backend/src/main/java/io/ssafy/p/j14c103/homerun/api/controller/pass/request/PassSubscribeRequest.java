package io.ssafy.p.j14c103.homerun.api.controller.pass.request;

import jakarta.validation.constraints.NotNull;

public class PassSubscribeRequest {

    @NotNull(message = "{validation.pass.subscribe.passId.notNull}")
    private Long passId;

    @NotNull(message = "{validation.pass.subscribe.sourceAccountId.notNull}")
    private String sourceAccountId;

    protected PassSubscribeRequest() {
    }

    public PassSubscribeRequest(final Long passId, final String sourceAccountId) {
        this.passId = passId;
        this.sourceAccountId = sourceAccountId;
    }

    public Long getPassId() { return passId; }
    public String getSourceAccountId() { return sourceAccountId; }
}
