package io.ssafy.p.j14c103.homerun.api.service.game.loan.response;

import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.LoanApplication;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

/**
 * 대출 심사 신청 응답.
 */
@Getter
public class LoanApplyResponse {

    private final Integer applicationId;
    private final String status;
    private final RequestInfo requestInfo;
    private final Result result;

    @Builder(access = AccessLevel.PRIVATE)
    private LoanApplyResponse(
        final Integer applicationId,
        final String status,
        final RequestInfo requestInfo,
        final Result result
    ) {
        this.applicationId = applicationId;
        this.status = status;
        this.requestInfo = requestInfo;
        this.result = result;
    }

    public Integer applicationId() {
        return applicationId;
    }

    public String status() {
        return status;
    }

    public RequestInfo requestInfo() {
        return requestInfo;
    }

    public Result result() {
        return result;
    }

    @Getter
    public static class RequestInfo {

        private final String applicationDate;

        @Builder(access = AccessLevel.PRIVATE)
        private RequestInfo(final String applicationDate) {
            this.applicationDate = applicationDate;
        }

        public static RequestInfo of(final String applicationDate) {
            return RequestInfo.builder()
                .applicationDate(applicationDate)
                .build();
        }

        public String applicationDate() {
            return applicationDate;
        }
    }

    @Getter
    public static class Result {

        private final Integer maxLoanAmount;
        private final String rejectionReason;

        @Builder(access = AccessLevel.PRIVATE)
        private Result(final Integer maxLoanAmount, final String rejectionReason) {
            this.maxLoanAmount = maxLoanAmount;
            this.rejectionReason = rejectionReason;
        }

        public static Result of(final Integer maxLoanAmount, final String rejectionReason) {
            return Result.builder()
                .maxLoanAmount(maxLoanAmount)
                .rejectionReason(rejectionReason)
                .build();
        }

        public Integer maxLoanAmount() {
            return maxLoanAmount;
        }

        public String rejectionReason() {
            return rejectionReason;
        }
    }

    public static LoanApplyResponse from(final LoanApplication application) {
        return LoanApplyResponse.builder()
            .applicationId(application.getLoanApplicationId())
            .status(application.getApplicationStatus().name())
            .requestInfo(RequestInfo.of(
                application.getAppliedAt() != null
                    ? application.getAppliedAt().toLocalDate().toString()
                    : null
            ))
            .result(Result.of(
                application.getApprovedLimitAmount(),
                application.getRejectionReason()
            ))
            .build();
    }
}
