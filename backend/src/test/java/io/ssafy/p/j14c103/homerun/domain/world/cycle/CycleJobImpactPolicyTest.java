package io.ssafy.p.j14c103.homerun.domain.world.cycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CycleJobImpactPolicyTest {

    private final CycleJobImpactPolicy cycleJobImpactPolicy = new CycleJobImpactPolicy();

    @DisplayName("CYCLE_BOOM의 중소기업 영향은 기획 문서 표와 일치한다")
    @Test
    void resolveBoomSmallBizImpact() {
        // when
        final CycleJobImpact impact = cycleJobImpactPolicy.resolve(
            CycleType.CYCLE_BOOM,
            JobType.SMALL_BIZ
        );

        // then
        assertThat(impact.getSalaryMultiplier()).isEqualByComparingTo("1.3");
        assertThat(impact.getLayoffMultiplier()).isEqualByComparingTo("0.7");
        assertThat(impact.getRehirePenaltyTurns()).isEqualTo(0);
    }

    @DisplayName("CYCLE_CURRENCY_CRISIS의 중견기업 영향은 기획 문서 표와 일치한다")
    @Test
    void resolveCurrencyCrisisMidBizImpact() {
        // when
        final CycleJobImpact impact = cycleJobImpactPolicy.resolve(
            CycleType.CYCLE_CURRENCY_CRISIS,
            JobType.MID_BIZ
        );

        // then
        assertThat(impact.getSalaryMultiplier()).isEqualByComparingTo("0.5");
        assertThat(impact.getLayoffMultiplier()).isEqualByComparingTo("2.5");
        assertThat(impact.getRehirePenaltyTurns()).isEqualTo(2);
    }

    @DisplayName("CYCLE_GREAT_DEPRESSION의 프리랜서 영향은 layoff 배율 없이 반환한다")
    @Test
    void resolveGreatDepressionFreelancerImpact() {
        // when
        final CycleJobImpact impact = cycleJobImpactPolicy.resolve(
            CycleType.CYCLE_GREAT_DEPRESSION,
            JobType.FREELANCER
        );

        // then
        assertThat(impact.getSalaryMultiplier()).isEqualByComparingTo("0.2");
        assertThat(impact.getLayoffMultiplier()).isNull();
        assertThat(impact.getRehirePenaltyTurns()).isEqualTo(4);
    }

    @DisplayName("CYCLE_RATE_HIKE의 대기업 영향은 기획 문서 표와 일치한다")
    @Test
    void resolveRateHikeLargeBizImpact() {
        // when
        final CycleJobImpact impact = cycleJobImpactPolicy.resolve(
            CycleType.CYCLE_RATE_HIKE,
            JobType.LARGE_BIZ
        );

        // then
        assertThat(impact.getSalaryMultiplier()).isEqualByComparingTo("0.85");
        assertThat(impact.getLayoffMultiplier()).isEqualByComparingTo("1.1");
        assertThat(impact.getRehirePenaltyTurns()).isEqualTo(0);
    }

    @DisplayName("STARTUP 영향은 현재 정책상 MID_BIZ와 동일하게 반환한다")
    @Test
    void resolveStartupAsMidBizTrack() {
        // when
        final CycleJobImpact startupImpact = cycleJobImpactPolicy.resolve(
            CycleType.CYCLE_STAGFLATION,
            JobType.STARTUP
        );
        final CycleJobImpact midBizImpact = cycleJobImpactPolicy.resolve(
            CycleType.CYCLE_STAGFLATION,
            JobType.MID_BIZ
        );

        // then
        assertThat(startupImpact.getSalaryMultiplier())
            .isEqualByComparingTo(midBizImpact.getSalaryMultiplier());
        assertThat(startupImpact.getLayoffMultiplier())
            .isEqualByComparingTo(midBizImpact.getLayoffMultiplier());
        assertThat(startupImpact.getRehirePenaltyTurns())
            .isEqualTo(midBizImpact.getRehirePenaltyTurns());
    }

    @DisplayName("지원하지 않는 직업 타입 요청이면 직업 타입 예외를 던진다")
    @Test
    void resolveWithUnsupportedJobType() {
        // when
        // then
        assertThatThrownBy(() -> cycleJobImpactPolicy.resolve(CycleType.CYCLE_BOOM, null))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.CHARACTER_JOB_TYPE_UNSUPPORTED);
    }
}
