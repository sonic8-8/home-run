package io.ssafy.p.j14c103.homerun.domain.character.career;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameCareerForcedResignationTest {

    @DisplayName("강제 퇴사 시 실업 상태와 재취업 대기 정보가 갱신된다.")
    @Test
    void forceResign() {
        // given
        final GameCareer gameCareer = createCareer();
        final ForcedResignationPolicy.ForcedResignationResult result =
            ForcedResignationPolicy.ForcedResignationResult.of(36_000_000, 2, 14);

        // when
        gameCareer.forceResign(result);

        // then
        assertThat(gameCareer.getEmploymentStatus()).isEqualTo(EmploymentStatus.UNEMPLOYED);
        assertThat(gameCareer.getProbationEndTurn()).isNull();
        assertThat(gameCareer.getRehireAvailableTurn()).isEqualTo(14);
        assertThat(gameCareer.getRemainingUnemploymentBenefitTurns()).isEqualTo(3);
        assertThat(gameCareer.getSalaryBeforeResignation()).isEqualTo(36_000_000);
    }

    @DisplayName("실업 급여를 소모하면 잔여 턴이 감소한다.")
    @Test
    void consumeUnemploymentBenefit() {
        // given
        final GameCareer gameCareer = createCareer();
        gameCareer.forceResign(ForcedResignationPolicy.ForcedResignationResult.of(36_000_000, 2, 14));

        // when
        final boolean firstConsumed = gameCareer.consumeUnemploymentBenefit();
        final boolean secondConsumed = gameCareer.consumeUnemploymentBenefit();
        final boolean thirdConsumed = gameCareer.consumeUnemploymentBenefit();
        final boolean fourthConsumed = gameCareer.consumeUnemploymentBenefit();

        // then
        assertThat(firstConsumed).isTrue();
        assertThat(secondConsumed).isTrue();
        assertThat(thirdConsumed).isTrue();
        assertThat(fourthConsumed).isFalse();
        assertThat(gameCareer.getRemainingUnemploymentBenefitTurns()).isZero();
    }

    private GameCareer createCareer() {
        return GameCareer.builder()
            .gameId(1)
            .jobType(JobType.STARTUP)
            .jobTitle("사원")
            .salary(36_000_000)
            .tenureTurns(10)
            .recentStudyCount(1)
            .recentNetworkingCount(1)
            .negotiationPreparationScore(0)
            .lastNegotiatedTurn(0)
            .employmentStatus(EmploymentStatus.PROBATION)
            .probationEndTurn(12)
            .rehireAvailableTurn(null)
            .remainingUnemploymentBenefitTurns(0)
            .salaryBeforeResignation(null)
            .build();
    }
}
