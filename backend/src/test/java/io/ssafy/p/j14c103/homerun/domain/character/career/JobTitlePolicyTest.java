package io.ssafy.p.j14c103.homerun.domain.character.career;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JobTitlePolicyTest {

    private final JobTitlePolicy jobTitlePolicy = new JobTitlePolicy();

    @DisplayName("기업형 직업은 근속 구간에 따라 직함이 계산된다.")
    @Test
    void calculateCorporateJobTitle() {
        // when & then
        assertThat(jobTitlePolicy.calculate(JobType.LARGE_BIZ, 0)).isEqualTo("수습/인턴");
        assertThat(jobTitlePolicy.calculate(JobType.MID_BIZ, 6)).isEqualTo("수습/인턴");
        assertThat(jobTitlePolicy.calculate(JobType.SMALL_BIZ, 7)).isEqualTo("사원");
        assertThat(jobTitlePolicy.calculate(JobType.STARTUP, 23)).isEqualTo("사원");
        assertThat(jobTitlePolicy.calculate(JobType.LARGE_BIZ, 24)).isEqualTo("대리");
        assertThat(jobTitlePolicy.calculate(JobType.MID_BIZ, 59)).isEqualTo("대리");
        assertThat(jobTitlePolicy.calculate(JobType.SMALL_BIZ, 60)).isEqualTo("과장");
        assertThat(jobTitlePolicy.calculate(JobType.STARTUP, 107)).isEqualTo("과장");
        assertThat(jobTitlePolicy.calculate(JobType.LARGE_BIZ, 108)).isEqualTo("차장");
        assertThat(jobTitlePolicy.calculate(JobType.MID_BIZ, 167)).isEqualTo("차장");
        assertThat(jobTitlePolicy.calculate(JobType.SMALL_BIZ, 168)).isEqualTo("부장");
    }

    @DisplayName("프리랜서는 근속 구간에 따라 별도 직함이 계산된다.")
    @Test
    void calculateFreelancerJobTitle() {
        // when & then
        assertThat(jobTitlePolicy.calculate(JobType.FREELANCER, 0)).isEqualTo("신입");
        assertThat(jobTitlePolicy.calculate(JobType.FREELANCER, 6)).isEqualTo("신입");
        assertThat(jobTitlePolicy.calculate(JobType.FREELANCER, 7)).isEqualTo("주니어");
        assertThat(jobTitlePolicy.calculate(JobType.FREELANCER, 23)).isEqualTo("주니어");
        assertThat(jobTitlePolicy.calculate(JobType.FREELANCER, 24)).isEqualTo("미드레벨");
        assertThat(jobTitlePolicy.calculate(JobType.FREELANCER, 59)).isEqualTo("미드레벨");
        assertThat(jobTitlePolicy.calculate(JobType.FREELANCER, 60)).isEqualTo("시니어");
        assertThat(jobTitlePolicy.calculate(JobType.FREELANCER, 107)).isEqualTo("시니어");
        assertThat(jobTitlePolicy.calculate(JobType.FREELANCER, 108)).isEqualTo("전문가");
        assertThat(jobTitlePolicy.calculate(JobType.FREELANCER, 167)).isEqualTo("전문가");
        assertThat(jobTitlePolicy.calculate(JobType.FREELANCER, 168)).isEqualTo("마스터");
    }

    @DisplayName("직업 유형이 없으면 예외가 발생한다.")
    @Test
    void calculateWithNullJobType() {
        // when & then
        assertThatThrownBy(() -> jobTitlePolicy.calculate(null, 0))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.CHARACTER_REQUEST_INVALID);
    }

    @DisplayName("근속 턴 수가 음수면 예외가 발생한다.")
    @Test
    void calculateWithNegativeTenureTurns() {
        // when & then
        assertThatThrownBy(() -> jobTitlePolicy.calculate(JobType.LARGE_BIZ, -1))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.CHARACTER_REQUEST_INVALID);
    }
}
