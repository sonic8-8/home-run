package io.ssafy.p.j14c103.homerun.api.service.game.loan.response;

import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.LoanApplication;

/**
 * 대출 심사 신청 응답.
 */
public record LoanApplyResponse(
        Integer applicationId,
        String status,
        RequestInfo requestInfo,
        Result result
) {

    public record RequestInfo(String applicationDate) {
    }

    public record Result(Integer maxLoanAmount, String rejectionReason) {
    }

    public static LoanApplyResponse from(final LoanApplication application) {
        return new LoanApplyResponse(
                application.getLoanApplicationId(),
                application.getApplicationStatus().name(),
                new RequestInfo(
                        application.getAppliedAt() != null
                                ? application.getAppliedAt().toLocalDate().toString()
                                : null
                ),
                new Result(
                        application.getApprovedLimitAmount(),
                        application.getRejectionReason()
                )
        );
    }
}
