package io.ssafy.p.j14c103.homerun.api.controller.pass.request;

import jakarta.validation.constraints.NotNull;

public class PassSaveRequest {

    @NotNull(message = "구독 ID는 필수입니다.")
    private Long subscriptionId;

    @NotNull(message = "출금 계좌 ID는 필수입니다.")
    private String sourceAccountId;

    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;

    @NotNull(message = "userKey는 필수입니다.")
    private String userKey;

    protected PassSaveRequest() {
    }

    public PassSaveRequest(final Long subscriptionId, final String sourceAccountId,
                           final Long userId, final String userKey) {
        this.subscriptionId = subscriptionId;
        this.sourceAccountId = sourceAccountId;
        this.userId = userId;
        this.userKey = userKey;
    }

    public Long getSubscriptionId() { return subscriptionId; }
    public String getSourceAccountId() { return sourceAccountId; }
    public Long getUserId() { return userId; }
    public String getUserKey() { return userKey; }
}
