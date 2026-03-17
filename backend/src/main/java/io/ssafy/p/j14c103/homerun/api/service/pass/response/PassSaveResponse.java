package io.ssafy.p.j14c103.homerun.api.service.pass.response;

public class PassSaveResponse {

    private final int savedAmount;
    private final int totalSaved;
    private final int remainingBalance;

    private PassSaveResponse(final int savedAmount, final int totalSaved, final int remainingBalance) {
        this.savedAmount = savedAmount;
        this.totalSaved = totalSaved;
        this.remainingBalance = remainingBalance;
    }

    public static PassSaveResponse of(final int savedAmount, final int totalSaved, final int remainingBalance) {
        return new PassSaveResponse(savedAmount, totalSaved, remainingBalance);
    }

    public int getSavedAmount() { return savedAmount; }
    public int getTotalSaved() { return totalSaved; }
    public int getRemainingBalance() { return remainingBalance; }
}
