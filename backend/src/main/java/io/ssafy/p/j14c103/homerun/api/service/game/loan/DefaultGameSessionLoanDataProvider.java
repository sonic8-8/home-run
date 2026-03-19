package io.ssafy.p.j14c103.homerun.api.service.game.loan;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * GameSession 엔티티 완성 전 임시 구현체.
 * TODO: GameSession 완성 시 실제 세션 데이터 조회로 교체
 */
@Slf4j
@Component
public class DefaultGameSessionLoanDataProvider implements GameSessionLoanDataProvider {

    @Override
    public LoanSessionData getLoanSessionData(final Integer sessionId) {
        log.warn("임시 세션 데이터 사용 중 (sessionId={}). GameSession 완성 시 교체 필요.", sessionId);
        return new LoanSessionData(
                36_000_000,     // annualSalary
                "LARGE_BIZ",    // jobType
                2,              // cssGrade
                "SEOUL",        // regionCode
                375_000_000     // propertyPrice
        );
    }
}
