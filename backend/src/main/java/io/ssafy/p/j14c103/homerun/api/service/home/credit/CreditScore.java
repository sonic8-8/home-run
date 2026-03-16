package io.ssafy.p.j14c103.homerun.api.service.home.credit;

import lombok.Builder;
import lombok.Getter;

/**
 * FICO 기반 자체 CSS 신용점수 결과 DTO.
 * 1000점 만점, 5등급 체계.
 */
@Getter
@Builder
public class CreditScore {

    private final int score;               // 0~1000
    private final int grade;               // 1~5
    private final String gradeLabel;       // Exceptional, Very Good, Good, Fair, Poor

    // FICO 5대 요소 개별 점수
    private final int paymentHistory;      // /350
    private final int amountsOwed;         // /300
    private final int creditLength;        // /150
    private final int creditMix;           // /100
    private final int newCredit;           // /100

    public static CreditScore of(int paymentHistory, int amountsOwed,
                                  int creditLength, int creditMix, int newCredit) {
        int total = paymentHistory + amountsOwed + creditLength + creditMix + newCredit;
        int grade = toGrade(total);
        String label = toLabel(grade);

        return CreditScore.builder()
                .score(total)
                .grade(grade)
                .gradeLabel(label)
                .paymentHistory(paymentHistory)
                .amountsOwed(amountsOwed)
                .creditLength(creditLength)
                .creditMix(creditMix)
                .newCredit(newCredit)
                .build();
    }

    private static int toGrade(int score) {
        if (score >= 900) return 1;
        if (score >= 800) return 2;
        if (score >= 700) return 3;
        if (score >= 600) return 4;
        return 5;
    }

    private static String toLabel(int grade) {
        return switch (grade) {
            case 1 -> "Exceptional";
            case 2 -> "Very Good";
            case 3 -> "Good";
            case 4 -> "Fair";
            default -> "Poor";
        };
    }

    /**
     * 등급에 따른 예상 금리 계수 (0.0 ~ 1.0).
     * 1등급 = minRate, 5등급 = maxRate
     */
    public double rateCoefficient() {
        return switch (grade) {
            case 1 -> 0.0;
            case 2 -> 0.25;
            case 3 -> 0.50;
            case 4 -> 0.75;
            default -> 1.0;
        };
    }
}
