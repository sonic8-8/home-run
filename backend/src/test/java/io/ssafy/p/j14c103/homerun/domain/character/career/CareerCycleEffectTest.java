package io.ssafy.p.j14c103.homerun.domain.character.career;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CareerCycleEffectTest {

    @DisplayName("사이클 입력이 없거나 RECOVERY면 MVP1 기본 효과를 유지한다.")
    @Test
    void fromWithRecoveryOrNoInput() {
        // when
        final CareerCycleEffect withoutCycle = CareerCycleEffect.from(null, JobType.SMALL_BIZ);
        final CareerCycleEffect recovery = CareerCycleEffect.from(
            CyclePhase.RECOVERY,
            JobType.SMALL_BIZ
        );

        // then
        assertThat(withoutCycle.applySalaryMultiplier(5)).isEqualTo(5);
        assertThat(withoutCycle.applyRehirePenalty(2)).isEqualTo(2);
        assertThat(recovery.applySalaryMultiplier(5)).isEqualTo(5);
        assertThat(recovery.applyRehirePenalty(2)).isEqualTo(2);
    }

    @DisplayName("BOOM이면 직업별 연봉 협상 배율과 재취업 페널티를 적용한다.")
    @Test
    void fromWithBoom() {
        // when
        final CareerCycleEffect smallBizEffect = CareerCycleEffect.from(
            CyclePhase.BOOM,
            JobType.SMALL_BIZ
        );
        final CareerCycleEffect freelancerEffect = CareerCycleEffect.from(
            CyclePhase.BOOM,
            JobType.FREELANCER
        );

        // then
        assertThat(smallBizEffect.applySalaryMultiplier(10)).isEqualTo(13);
        assertThat(smallBizEffect.applyRehirePenalty(2)).isEqualTo(2);
        assertThat(freelancerEffect.applySalaryMultiplier(10)).isEqualTo(15);
        assertThat(freelancerEffect.applyRehirePenalty(1)).isEqualTo(1);
    }

    @DisplayName("CRISIS면 직업별 연봉 협상 배율과 재취업 대기 페널티를 적용한다.")
    @Test
    void fromWithCrisis() {
        // when
        final CareerCycleEffect startupEffect = CareerCycleEffect.from(
            CyclePhase.CRISIS,
            JobType.STARTUP
        );
        final CareerCycleEffect largeBizEffect = CareerCycleEffect.from(
            CyclePhase.CRISIS,
            JobType.LARGE_BIZ
        );

        // then
        assertThat(startupEffect.applySalaryMultiplier(10)).isEqualTo(6);
        assertThat(startupEffect.applyRehirePenalty(2)).isEqualTo(4);
        assertThat(largeBizEffect.applySalaryMultiplier(10)).isEqualTo(7);
        assertThat(largeBizEffect.applyRehirePenalty(1)).isEqualTo(2);
    }
}
