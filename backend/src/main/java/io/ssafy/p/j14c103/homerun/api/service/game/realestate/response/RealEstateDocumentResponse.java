package io.ssafy.p.j14c103.homerun.api.service.game.realestate.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;

@Getter
public class RealEstateDocumentResponse {

    private final Long propertyId;
    private final String propertyName;
    private final String address;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final Long salePrice;
    private final String documentType;
    private final List<RegistryRowResponse> gapguRows;
    private final List<RegistryRowResponse> eulguRows;
    private final SolutionResponse solution;

    private RealEstateDocumentResponse(
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
        this.propertyId = propertyId;
        this.propertyName = propertyName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.salePrice = salePrice;
        this.documentType = documentType;
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

    public Long propertyId() {
        return propertyId;
    }

    public String propertyName() {
        return propertyName;
    }

    public String address() {
        return address;
    }

    public BigDecimal latitude() {
        return latitude;
    }

    public BigDecimal longitude() {
        return longitude;
    }

    public Long salePrice() {
        return salePrice;
    }

    public String documentType() {
        return documentType;
    }

    public List<RegistryRowResponse> gapguRows() {
        return gapguRows;
    }

    public List<RegistryRowResponse> eulguRows() {
        return eulguRows;
    }

    public SolutionResponse solution() {
        return solution;
    }

    @Getter
    public static class RegistryRowResponse {

        private final String rankNo;
        private final String purpose;
        private final String receipt;
        private final String reason;
        private final String details;

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
            return new RegistryRowResponse(rankNo, purpose, receipt, reason, details);
        }

        public String rankNo() {
            return rankNo;
        }

        public String purpose() {
            return purpose;
        }

        public String receipt() {
            return receipt;
        }

        public String reason() {
            return reason;
        }

        public String details() {
            return details;
        }
    }

    @Getter
    public static class SolutionResponse {

        private final String verdict;
        private final SectionSolutionResponse gapgu;
        private final SectionSolutionResponse eulgu;

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
            return new SolutionResponse(verdict, gapgu, eulgu);
        }

        public String verdict() {
            return verdict;
        }

        public SectionSolutionResponse gapgu() {
            return gapgu;
        }

        public SectionSolutionResponse eulgu() {
            return eulgu;
        }
    }

    @Getter
    public static class SectionSolutionResponse {

        private final String verdict;
        private final String issueSummary;
        private final List<String> keyPoints;
        private final String feedbackCorrect;
        private final String feedbackWrong;

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
            return new SectionSolutionResponse(
                verdict,
                issueSummary,
                keyPoints,
                feedbackCorrect,
                feedbackWrong
            );
        }

        public String verdict() {
            return verdict;
        }

        public String issueSummary() {
            return issueSummary;
        }

        public List<String> keyPoints() {
            return keyPoints;
        }

        public String feedbackCorrect() {
            return feedbackCorrect;
        }

        public String feedbackWrong() {
            return feedbackWrong;
        }
    }
}
