package io.ssafy.p.j14c103.homerun.domain.seedmoney;

public enum TransactionType {

    SAVE("저축"),
    DEPOSIT("입금"),
    TRANSFER("송금");

    private final String displayName;

    TransactionType(final String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
