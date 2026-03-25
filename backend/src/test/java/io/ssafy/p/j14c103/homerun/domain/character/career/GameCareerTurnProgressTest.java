package io.ssafy.p.j14c103.homerun.domain.character.career;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameCareerTurnProgressTest {

    private final JobTitlePolicy jobTitlePolicy = new JobTitlePolicy();

    @DisplayName("재직 중이면 턴 진행 시 근속과 직함이 갱신된다.")
    @Test
    void advanceTurn() {
        // given
        final GameCareer gameCareer = createCareer(
            23,
            EmploymentStatus.EMPLOYED,
            null,
            JobType.STARTUP,
            "사원"
        );

        // when
        gameCareer.advanceTurn(jobTitlePolicy, 24);

        // then
        assertThat(gameCareer.getTenureTurns()).isEqualTo(24);
        assertThat(gameCareer.getJobTitle()).isEqualTo("대리");
        assertThat(gameCareer.getEmploymentStatus()).isEqualTo(EmploymentStatus.EMPLOYED);
    }

    @DisplayName("수습 종료 턴에 도달하면 재직 상태로 전환된다.")
    @Test
    void advanceTurnReleaseProbation() {
        // given
        final GameCareer gameCareer = createCareer(
            5,
            EmploymentStatus.PROBATION,
            6,
            JobType.MID_BIZ,
            "수습/인턴"
        );

        // when
        gameCareer.advanceTurn(jobTitlePolicy, 6);

        // then
        assertThat(gameCareer.getTenureTurns()).isEqualTo(6);
        assertThat(gameCareer.getEmploymentStatus()).isEqualTo(EmploymentStatus.EMPLOYED);
        assertThat(gameCareer.getProbationEndTurn()).isNull();
        assertThat(gameCareer.getJobTitle()).isEqualTo("수습/인턴");
    }

    @DisplayName("실직 상태면 턴을 진행해도 근속과 직함이 유지된다.")
    @Test
    void advanceTurnForUnemployedCareer() {
        // given
        final GameCareer gameCareer = createCareer(
            10,
            EmploymentStatus.UNEMPLOYED,
            null,
            JobType.SMALL_BIZ,
            "사원"
        );

        // when
        gameCareer.advanceTurn(jobTitlePolicy, 20);

        // then
        assertThat(gameCareer.getTenureTurns()).isEqualTo(10);
        assertThat(gameCareer.getJobTitle()).isEqualTo("사원");
        assertThat(gameCareer.getEmploymentStatus()).isEqualTo(EmploymentStatus.UNEMPLOYED);
    }

    private GameCareer createCareer(
        final int tenureTurns,
        final EmploymentStatus employmentStatus,
        final Integer probationEndTurn,
        final JobType jobType,
        final String jobTitle
    ) {
        return GameCareer.builder()
            .gameId(1)
            .jobType(jobType)
            .jobTitle(jobTitle)
            .salary(36_000_000)
            .tenureTurns(tenureTurns)
            .recentStudyCount(0)
            .recentNetworkingCount(0)
            .negotiationPreparationScore(0)
            .lastNegotiatedTurn(0)
            .employmentStatus(employmentStatus)
            .probationEndTurn(probationEndTurn)
            .rehireAvailableTurn(null)
            .remainingUnemploymentBenefitTurns(0)
            .salaryBeforeResignation(null)
            .build();
    }
}
