package io.ssafy.p.j14c103.homerun.api.service.seedmoney.response;

import java.time.LocalDateTime;

public class SeedmoneyAccountResponse {

    private final Long id;
    private final String bankName;
    private final String maskedAccountNo;
    private final Integer balance;
    private final LocalDateTime updatedAt;

    private SeedmoneyAccountResponse(
            final Long id, final String bankName, final String maskedAccountNo,
            final Integer balance, final LocalDateTime updatedAt) {
        this.id = id;
        this.bankName = bankName;
        this.maskedAccountNo = maskedAccountNo;
        this.balance = balance;
        this.updatedAt = updatedAt;
    }

    public static SeedmoneyAccountResponse of(
            final Long id, final String bankName, final String maskedAccountNo,
            final Integer balance, final LocalDateTime updatedAt) {
        return new SeedmoneyAccountResponse(id, bankName, maskedAccountNo, balance, updatedAt);
    }

    public Long getId() { return id; }
    public String getBankName() { return bankName; }
    public String getMaskedAccountNo() { return maskedAccountNo; }
    public Integer getBalance() { return balance; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
