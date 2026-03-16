package io.ssafy.p.j14c103.homerun.api.service.seedmoney.response;

public class SeedmoneyTransactionResponse {

    private final String transactionId;
    private final Integer remainingBalance;

    private SeedmoneyTransactionResponse(final String transactionId, final Integer remainingBalance) {
        this.transactionId = transactionId;
        this.remainingBalance = remainingBalance;
    }

    public static SeedmoneyTransactionResponse of(final String transactionId, final Integer remainingBalance) {
        return new SeedmoneyTransactionResponse(transactionId, remainingBalance);
    }

    public String getTransactionId() { return transactionId; }
    public Integer getRemainingBalance() { return remainingBalance; }
}
