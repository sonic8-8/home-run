package io.ssafy.p.j14c103.homerun.api.service.home.credit;

/**
 * 신용점수 산출 인터페이스.
 * 현재: FICO 기반 자체 CSS (FicoCreditScoringService)
 * 향후: 마이데이터 연동 시 구현체 교체로 실제 신용점수 전환 가능
 */
public interface CreditScoreProvider {

    CreditScore calculate(Long userId);
}
