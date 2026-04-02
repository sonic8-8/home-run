package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.api.service.game.port.EndingRuleEvaluator;
import io.ssafy.p.j14c103.homerun.domain.gamesession.SessionStatus;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MvpEndingRuleEvaluatorServiceTest {

    private final MvpEndingRuleEvaluatorService mvpEndingRuleEvaluator =
        new MvpEndingRuleEvaluatorService();

    @DisplayName("MVP 엔딩 규칙은 현금, 주식, 부동산에서 대출을 차감해 순자산을 계산한다.")
    @Test
    void calculateNetWorth() {
        // given
        final EndingRuleEvaluator.EndingRuleContext context = EndingRuleEvaluator.EndingRuleContext.of(
            12,
            Money.of(2_000_000L),
            Money.of(300_000L),
            Money.of(500_000L),
            Money.of(500_000L),
            false,
            false
        );

        // when
        final EndingRuleEvaluator.EndingEvaluation evaluation = mvpEndingRuleEvaluator.evaluate(context);

        // then
        assertThat(evaluation.getNetWorth()).isEqualTo(Money.of(2_300_000L));
        assertThat(evaluation.getSessionStatus()).isEqualTo(SessionStatus.IN_PROGRESS);
    }

    @DisplayName("MVP 엔딩 규칙은 목표 집을 확보하면 클리어를 우선한다.")
    @Test
    void clearWhenTargetPropertyOwned() {
        // given
        final EndingRuleEvaluator.EndingRuleContext context = EndingRuleEvaluator.EndingRuleContext.of(
            120,
            Money.of(100_000L),
            Money.of(0L),
            Money.of(0L),
            Money.of(300_000L),
            true,
            false
        );

        // when
        final EndingRuleEvaluator.EndingEvaluation evaluation = mvpEndingRuleEvaluator.evaluate(context);

        // then
        assertThat(evaluation.getSessionStatus()).isEqualTo(SessionStatus.CLEAR);
        assertThat(evaluation.getNetWorth()).isEqualTo(Money.of(-200_000L));
    }

    @DisplayName("MVP 엔딩 규칙은 순자산이 0 이하이면 파산 처리한다.")
    @Test
    void bankruptWhenNetWorthIsZeroOrNegative() {
        // given
        final EndingRuleEvaluator.EndingRuleContext context = EndingRuleEvaluator.EndingRuleContext.of(
            45,
            Money.of(200_000L),
            Money.of(100_000L),
            Money.of(0L),
            Money.of(300_000L),
            false,
            false
        );

        // when
        final EndingRuleEvaluator.EndingEvaluation evaluation = mvpEndingRuleEvaluator.evaluate(context);

        // then
        assertThat(evaluation.getSessionStatus()).isEqualTo(SessionStatus.BANKRUPT);
        assertThat(evaluation.getNetWorth()).isEqualTo(Money.zero());
    }

    @DisplayName("MVP 엔딩 규칙은 360턴에 도달하면 타임아웃 처리한다.")
    @Test
    void timeoutWhenReachedTurnLimit() {
        // given
        final EndingRuleEvaluator.EndingRuleContext context = EndingRuleEvaluator.EndingRuleContext.of(
            360,
            Money.of(2_000_000L),
            Money.of(500_000L),
            Money.of(700_000L),
            Money.of(100_000L),
            false,
            false
        );

        // when
        final EndingRuleEvaluator.EndingEvaluation evaluation = mvpEndingRuleEvaluator.evaluate(context);

        // then
        assertThat(evaluation.getSessionStatus()).isEqualTo(SessionStatus.TIMEOUT);
        assertThat(evaluation.getNetWorth()).isEqualTo(Money.of(3_100_000L));
    }

    @DisplayName("주거 상실 압류 신호가 있으면 파산이나 타임아웃보다 압류 엔딩을 우선한다.")
    @Test
    void foreclosureWhenTriggered() {
        // given
        final EndingRuleEvaluator.EndingRuleContext context = EndingRuleEvaluator.EndingRuleContext.of(
            360,
            Money.of(200_000L),
            Money.of(100_000L),
            Money.of(0L),
            Money.of(300_000L),
            false,
            true
        );

        // when
        final EndingRuleEvaluator.EndingEvaluation evaluation = mvpEndingRuleEvaluator.evaluate(context);

        // then
        assertThat(evaluation.getSessionStatus()).isEqualTo(SessionStatus.FORECLOSURE);
        assertThat(evaluation.getNetWorth()).isEqualTo(Money.zero());
    }
}
