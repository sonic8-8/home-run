package io.ssafy.p.j14c103.homerun.domain.gamesession.loan;

/**
 * 대출 심사 상태.
 * PENDING → APPROVED / REJECTED → CONFIRMED
 */
public enum LoanApplicationStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CONFIRMED
}
