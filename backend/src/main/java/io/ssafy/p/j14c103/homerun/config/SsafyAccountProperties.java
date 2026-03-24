package io.ssafy.p.j14c103.homerun.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ssafy.account")
public class SsafyAccountProperties {

    private final String bankCode;
    private final String bankName;
    private final String accountTypeUniqueNo;
    private final long mainInitialBalance;

    private SsafyAccountProperties(
            final String bankCode,
            final String bankName,
            final String accountTypeUniqueNo,
            final long mainInitialBalance) {
        this.bankCode = bankCode;
        this.bankName = bankName;
        this.accountTypeUniqueNo = accountTypeUniqueNo;
        this.mainInitialBalance = mainInitialBalance;
    }

    public static SsafyAccountProperties of(
            final String bankCode,
            final String bankName,
            final String accountTypeUniqueNo,
            final long mainInitialBalance) {
        if (bankCode == null || bankCode.isBlank()) {
            throw new IllegalArgumentException("SSAFY 계좌 은행 코드는 필수입니다.");
        }
        if (bankName == null || bankName.isBlank()) {
            throw new IllegalArgumentException("SSAFY 계좌 은행 이름은 필수입니다.");
        }
        if (mainInitialBalance < 0) {
            throw new IllegalArgumentException("초기 잔액은 0 이상이어야 합니다.");
        }

        return new SsafyAccountProperties(bankCode, bankName, accountTypeUniqueNo, mainInitialBalance);
    }

    public String getBankCode() {
        return bankCode;
    }

    public String getBankName() {
        return bankName;
    }

    public String getAccountTypeUniqueNo() {
        return accountTypeUniqueNo;
    }

    public long getMainInitialBalance() {
        return mainInitialBalance;
    }
}
