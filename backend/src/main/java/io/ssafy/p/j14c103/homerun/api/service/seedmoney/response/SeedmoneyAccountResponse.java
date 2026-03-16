package io.ssafy.p.j14c103.homerun.api.service.seedmoney.response;

import java.time.LocalDateTime;

public class SeedmoneyAccountResponse {

    private final Long id;
    private final String bankName;
    private final String accountNumber;
    private final Integer balance;
    private final LocalDateTime updatedAt;

    private SeedmoneyAccountResponse(
            final Long id, final String bankName, final String accountNumber,
            final Integer balance, final LocalDateTime updatedAt) {
        this.id = id;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.updatedAt = updatedAt;
    }

    public static SeedmoneyAccountResponse of(
            final Long id, final String bankName, final String accountNumber,
            final Integer balance, final LocalDateTime updatedAt) {
        return new SeedmoneyAccountResponse(id, bankName, accountNumber, balance, updatedAt);
    }

    public Long getId() { return id; }
    public String getBankName() { return bankName; }
    public String getAccountNumber() { return accountNumber; }
    public Integer getBalance() { return balance; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
