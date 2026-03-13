package io.ssafy.p.j14c103.homerun.domain.cycle;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CyclePhaseTest {
    @DisplayName("CyclePhase enum 값은 최신 spec과 일치한다")
    @Test
    void values() {
        assertThat(CyclePhase.values()).containsExactly(
                CyclePhase.BOOM,
                CyclePhase.CRISIS,
                CyclePhase.RECOVERY
        );
    }
}
