package io.ssafy.p.j14c103.homerun.domain.character.career;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JobTransferPolicyTest {

    private final JobTransferPolicy jobTransferPolicy = new JobTransferPolicy();

    @DisplayName("지식이 낮으면 현재 직업과 한 단계 아래 오퍼만 노출된다.")
    @Test
    void calculateLowKnowledgeOfferPool() {
        // when
        final JobTransferPolicy.JobOfferPool result = jobTransferPolicy.calculateOfferPool(
            createCareer(JobType.MID_BIZ, 40_000_000, EmploymentStatus.EMPLOYED),
            createStat(30),
            0
        );

        // then
        assertThat(result.offers())
            .extracting(JobTransferPolicy.JobOffer::jobType)
            .containsExactly(JobType.MID_BIZ, JobType.SMALL_BIZ, JobType.FREELANCER);
    }

    @DisplayName("지식이 중간이면 같은 단계와 한 단계 위 오퍼가 노출된다.")
    @Test
    void calculateMidKnowledgeOfferPool() {
        // when
        final JobTransferPolicy.JobOfferPool result = jobTransferPolicy.calculateOfferPool(
            createCareer(JobType.SMALL_BIZ, 30_000_000, EmploymentStatus.EMPLOYED),
            createStat(60),
            0
        );

        // then
        assertThat(result.offers())
            .extracting(JobTransferPolicy.JobOffer::jobType)
            .containsExactly(
                JobType.SMALL_BIZ,
                JobType.MID_BIZ,
                JobType.STARTUP,
                JobType.FREELANCER
            );
    }

    @DisplayName("지식이 높으면 두 단계 위 오퍼까지 노출된다.")
    @Test
    void calculateHighKnowledgeOfferPool() {
        // when
        final JobTransferPolicy.JobOfferPool result = jobTransferPolicy.calculateOfferPool(
            createCareer(JobType.SMALL_BIZ, 30_000_000, EmploymentStatus.EMPLOYED),
            createStat(61),
            0
        );

        // then
        assertThat(result.offers())
            .extracting(JobTransferPolicy.JobOffer::jobType)
            .containsExactly(
                JobType.SMALL_BIZ,
                JobType.MID_BIZ,
                JobType.STARTUP,
                JobType.LARGE_BIZ,
                JobType.FREELANCER
            );
    }

    @DisplayName("오퍼 연봉은 지식 배율과 직군별 시장 연봉 하한을 함께 반영한다.")
    @Test
    void calculateOfferedSalaryWithMarketFloor() {
        // when
        final JobTransferPolicy.JobOfferPool result = jobTransferPolicy.calculateOfferPool(
            createCareer(JobType.LARGE_BIZ, 30_000_000, EmploymentStatus.EMPLOYED),
            createStat(0),
            0
        );

        // then
        assertThat(result.offers())
            .extracting(
                JobTransferPolicy.JobOffer::jobType,
                JobTransferPolicy.JobOffer::offeredSalary,
                JobTransferPolicy.JobOffer::probationTurns
            )
            .containsExactly(
                tuple(JobType.LARGE_BIZ, 42_000_000, 1),
                tuple(JobType.MID_BIZ, 32_000_000, 1),
                tuple(JobType.STARTUP, 31_000_000, 1),
                tuple(JobType.FREELANCER, 28_000_000, null)
            );
    }

    @DisplayName("최근 12턴 내 친구 만나기가 2회 이상이면 오퍼 확률 보너스를 반환한다.")
    @Test
    void applyMeetFriendBonus() {
        // when
        final JobTransferPolicy.JobOfferPool result = jobTransferPolicy.calculateOfferPool(
            createCareer(JobType.SMALL_BIZ, 30_000_000, EmploymentStatus.EMPLOYED),
            createStat(50),
            2
        );

        // then
        assertThat(result.offerChanceBonusRate()).isEqualTo(10);
        assertThat(result.meetFriendBonusApplied()).isTrue();
    }

    @DisplayName("무직 상태면 이직 오퍼를 반환하지 않는다.")
    @Test
    void returnEmptyOfferPoolForUnemployed() {
        // when
        final JobTransferPolicy.JobOfferPool result = jobTransferPolicy.calculateOfferPool(
            createCareer(JobType.SMALL_BIZ, 30_000_000, EmploymentStatus.UNEMPLOYED),
            createStat(80),
            3
        );

        // then
        assertThat(result.offers()).isEmpty();
        assertThat(result.offerChanceBonusRate()).isZero();
        assertThat(result.meetFriendBonusApplied()).isFalse();
    }

    private GameCareer createCareer(
        final JobType jobType,
        final int salary,
        final EmploymentStatus employmentStatus
    ) {
        return GameCareer.builder()
            .gameId(1)
            .jobType(jobType)
            .jobTitle("사원")
            .salary(salary)
            .tenureTurns(12)
            .recentStudyCount(0)
            .recentNetworkingCount(0)
            .negotiationPreparationScore(0)
            .lastNegotiatedTurn(0)
            .employmentStatus(employmentStatus)
            .probationEndTurn(null)
            .rehireAvailableTurn(null)
            .remainingUnemploymentBenefitTurns(0)
            .salaryBeforeResignation(null)
            .build();
    }

    private GameStat createStat(final int knowledge) {
        return GameStat.builder()
            .gameId(1)
            .health(70)
            .fatigue(20)
            .stress(20)
            .happiness(50)
            .knowledge(knowledge)
            .burnout(false)
            .burnoutStartedTurn(null)
            .hospitalizedUntilTurn(null)
            .build();
    }
}
