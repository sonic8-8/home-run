package io.ssafy.p.j14c103.homerun.api.dto.seedmoney;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SeedmoneyTransferRequest {

    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;

    @NotNull(message = "userKey는 필수입니다.")
    private String userKey;

    @NotNull(message = "이체 금액은 필수입니다.")
    @Positive(message = "이체 금액은 0보다 커야 합니다.")
    private Long amount;

    @NotNull(message = "입금 계좌번호는 필수입니다.")
    private String toAccountNo;

    protected SeedmoneyTransferRequest() {
    }

    public SeedmoneyTransferRequest(final Long userId, final String userKey,
                                    final Long amount, final String toAccountNo) {
        this.userId = userId;
        this.userKey = userKey;
        this.amount = amount;
        this.toAccountNo = toAccountNo;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserKey() {
        return userKey;
    }

    public Long getAmount() {
        return amount;
    }

    public String getToAccountNo() {
        return toAccountNo;
    }
}
