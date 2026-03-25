package io.ssafy.p.j14c103.homerun.domain.world.cycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CycleStateTest {

    @DisplayName("CycleState는 phase와 type 조합이 일치할 때만 생성할 수 있다")
    @Test
    void of() {
        final CycleState cycleState = CycleState.of(
            CyclePhase.CRISIS,
            CycleType.CYCLE_PANDEMIC,
            8
        );

        assertThat(cycleState.getPhase()).isEqualTo(CyclePhase.CRISIS);
        assertThat(cycleState.getType()).isEqualTo(CycleType.CYCLE_PANDEMIC);
        assertThat(cycleState.getRemainingTurns()).isEqualTo(8);
    }

    @DisplayName("CycleState는 phase와 type 조합이 어긋나면 예외가 발생한다")
    @Test
    void ofWithMismatchedPhaseAndType() {
        assertThatThrownBy(() -> CycleState.of(
            CyclePhase.BOOM,
            CycleType.CYCLE_OIL_SHOCK,
            12
        ))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.WORLD_CYCLE_INPUT_INVALID);
    }

    @DisplayName("CycleState는 남은 턴이 1 미만이면 예외가 발생한다")
    @Test
    void ofWithInvalidRemainingTurns() {
        assertThatThrownBy(() -> CycleState.of(
            CyclePhase.RECOVERY,
            CycleType.CYCLE_RATE_HIKE,
            0
        ))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.WORLD_CYCLE_INPUT_INVALID);
    }
}
