package io.ssafy.p.j14c103.homerun.api.controller.pass.request;

import jakarta.validation.constraints.NotNull;

public class PassSubscribeRequest {

    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;

    @NotNull(message = "PASS 상품 ID는 필수입니다.")
    private Long passId;

    @NotNull(message = "출금 계좌 ID는 필수입니다.")
    private String sourceAccountId;

    @NotNull(message = "userKey는 필수입니다.")
    private String userKey;

    protected PassSubscribeRequest() {
    }

    public PassSubscribeRequest(final Long userId, final Long passId,
                                final String sourceAccountId, final String userKey) {
        this.userId = userId;
        this.passId = passId;
        this.sourceAccountId = sourceAccountId;
        this.userKey = userKey;
    }

    public Long getUserId() { return userId; }
    public Long getPassId() { return passId; }
    public String getSourceAccountId() { return sourceAccountId; }
    public String getUserKey() { return userKey; }
}
