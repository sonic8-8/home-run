package io.ssafy.p.j14c103.homerun.domain.character.career;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UnemploymentBenefitPolicyTest {

    private final UnemploymentBenefitPolicy unemploymentBenefitPolicy =
        new UnemploymentBenefitPolicy();

    @DisplayName("실업 상태에서는 퇴사 전 월급의 절반을 실업 급여로 계산한다.")
    @Test
    void calculate() {
        // when
        final UnemploymentBenefitPolicy.UnemploymentBenefitResult result =
            unemploymentBenefitPolicy.calculate(createUnemployedCareer());

        // then
        assertThat(result.benefitGranted()).isTrue();
        assertThat(result.benefitAmount()).isEqualTo(1_500_000);
    }

    @DisplayName("실업 급여 잔여 턴이 없으면 지급하지 않는다.")
    @Test
    void calculateWithoutRemainingTurns() {
        // when
        final UnemploymentBenefitPolicy.UnemploymentBenefitResult result =
            unemploymentBenefitPolicy.calculate(createCareerWithoutBenefitTurns());

        // then
        assertThat(result.benefitGranted()).isFalse();
        assertThat(result.benefitAmount()).isZero();
    }

    private GameCareer createUnemployedCareer() {
        return GameCareer.builder()
            .gameId(1)
            .jobType(JobType.STARTUP)
            .jobTitle("사원")
            .salary(36_000_000)
            .tenureTurns(8)
            .recentStudyCount(0)
            .recentNetworkingCount(0)
            .negotiationPreparationScore(0)
            .lastNegotiatedTurn(0)
            .employmentStatus(EmploymentStatus.UNEMPLOYED)
            .probationEndTurn(null)
            .rehireAvailableTurn(12)
            .remainingUnemploymentBenefitTurns(3)
            .salaryBeforeResignation(36_000_000)
            .build();
    }

    private GameCareer createCareerWithoutBenefitTurns() {
        return GameCareer.builder()
            .gameId(1)
            .jobType(JobType.STARTUP)
            .jobTitle("사원")
            .salary(36_000_000)
            .tenureTurns(8)
            .recentStudyCount(0)
            .recentNetworkingCount(0)
            .negotiationPreparationScore(0)
            .lastNegotiatedTurn(0)
            .employmentStatus(EmploymentStatus.UNEMPLOYED)
            .probationEndTurn(null)
            .rehireAvailableTurn(12)
            .remainingUnemploymentBenefitTurns(0)
            .salaryBeforeResignation(36_000_000)
            .build();
    }
}
