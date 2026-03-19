package io.ssafy.p.j14c103.homerun.api.controller.seedmoney.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SeedmoneyTransferRequest {

    @NotNull(message = "{validation.seedmoney.transfer.amount.notNull}")
    @Positive(message = "{validation.seedmoney.transfer.amount.positive}")
    private Long amount;

    @NotNull(message = "{validation.seedmoney.transfer.toAccountNumber.notNull}")
    private String toAccountNumber;

    protected SeedmoneyTransferRequest() {
    }

    public SeedmoneyTransferRequest(final Long amount, final String toAccountNumber) {
        this.amount = amount;
        this.toAccountNumber = toAccountNumber;
    }

    public Long getAmount() { return amount; }
    public String getToAccountNumber() { return toAccountNumber; }
}
