package io.ssafy.p.j14c103.homerun.api.service.seedmoney.response;

public class SeedmoneyAccountResponse {

    private final String bankName;
    private final String accountNumber;
    private final Long balance;

    private SeedmoneyAccountResponse(
            final String bankName, final String accountNumber, final Long balance) {
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    public static SeedmoneyAccountResponse of(
            final String bankName, final String accountNumber, final Long balance) {
        return new SeedmoneyAccountResponse(bankName, accountNumber, balance);
    }

    public static SeedmoneyAccountResponse of(
            final String bankName, final String accountNumber, final long balance) {
        return of(bankName, accountNumber, Long.valueOf(balance));
    }

    public String getBankName() { return bankName; }
    public String getAccountNumber() { return accountNumber; }
    public Long getBalance() { return balance; }
}
