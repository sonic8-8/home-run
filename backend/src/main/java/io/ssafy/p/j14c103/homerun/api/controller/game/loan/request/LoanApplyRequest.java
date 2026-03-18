package io.ssafy.p.j14c103.homerun.api.controller.game.loan.request;

/**
 * 대출 심사 신청 요청.
 */
public record LoanApplyRequest(
        String productId,
        Integer propertyId
) {
}
