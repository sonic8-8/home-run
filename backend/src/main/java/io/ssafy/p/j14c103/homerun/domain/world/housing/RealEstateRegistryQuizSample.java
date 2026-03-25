package io.ssafy.p.j14c103.homerun.domain.world.housing;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RealEstateRegistryQuizSample {

    private String quizVerdict;
    private List<RealEstateRegistryRow> rows;
    private String issueSummary;
    private List<String> keyPoints;
    private String feedbackCorrect;
    private String feedbackWrong;

    private RealEstateRegistryQuizSample(
        String quizVerdict,
        List<RealEstateRegistryRow> rows,
        String issueSummary,
        List<String> keyPoints,
        String feedbackCorrect,
        String feedbackWrong
    ) {
        this.quizVerdict = quizVerdict;
        this.rows = rows;
        this.issueSummary = issueSummary;
        this.keyPoints = keyPoints;
        this.feedbackCorrect = feedbackCorrect;
        this.feedbackWrong = feedbackWrong;
    }

    public static RealEstateRegistryQuizSample create(
        String quizVerdict,
        List<RealEstateRegistryRow> rows,
        String issueSummary,
        List<String> keyPoints,
        String feedbackCorrect,
        String feedbackWrong
    ) {
        return new RealEstateRegistryQuizSample(
            quizVerdict,
            rows,
            issueSummary,
            keyPoints,
            feedbackCorrect,
            feedbackWrong
        );
    }
}
