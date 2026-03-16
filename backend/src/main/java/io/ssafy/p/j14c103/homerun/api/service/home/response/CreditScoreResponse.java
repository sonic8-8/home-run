package io.ssafy.p.j14c103.homerun.api.service.home.response;

public class CreditScoreResponse {

    private final String ratingName;
    private final long demandDepositAsset;
    private final long depositSavingsAsset;
    private final long totalAsset;

    private CreditScoreResponse(final String ratingName, final long demandDepositAsset,
                                 final long depositSavingsAsset, final long totalAsset) {
        this.ratingName = ratingName;
        this.demandDepositAsset = demandDepositAsset;
        this.depositSavingsAsset = depositSavingsAsset;
        this.totalAsset = totalAsset;
    }

    public static CreditScoreResponse of(final String ratingName, final long demandDepositAsset,
                                          final long depositSavingsAsset, final long totalAsset) {
        return new CreditScoreResponse(ratingName, demandDepositAsset, depositSavingsAsset, totalAsset);
    }

    public String getRatingName() { return ratingName; }
    public long getDemandDepositAsset() { return demandDepositAsset; }
    public long getDepositSavingsAsset() { return depositSavingsAsset; }
    public long getTotalAsset() { return totalAsset; }
}
