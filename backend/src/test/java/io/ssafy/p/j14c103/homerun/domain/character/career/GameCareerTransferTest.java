package io.ssafy.p.j14c103.homerun.domain.character.career;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameCareerTransferTest {

    private final JobTitlePolicy jobTitlePolicy = new JobTitlePolicy();

    @DisplayName("기업형 이직을 수락하면 직업, 직함, 연봉, 근속, 수습 종료 턴이 갱신된다.")
    @Test
    void acceptCorporateTransfer() {
        // given
        final GameCareer gameCareer = createCareer(JobType.STARTUP, 31_000_000);
        final JobTransferPolicy.JobOffer offer = JobTransferPolicy.JobOffer.of(
            "OFFER-001",
            JobType.LARGE_BIZ,
            "OO 대기업",
            31_000_000,
            60_000_000,
            2
        );

        // when
        gameCareer.acceptTransfer(offer, jobTitlePolicy, 15);

        // then
        assertThat(gameCareer.getJobType()).isEqualTo(JobType.LARGE_BIZ);
        assertThat(gameCareer.getJobTitle()).isEqualTo("수습/인턴");
        assertThat(gameCareer.getSalary()).isEqualTo(60_000_000);
        assertThat(gameCareer.getTenureTurns()).isZero();
        assertThat(gameCareer.getEmploymentStatus()).isEqualTo(EmploymentStatus.PROBATION);
        assertThat(gameCareer.getProbationEndTurn()).isEqualTo(17);
        assertThat(gameCareer.getRehireAvailableTurn()).isNull();
        assertThat(gameCareer.getRemainingUnemploymentBenefitTurns()).isZero();
        assertThat(gameCareer.getSalaryBeforeResignation()).isNull();
    }

    @DisplayName("프리랜서 이직을 수락하면 수습 없이 재직 상태로 갱신된다.")
    @Test
    void acceptFreelancerTransfer() {
        // given
        final GameCareer gameCareer = createCareer(JobType.MID_BIZ, 32_000_000);
        final JobTransferPolicy.JobOffer offer = JobTransferPolicy.JobOffer.of(
            "OFFER-002",
            JobType.FREELANCER,
            "OO 프리랜서 프로젝트",
            32_000_000,
            40_000_000,
            null
        );

        // when
        gameCareer.acceptTransfer(offer, jobTitlePolicy, 15);

        // then
        assertThat(gameCareer.getJobType()).isEqualTo(JobType.FREELANCER);
        assertThat(gameCareer.getJobTitle()).isEqualTo("신입");
        assertThat(gameCareer.getSalary()).isEqualTo(40_000_000);
        assertThat(gameCareer.getTenureTurns()).isZero();
        assertThat(gameCareer.getEmploymentStatus()).isEqualTo(EmploymentStatus.EMPLOYED);
        assertThat(gameCareer.getProbationEndTurn()).isNull();
        assertThat(gameCareer.getRehireAvailableTurn()).isNull();
        assertThat(gameCareer.getRemainingUnemploymentBenefitTurns()).isZero();
        assertThat(gameCareer.getSalaryBeforeResignation()).isNull();
    }

    private GameCareer createCareer(final JobType jobType, final int salary) {
        return GameCareer.builder()
            .gameId(1)
            .jobType(jobType)
            .jobTitle("사원")
            .salary(salary)
            .tenureTurns(14)
            .recentStudyCount(1)
            .recentNetworkingCount(1)
            .negotiationPreparationScore(0)
            .lastNegotiatedTurn(0)
            .employmentStatus(EmploymentStatus.EMPLOYED)
            .probationEndTurn(null)
            .rehireAvailableTurn(20)
            .remainingUnemploymentBenefitTurns(1)
            .salaryBeforeResignation(29_000_000)
            .build();
    }
}
