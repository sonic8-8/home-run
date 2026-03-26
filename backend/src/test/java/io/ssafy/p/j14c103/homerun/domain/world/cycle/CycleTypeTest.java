package io.ssafy.p.j14c103.homerun.domain.world.cycle;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CycleTypeTest {

    @DisplayName("CycleType enum 값은 문서의 10개 세부 사이클과 일치한다")
    @Test
    void values() {
        assertThat(CycleType.values()).containsExactly(
            CycleType.CYCLE_BOOM,
            CycleType.CYCLE_FINANCIAL_CRISIS,
            CycleType.CYCLE_CURRENCY_CRISIS,
            CycleType.CYCLE_TECH_BUBBLE,
            CycleType.CYCLE_OIL_SHOCK,
            CycleType.CYCLE_STAGFLATION,
            CycleType.CYCLE_PANDEMIC,
            CycleType.CYCLE_GREAT_DEPRESSION,
            CycleType.CYCLE_RATE_HIKE,
            CycleType.CYCLE_GEOPOLITICAL
        );
    }

    @DisplayName("세부 사이클은 상위 phase와 지속 턴 범위를 함께 제공한다")
    @Test
    void metadata() {
        assertThat(CycleType.CYCLE_BOOM.getPhase()).isEqualTo(CyclePhase.BOOM);
        assertThat(CycleType.CYCLE_BOOM.getMinDurationTurns()).isEqualTo(24);
        assertThat(CycleType.CYCLE_BOOM.getMaxDurationTurns()).isEqualTo(48);

        assertThat(CycleType.CYCLE_FINANCIAL_CRISIS.getPhase()).isEqualTo(CyclePhase.CRISIS);
        assertThat(CycleType.CYCLE_FINANCIAL_CRISIS.getMinDurationTurns()).isEqualTo(30);
        assertThat(CycleType.CYCLE_FINANCIAL_CRISIS.getMaxDurationTurns()).isEqualTo(30);

        assertThat(CycleType.CYCLE_GEOPOLITICAL.getPhase()).isEqualTo(CyclePhase.RECOVERY);
        assertThat(CycleType.CYCLE_GEOPOLITICAL.getMinDurationTurns()).isEqualTo(6);
        assertThat(CycleType.CYCLE_GEOPOLITICAL.getMaxDurationTurns()).isEqualTo(18);
    }
}
