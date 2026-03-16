package io.ssafy.p.j14c103.homerun.api.service.seedmoney.response;

public class SeedmoneyAccountResponse {

    private final String bankName;
    private final String accountNumber;
    private final Integer balance;

    private SeedmoneyAccountResponse(
            final String bankName, final String accountNumber, final Integer balance) {
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    public static SeedmoneyAccountResponse of(
            final String bankName, final String accountNumber, final Integer balance) {
        return new SeedmoneyAccountResponse(bankName, accountNumber, balance);
    }

    public String getBankName() { return bankName; }
    public String getAccountNumber() { return accountNumber; }
    public Integer getBalance() { return balance; }
}
