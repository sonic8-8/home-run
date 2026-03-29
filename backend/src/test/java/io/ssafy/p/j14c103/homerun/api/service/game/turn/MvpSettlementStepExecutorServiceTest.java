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

    @DisplayName("MVP 정산 단계 실행기는 preview와 world 결과를 13단계 skeleton으로 매핑한다.")
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
                    Money.of(150_000L),
                    Map.of("stress", 1, "knowledge", 2),
                    "경기 회복기",
                    true,
                    Map.of("stress", 1),
                    true
                )
            );

            assertThat(result.getDescription()).isNotBlank();
            if (stepType == SettlementStepType.INCOME_SALARY_SETTLEMENT) {
                assertThat(result.getCashDelta()).isEqualTo(Money.of(150_000L));
            } else {
                assertThat(result.getCashDelta()).isEqualTo(Money.zero());
            }
            assertThat(result.getStockValueDelta()).isEqualTo(Money.zero());
            assertThat(result.getLoanBalanceDelta()).isEqualTo(Money.zero());
            if (stepType == SettlementStepType.STATUS_CHARACTER_UPDATE) {
                assertThat(result.getStatChanges()).containsEntry("stress", 1).containsEntry("knowledge", 2);
            } else {
                assertThat(result.getStatChanges()).isEmpty();
            }
            if (stepType == SettlementStepType.STATUS_PENDING_EVENT_PREPARE) {
                assertThat(result.isEventTriggered()).isTrue();
            } else {
                assertThat(result.isEventTriggered()).isFalse();
            }
            assertThat(result.isTargetPropertyOwned()).isTrue();
        }
    }
}
