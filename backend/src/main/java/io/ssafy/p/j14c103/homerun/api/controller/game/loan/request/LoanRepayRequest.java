package io.ssafy.p.j14c103.homerun.api.controller.game.loan.request;

/**
 * 중도 상환 요청.
 */
public record LoanRepayRequest(
        Integer loanId,
        int amount
) {
}
