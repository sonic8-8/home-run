package io.ssafy.p.j14c103.homerun.api.service.seedmoney.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SeedmoneyAccountResponse {

    private final Long id;
    private final String accountNumber;
    private final BigDecimal balance;
    private final LocalDateTime updatedAt;

    private SeedmoneyAccountResponse(
            final Long id,
            final String accountNumber,
            final BigDecimal balance,
            final LocalDateTime updatedAt) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.updatedAt = updatedAt;
    }

    public static SeedmoneyAccountResponse of(
            final Long id,
            final String accountNumber,
            final BigDecimal balance,
            final LocalDateTime updatedAt) {
        return new SeedmoneyAccountResponse(id, accountNumber, balance, updatedAt);
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
