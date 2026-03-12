package io.ssafy.p.j14c103.homerun.api.controller.pass.request;

import jakarta.validation.constraints.NotNull;

public class PassSubscribeRequest {

    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;

    @NotNull(message = "PASS 상품 ID는 필수입니다.")
    private Long passProductId;

    @NotNull(message = "출금 계좌번호는 필수입니다.")
    private String sourceAccountNo;

    @NotNull(message = "userKey는 필수입니다.")
    private String userKey;

    protected PassSubscribeRequest() {
    }

    public PassSubscribeRequest(final Long userId, final Long passProductId,
                                final String sourceAccountNo, final String userKey) {
        this.userId = userId;
        this.passProductId = passProductId;
        this.sourceAccountNo = sourceAccountNo;
        this.userKey = userKey;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getPassProductId() {
        return passProductId;
    }

    public String getSourceAccountNo() {
        return sourceAccountNo;
    }

    public String getUserKey() {
        return userKey;
    }
}
