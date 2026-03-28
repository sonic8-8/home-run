package io.ssafy.p.j14c103.homerun.api.service.pass.response;

public class PassSaveResponse {

    private final long savedAmount;
    private final long totalSaved;
    private final long subscriptionTotalSaved;
    private final long overallTotalSaved;
    private final long remainingBalance;

    private PassSaveResponse(
            final long savedAmount,
            final long subscriptionTotalSaved,
            final long overallTotalSaved,
            final long remainingBalance
    ) {
        this.savedAmount = savedAmount;
        this.totalSaved = subscriptionTotalSaved;
        this.subscriptionTotalSaved = subscriptionTotalSaved;
        this.overallTotalSaved = overallTotalSaved;
        this.remainingBalance = remainingBalance;
    }

    public static PassSaveResponse of(
            final long savedAmount,
            final long subscriptionTotalSaved,
            final long overallTotalSaved,
            final long remainingBalance
    ) {
        return new PassSaveResponse(savedAmount, subscriptionTotalSaved, overallTotalSaved, remainingBalance);
    }

    public long getSavedAmount() { return savedAmount; }
    public long getTotalSaved() { return totalSaved; }
    public long getSubscriptionTotalSaved() { return subscriptionTotalSaved; }
    public long getOverallTotalSaved() { return overallTotalSaved; }
    public long getRemainingBalance() { return remainingBalance; }
}
