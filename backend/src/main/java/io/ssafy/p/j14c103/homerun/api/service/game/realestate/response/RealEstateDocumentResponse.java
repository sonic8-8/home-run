package io.ssafy.p.j14c103.homerun.api.service.game.realestate.response;

import java.math.BigDecimal;
import java.util.List;

public record RealEstateDocumentResponse(
    Long propertyId,
    String propertyName,
    String address,
    BigDecimal latitude,
    BigDecimal longitude,
    Long salePrice,
    String documentType,
    List<RegistryRowResponse> gapguRows,
    List<RegistryRowResponse> eulguRows,
    SolutionResponse solution
) {

    public RealEstateDocumentResponse {
        gapguRows = List.copyOf(gapguRows);
        eulguRows = List.copyOf(eulguRows);
    }

    public static RealEstateDocumentResponse of(
        final Long propertyId,
        final String propertyName,
        final String address,
        final BigDecimal latitude,
        final BigDecimal longitude,
        final Long salePrice,
        final String documentType,
        final List<RegistryRowResponse> gapguRows,
        final List<RegistryRowResponse> eulguRows,
        final SolutionResponse solution
    ) {
        return new RealEstateDocumentResponse(
            propertyId,
            propertyName,
            address,
            latitude,
            longitude,
            salePrice,
            documentType,
            gapguRows,
            eulguRows,
            solution
        );
    }

    public record RegistryRowResponse(
        String rankNo,
        String purpose,
        String receipt,
        String reason,
        String details
    ) {

        public static RegistryRowResponse of(
            final String rankNo,
            final String purpose,
            final String receipt,
            final String reason,
            final String details
        ) {
            return new RegistryRowResponse(rankNo, purpose, receipt, reason, details);
        }
    }

    public record SolutionResponse(
        String verdict,
        SectionSolutionResponse gapgu,
        SectionSolutionResponse eulgu
    ) {

        public static SolutionResponse of(
            final String verdict,
            final SectionSolutionResponse gapgu,
            final SectionSolutionResponse eulgu
        ) {
            return new SolutionResponse(verdict, gapgu, eulgu);
        }
    }

    public record SectionSolutionResponse(
        String verdict,
        String issueSummary,
        List<String> keyPoints,
        String feedbackCorrect,
        String feedbackWrong
    ) {

        public SectionSolutionResponse {
            keyPoints = List.copyOf(keyPoints);
        }

        public static SectionSolutionResponse of(
            final String verdict,
            final String issueSummary,
            final List<String> keyPoints,
            final String feedbackCorrect,
            final String feedbackWrong
        ) {
            return new SectionSolutionResponse(
                verdict,
                issueSummary,
                keyPoints,
                feedbackCorrect,
                feedbackWrong
            );
        }
    }
}
