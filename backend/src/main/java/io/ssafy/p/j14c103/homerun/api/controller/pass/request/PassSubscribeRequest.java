package io.ssafy.p.j14c103.homerun.api.controller.pass.request;

import jakarta.validation.constraints.NotNull;

public class PassSubscribeRequest {

    @NotNull(message = "PASS 상품 ID는 필수입니다.")
    private Long passId;

    @NotNull(message = "출금 계좌 ID는 필수입니다.")
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
