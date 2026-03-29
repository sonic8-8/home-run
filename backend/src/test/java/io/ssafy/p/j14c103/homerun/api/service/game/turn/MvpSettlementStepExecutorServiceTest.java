package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.api.service.game.port.SettlementStepExecutor;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MvpSettlementStepExecutorServiceTest {

    private final MvpSettlementStepExecutorService mvpSettlementStepExecutor =
        new MvpSettlementStepExecutorService();

    @DisplayName("MVP 정산 단계 실행기는 각 단계를 no-op 결과로 반환한다.")
    @Test
    void execute() {
        for (SettlementStepType stepType : SettlementStepType.orderedValues()) {
            final SettlementStepExecutor.StepExecutionResult result = mvpSettlementStepExecutor.execute(
                SettlementStepExecutor.SettlementStepExecutionContext.of(
                    101L,
                    12,
                    stepType,
                    Money.of(1_000_000L),
                    Money.of(200_000L),
                    Money.of(300_000L),
                    Map.of("stress", 1),
                    true
                )
            );

            assertThat(result.getDescription()).isNotBlank();
            assertThat(result.getCashDelta()).isEqualTo(Money.zero());
            assertThat(result.getStockValueDelta()).isEqualTo(Money.zero());
            assertThat(result.getLoanBalanceDelta()).isEqualTo(Money.zero());
            assertThat(result.getStatChanges()).isEmpty();
            assertThat(result.isEventTriggered()).isFalse();
            assertThat(result.isTargetPropertyOwned()).isTrue();
        }
    }
}
