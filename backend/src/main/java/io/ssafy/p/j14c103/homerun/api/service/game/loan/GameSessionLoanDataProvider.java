package io.ssafy.p.j14c103.homerun.api.service.game.loan;

/**
 * 게임 세션에서 대출 심사에 필요한 데이터를 제공하는 인터페이스.
 * GameSession 엔티티 완성 시 실제 구현체로 교체.
 */
public interface GameSessionLoanDataProvider {

    /**
     * 세션 데이터 조회.
     */
    LoanSessionData getLoanSessionData(Integer sessionId);

    /**
     * 대출 심사에 필요한 세션 데이터.
     */
    record LoanSessionData(
            int annualSalary,
            String jobType,
            int cssGrade,
            String regionCode,
            Integer propertyPrice
    ) {}
}
