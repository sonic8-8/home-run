package io.ssafy.p.j14c103.homerun.api.service.game.realestate.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class RealEstateDocumentResponse {

    private final Long propertyId;
    private final String propertyName;
    private final String address;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final Long salePrice;
    private final List<RegistryRowResponse> gapguRows;
    private final List<RegistryRowResponse> eulguRows;
    private final SolutionResponse solution;

    @Builder
    private RealEstateDocumentResponse(
        final Long propertyId,
        final String propertyName,
        final String address,
        final BigDecimal latitude,
        final BigDecimal longitude,
        final Long salePrice,
        final List<RegistryRowResponse> gapguRows,
        final List<RegistryRowResponse> eulguRows,
        final SolutionResponse solution
    ) {
        this.propertyId = propertyId;
        this.propertyName = propertyName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.salePrice = salePrice;
        this.gapguRows = List.copyOf(gapguRows);
        this.eulguRows = List.copyOf(eulguRows);
        this.solution = solution;
    }

    public static RealEstateDocumentResponse of(
        final Long propertyId,
        final String propertyName,
        final String address,
        final BigDecimal latitude,
        final BigDecimal longitude,
        final Long salePrice,
        final List<RegistryRowResponse> gapguRows,
        final List<RegistryRowResponse> eulguRows,
        final SolutionResponse solution
    ) {
        return RealEstateDocumentResponse.builder()
            .propertyId(propertyId)
            .propertyName(propertyName)
            .address(address)
            .latitude(latitude)
            .longitude(longitude)
            .salePrice(salePrice)
            .gapguRows(gapguRows)
            .eulguRows(eulguRows)
            .solution(solution)
            .build();
    }

    @Getter
    public static class RegistryRowResponse {

        private final String rankNo;
        private final String purpose;
        private final String receipt;
        private final String reason;
        private final String details;

        @Builder
        private RegistryRowResponse(
            final String rankNo,
            final String purpose,
            final String receipt,
            final String reason,
            final String details
        ) {
            this.rankNo = rankNo;
            this.purpose = purpose;
            this.receipt = receipt;
            this.reason = reason;
            this.details = details;
        }

        public static RegistryRowResponse of(
            final String rankNo,
            final String purpose,
            final String receipt,
            final String reason,
            final String details
        ) {
            return RegistryRowResponse.builder()
                .rankNo(rankNo)
                .purpose(purpose)
                .receipt(receipt)
                .reason(reason)
                .details(details)
                .build();
        }
    }

    @Getter
    public static class SolutionResponse {

        private final String verdict;
        private final SectionSolutionResponse gapgu;
        private final SectionSolutionResponse eulgu;

        @Builder
        private SolutionResponse(
            final String verdict,
            final SectionSolutionResponse gapgu,
            final SectionSolutionResponse eulgu
        ) {
            this.verdict = verdict;
            this.gapgu = gapgu;
            this.eulgu = eulgu;
        }

        public static SolutionResponse of(
            final String verdict,
            final SectionSolutionResponse gapgu,
            final SectionSolutionResponse eulgu
        ) {
            return SolutionResponse.builder()
                .verdict(verdict)
                .gapgu(gapgu)
                .eulgu(eulgu)
                .build();
        }
    }

    @Getter
    public static class SectionSolutionResponse {

        private final String verdict;
        private final String issueSummary;
        private final List<String> keyPoints;
        private final String feedbackCorrect;
        private final String feedbackWrong;

        @Builder
        private SectionSolutionResponse(
            final String verdict,
            final String issueSummary,
            final List<String> keyPoints,
            final String feedbackCorrect,
            final String feedbackWrong
        ) {
            this.verdict = verdict;
            this.issueSummary = issueSummary;
            this.keyPoints = List.copyOf(keyPoints);
            this.feedbackCorrect = feedbackCorrect;
            this.feedbackWrong = feedbackWrong;
        }

        public static SectionSolutionResponse of(
            final String verdict,
            final String issueSummary,
            final List<String> keyPoints,
            final String feedbackCorrect,
            final String feedbackWrong
        ) {
            return SectionSolutionResponse.builder()
                .verdict(verdict)
                .issueSummary(issueSummary)
                .keyPoints(keyPoints)
                .feedbackCorrect(feedbackCorrect)
                .feedbackWrong(feedbackWrong)
                .build();
        }
    }
}
