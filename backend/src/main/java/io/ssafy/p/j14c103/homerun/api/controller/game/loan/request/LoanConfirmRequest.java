package io.ssafy.p.j14c103.homerun.api.controller.game.loan.request;

/**
 * 대출 최종 확정 요청.
 */
public record LoanConfirmRequest(
        Integer applicationId,
        int requestedAmount,
        boolean agreed
) {
}
