package io.ssafy.p.j14c103.homerun.api.service.seedmoney.response;

public class SeedmoneyTransactionResponse {

    private final String transactionId;
    private final Long remainingBalance;

    private SeedmoneyTransactionResponse(final String transactionId, final Long remainingBalance) {
        this.transactionId = transactionId;
        this.remainingBalance = remainingBalance;
    }

    public static SeedmoneyTransactionResponse of(final String transactionId, final Long remainingBalance) {
        return new SeedmoneyTransactionResponse(transactionId, remainingBalance);
    }

    public static SeedmoneyTransactionResponse of(final String transactionId, final long remainingBalance) {
        return of(transactionId, Long.valueOf(remainingBalance));
    }

    public String getTransactionId() { return transactionId; }
    public Long getRemainingBalance() { return remainingBalance; }
}
