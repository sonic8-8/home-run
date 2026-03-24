package io.ssafy.p.j14c103.homerun.domain.world.cycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CycleTransitionPolicyTest {

    private final CycleTransitionPolicy cycleTransitionPolicy = new CycleTransitionPolicy();

    @DisplayName("BOOM 상태는 문서의 가중치 구간에 따라 다음 상태를 결정한다")
    @Test
    void nextPhaseFromBoom() {
        // given
        CyclePhase currentPhase = CyclePhase.BOOM;

        // when
        CyclePhase boomAtLowerBound = cycleTransitionPolicy.nextPhase(currentPhase, 1);
        CyclePhase boomAtUpperBound = cycleTransitionPolicy.nextPhase(currentPhase, 55);
        CyclePhase crisisAtLowerBound = cycleTransitionPolicy.nextPhase(currentPhase, 56);
        CyclePhase crisisAtUpperBound = cycleTransitionPolicy.nextPhase(currentPhase, 80);
        CyclePhase recoveryAtLowerBound = cycleTransitionPolicy.nextPhase(currentPhase, 81);
        CyclePhase recoveryAtUpperBound = cycleTransitionPolicy.nextPhase(currentPhase, 100);

        // then
        assertThat(boomAtLowerBound).isEqualTo(CyclePhase.BOOM);
        assertThat(boomAtUpperBound).isEqualTo(CyclePhase.BOOM);
        assertThat(crisisAtLowerBound).isEqualTo(CyclePhase.CRISIS);
        assertThat(crisisAtUpperBound).isEqualTo(CyclePhase.CRISIS);
        assertThat(recoveryAtLowerBound).isEqualTo(CyclePhase.RECOVERY);
        assertThat(recoveryAtUpperBound).isEqualTo(CyclePhase.RECOVERY);
    }

    @DisplayName("CRISIS 상태는 문서의 가중치 구간에 따라 다음 상태를 결정한다")
    @Test
    void nextPhaseFromCrisis() {
        // given
        CyclePhase currentPhase = CyclePhase.CRISIS;

        // when
        CyclePhase boomAtLowerBound = cycleTransitionPolicy.nextPhase(currentPhase, 1);
        CyclePhase boomAtUpperBound = cycleTransitionPolicy.nextPhase(currentPhase, 15);
        CyclePhase crisisAtLowerBound = cycleTransitionPolicy.nextPhase(currentPhase, 16);
        CyclePhase crisisAtUpperBound = cycleTransitionPolicy.nextPhase(currentPhase, 40);
        CyclePhase recoveryAtLowerBound = cycleTransitionPolicy.nextPhase(currentPhase, 41);
        CyclePhase recoveryAtUpperBound = cycleTransitionPolicy.nextPhase(currentPhase, 100);

        // then
        assertThat(boomAtLowerBound).isEqualTo(CyclePhase.BOOM);
        assertThat(boomAtUpperBound).isEqualTo(CyclePhase.BOOM);
        assertThat(crisisAtLowerBound).isEqualTo(CyclePhase.CRISIS);
        assertThat(crisisAtUpperBound).isEqualTo(CyclePhase.CRISIS);
        assertThat(recoveryAtLowerBound).isEqualTo(CyclePhase.RECOVERY);
        assertThat(recoveryAtUpperBound).isEqualTo(CyclePhase.RECOVERY);
    }

    @DisplayName("RECOVERY 상태는 문서의 가중치 구간에 따라 다음 상태를 결정한다")
    @Test
    void nextPhaseFromRecovery() {
        // given
        CyclePhase currentPhase = CyclePhase.RECOVERY;

        // when
        CyclePhase boomAtLowerBound = cycleTransitionPolicy.nextPhase(currentPhase, 1);
        CyclePhase boomAtUpperBound = cycleTransitionPolicy.nextPhase(currentPhase, 65);
        CyclePhase crisisAtLowerBound = cycleTransitionPolicy.nextPhase(currentPhase, 66);
        CyclePhase crisisAtUpperBound = cycleTransitionPolicy.nextPhase(currentPhase, 75);
        CyclePhase recoveryAtLowerBound = cycleTransitionPolicy.nextPhase(currentPhase, 76);
        CyclePhase recoveryAtUpperBound = cycleTransitionPolicy.nextPhase(currentPhase, 100);

        // then
        assertThat(boomAtLowerBound).isEqualTo(CyclePhase.BOOM);
        assertThat(boomAtUpperBound).isEqualTo(CyclePhase.BOOM);
        assertThat(crisisAtLowerBound).isEqualTo(CyclePhase.CRISIS);
        assertThat(crisisAtUpperBound).isEqualTo(CyclePhase.CRISIS);
        assertThat(recoveryAtLowerBound).isEqualTo(CyclePhase.RECOVERY);
        assertThat(recoveryAtUpperBound).isEqualTo(CyclePhase.RECOVERY);
    }

    @DisplayName("랜덤값이 1 미만 또는 100 초과면 예외가 발생한다")
    @Test
    void nextPhaseWithInvalidRoll() {
        // given
        CyclePhase currentPhase = CyclePhase.BOOM;

        // when // then
        assertThatThrownBy(() -> cycleTransitionPolicy.nextPhase(currentPhase, 0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> cycleTransitionPolicy.nextPhase(currentPhase, 101))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("상태별 설명 문구를 반환한다")
    @Test
    void descriptionOf() {
        // when
        String boomDescription = cycleTransitionPolicy.descriptionOf(CyclePhase.BOOM);
        String crisisDescription = cycleTransitionPolicy.descriptionOf(CyclePhase.CRISIS);
        String recoveryDescription = cycleTransitionPolicy.descriptionOf(CyclePhase.RECOVERY);

        // then
        assertThat(boomDescription).isEqualTo("경기 호황기");
        assertThat(crisisDescription).isEqualTo("경기 위기");
        assertThat(recoveryDescription).isEqualTo("경기 회복기");
    }
}
