package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.ssafy.p.j14c103.homerun.api.service.game.port.SettlementStepExecutor;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.request.SettlementOrchestratorRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.response.SettlementOrchestratorResult;
import io.ssafy.p.j14c103.homerun.domain.gamesession.SessionStatus;
import io.ssafy.p.j14c103.homerun.domain.gamesession.settlement.SettlementPhaseType;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class SettlementOrchestratorServiceTest extends IntegrationTestSupport {

    private static final String SETTLEMENT_PHASE_DURATION = "homerun.settlement.phase.duration";

    @Autowired
    private SettlementOrchestratorService settlementOrchestratorService;

    @Autowired
    private MeterRegistry meterRegistry;

    @MockitoBean
    private SettlementStepExecutor settlementStepExecutor;

    @DisplayName("정산 오케스트레이터는 3페이즈 13단계를 고정 순서로 실행하고 MVP 엔딩 판정을 반영한다.")
    @Test
    void orchestrate() {
        // given
        final long marketTimerCountBefore = settlementPhaseTimerCount(SettlementPhaseType.MARKET_UPDATE);
        final long incomeTimerCountBefore = settlementPhaseTimerCount(SettlementPhaseType.INCOME_EXPENSE);
        final long statusTimerCountBefore = settlementPhaseTimerCount(SettlementPhaseType.STATUS_UPDATE);
        final SettlementOrchestratorRequest request = SettlementOrchestratorRequest.of(
            101L,
            1001L,
            12,
            Money.of(2_000_000L),
            Money.of(300_000L),
            Money.zero(),
            Money.of(100_000L),
            Money.zero(),
            Map.of(),
            "경기 회복기",
            false,
            false,
            false
        );
        stubStep(SettlementStepType.MARKET_CYCLE_UPDATE, "경기 사이클을 갱신한다", 0L, 50_000L, 0L, Map.of());
        stubStep(SettlementStepType.MARKET_STOCK_PRICE_REFRESH, "주가를 갱신한다", 0L, 100_000L, 0L, Map.of());
        stubStep(SettlementStepType.MARKET_WORLD_SIGNAL_REFRESH, "월드 신호를 정리한다", 0L, 0L, 0L, Map.of());
        stubStep(SettlementStepType.INCOME_SALARY_SETTLEMENT, "월급을 반영한다", 300_000L, 0L, 0L, Map.of());
        stubStep(SettlementStepType.INCOME_SIDE_JOB_SETTLEMENT, "부업 수입을 반영한다", 120_000L, 0L, 0L, Map.of("fatigue", 2));
        stubStep(SettlementStepType.INCOME_FIXED_EXPENSE_SETTLEMENT, "고정 지출을 차감한다", -150_000L, 0L, 0L, Map.of());
        stubStep(SettlementStepType.INCOME_HOUSING_COST_SETTLEMENT, "주거 비용을 차감한다", -80_000L, 0L, 0L, Map.of());
        stubStep(SettlementStepType.INCOME_CARD_BILL_SETTLEMENT, "카드 청구액을 차감한다", -70_000L, 0L, 0L, Map.of());
        stubStep(SettlementStepType.INCOME_STOCK_ORDER_SETTLEMENT, "주문 체결 결과를 반영한다", 50_000L, 0L, 0L, Map.of());
        stubStep(SettlementStepType.INCOME_LOAN_INTEREST_SETTLEMENT, "대출 이자를 차감한다", -20_000L, 0L, 30_000L, Map.of());
        stubStep(SettlementStepType.STATUS_CHARACTER_UPDATE, "캐릭터 상태를 갱신한다", 0L, 0L, 0L, Map.of("health", 3, "stress", -4));
        stubStep(SettlementStepType.STATUS_PENDING_EVENT_PREPARE, "이벤트 대기열을 준비한다", 0L, 0L, 0L, Map.of());
        stubStep(SettlementStepType.STATUS_ENDING_CHECKPOINT, "엔딩 체크포인트를 기록한다", 0L, 0L, 0L, Map.of());

        // when
        final SettlementOrchestratorResult result = settlementOrchestratorService.orchestrate(request);

        // then
        assertThat(result.getStepResults()).hasSize(13);
        assertThat(result.getFinalCash()).isEqualTo(Money.of(2_150_000L));
        assertThat(result.getFinalStockValue()).isEqualTo(Money.of(450_000L));
        assertThat(result.getFinalLoanBalance()).isEqualTo(Money.of(130_000L));
        assertThat(result.getTotalAssets()).isEqualTo(Money.of(2_600_000L));
        assertThat(result.getNetWorth()).isEqualTo(Money.of(2_470_000L));
        assertThat(result.getEndingStatus()).isEqualTo(SessionStatus.IN_PROGRESS);
        assertThat(result.getAggregatedStatChanges()).containsEntry("health", 3)
            .containsEntry("stress", -4)
            .containsEntry("fatigue", 2);
        assertThat(settlementPhaseTimerCount(SettlementPhaseType.MARKET_UPDATE))
            .isEqualTo(marketTimerCountBefore + 1);
        assertThat(settlementPhaseTimerCount(SettlementPhaseType.INCOME_EXPENSE))
            .isEqualTo(incomeTimerCountBefore + 1);
        assertThat(settlementPhaseTimerCount(SettlementPhaseType.STATUS_UPDATE))
            .isEqualTo(statusTimerCountBefore + 1);

        final InOrder inOrder = inOrder(settlementStepExecutor);
        for (SettlementStepType stepType : SettlementStepType.orderedValues()) {
            inOrder.verify(settlementStepExecutor).execute(
                argThat((SettlementStepExecutor.SettlementStepExecutionContext context) ->
                    context != null && context.getStepType() == stepType
                )
            );
        }
    }

    private void stubStep(
        final SettlementStepType stepType,
        final String description,
        final long cashDelta,
        final long stockValueDelta,
        final long loanBalanceDelta,
        final Map<String, Integer> statChanges
    ) {
        given(settlementStepExecutor.execute(
            argThat((SettlementStepExecutor.SettlementStepExecutionContext context) ->
                context != null && context.getStepType() == stepType
            )
        ))
            .willReturn(SettlementStepExecutor.StepExecutionResult.of(
                description,
                Money.of(cashDelta),
                Money.of(stockValueDelta),
                Money.of(loanBalanceDelta),
                statChanges,
                false,
                false
            ));
    }

    private long settlementPhaseTimerCount(final SettlementPhaseType phaseType) {
        final Timer timer = meterRegistry.find(SETTLEMENT_PHASE_DURATION)
            .tag("phase", phaseType.name())
            .timer();
        if (timer == null) {
            return 0L;
        }
        return timer.count();
    }
}
