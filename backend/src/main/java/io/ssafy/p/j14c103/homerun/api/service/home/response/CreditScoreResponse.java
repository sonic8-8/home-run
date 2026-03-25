package io.ssafy.p.j14c103.homerun.api.service.home.response;

import io.ssafy.p.j14c103.homerun.api.service.home.credit.CreditScore;
import lombok.Getter;

@Getter
public class CreditScoreResponse {

    private final int score;
    private final int grade;
    private final String gradeLabel;

    // FICO 개별 요소
    private final int paymentHistory;
    private final int amountsOwed;
    private final int creditLength;
    private final int creditMix;
    private final int newCredit;

    // SSAFY 원본 데이터 (참고용)
    private final String ratingName;
    private final long totalAsset;
    private final long totalDebt;
    private final long netAsset;

    private CreditScoreResponse(
        CreditScore css,
        String ratingName,
        long totalAsset,
        long totalDebt,
        long netAsset
    ) {
        this.score = css.getScore();
        this.grade = css.getGrade();
        this.gradeLabel = css.getGradeLabel();
        this.paymentHistory = css.getPaymentHistory();
        this.amountsOwed = css.getAmountsOwed();
        this.creditLength = css.getCreditLength();
        this.creditMix = css.getCreditMix();
        this.newCredit = css.getNewCredit();
        this.ratingName = ratingName;
        this.totalAsset = totalAsset;
        this.totalDebt = totalDebt;
        this.netAsset = netAsset;
    }

    public static CreditScoreResponse of(CreditScore css, String ratingName, long totalAsset) {
        return new CreditScoreResponse(css, ratingName, totalAsset, 0L, 0L);
    }

    public static CreditScoreResponse of(
        CreditScore css,
        String ratingName,
        long totalAsset,
        long totalDebt,
        long netAsset
    ) {
        return new CreditScoreResponse(css, ratingName, totalAsset, totalDebt, netAsset);
    }
}
