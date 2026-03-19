package io.ssafy.p.j14c103.homerun.api.controller.seedmoney.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SeedmoneyDepositRequest {

    @NotNull(message = "입금 금액은 필수입니다.")
    @Positive(message = "입금 금액은 0보다 커야 합니다.")
    private Long amount;

    @NotNull(message = "출금 계좌번호는 필수입니다.")
    private String fromAccountNumber;

    protected SeedmoneyDepositRequest() {
    }

    public SeedmoneyDepositRequest(final Long amount, final String fromAccountNumber) {
        this.amount = amount;
        this.fromAccountNumber = fromAccountNumber;
    }

    public Long getAmount() { return amount; }
    public String getFromAccountNumber() { return fromAccountNumber; }
}
