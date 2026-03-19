package io.ssafy.p.j14c103.homerun.api.controller.seedmoney.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SeedmoneyDepositRequest {

    @NotNull(message = "{validation.seedmoney.deposit.amount.notNull}")
    @Positive(message = "{validation.seedmoney.deposit.amount.positive}")
    private Long amount;

    @NotNull(message = "{validation.seedmoney.deposit.fromAccountNumber.notNull}")
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
