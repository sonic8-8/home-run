package io.ssafy.p.j14c103.homerun.api.service.pass.response;

public class PassSaveResponse {

    private final int savedAmount;
    private final int totalSaved;
    private final int subscriptionTotalSaved;
    private final int overallTotalSaved;
    private final int remainingBalance;

    private PassSaveResponse(
            final int savedAmount,
            final int subscriptionTotalSaved,
            final int overallTotalSaved,
            final int remainingBalance
    ) {
        this.savedAmount = savedAmount;
        this.totalSaved = subscriptionTotalSaved;
        this.subscriptionTotalSaved = subscriptionTotalSaved;
        this.overallTotalSaved = overallTotalSaved;
        this.remainingBalance = remainingBalance;
    }

    public static PassSaveResponse of(
            final int savedAmount,
            final int subscriptionTotalSaved,
            final int overallTotalSaved,
            final int remainingBalance
    ) {
        return new PassSaveResponse(savedAmount, subscriptionTotalSaved, overallTotalSaved, remainingBalance);
    }

    public int getSavedAmount() { return savedAmount; }
    public int getTotalSaved() { return totalSaved; }
    public int getSubscriptionTotalSaved() { return subscriptionTotalSaved; }
    public int getOverallTotalSaved() { return overallTotalSaved; }
    public int getRemainingBalance() { return remainingBalance; }
}
