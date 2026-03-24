package io.ssafy.p.j14c103.homerun.domain.character.career;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SalaryNegotiationPolicyTest {

    private final SalaryNegotiationPolicy salaryNegotiationPolicy = new SalaryNegotiationPolicy();

    @DisplayName("연봉 협상은 12턴 이내 재협상을 허용하지 않는다.")
    @Test
    void negotiateWithinTwelveTurns() {
        // given
        final GameCareer gameCareer = createGameCareer(JobType.SMALL_BIZ, 30_000_000, 10);
        final GameStat gameStat = createGameStat(70, 70);

        // when & then
        assertThatThrownBy(() -> salaryNegotiationPolicy.negotiate(gameCareer, gameStat, 21))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.CHARACTER_REQUEST_INVALID);
    }

    @DisplayName("지식 구간이 높아질수록 인상률이 커진다.")
    @Test
    void negotiateByKnowledgeRange() {
        // given
        final GameCareer gameCareer = createGameCareer(JobType.SMALL_BIZ, 30_000_000, 0);

        // when
        final SalaryNegotiationPolicy.NegotiationResult lowKnowledgeResult =
            salaryNegotiationPolicy.negotiate(gameCareer, createGameStat(70, 30), 12);
        final SalaryNegotiationPolicy.NegotiationResult midKnowledgeResult =
            salaryNegotiationPolicy.negotiate(gameCareer, createGameStat(70, 45), 12);
        final SalaryNegotiationPolicy.NegotiationResult highKnowledgeResult =
            salaryNegotiationPolicy.negotiate(gameCareer, createGameStat(70, 75), 12);

        // then
        assertThat(lowKnowledgeResult.raiseRate()).isEqualTo(5);
        assertThat(midKnowledgeResult.raiseRate()).isEqualTo(7);
        assertThat(highKnowledgeResult.raiseRate()).isEqualTo(10);
    }

    @DisplayName("체력이 50 미만이면 연봉 협상 인상률에 페널티가 적용된다.")
    @Test
    void negotiateWithLowHealthPenalty() {
        // given
        final GameCareer gameCareer = createGameCareer(JobType.SMALL_BIZ, 30_000_000, 0);

        // when
        final SalaryNegotiationPolicy.NegotiationResult healthyResult =
            salaryNegotiationPolicy.negotiate(gameCareer, createGameStat(70, 75), 12);
        final SalaryNegotiationPolicy.NegotiationResult lowHealthResult =
            salaryNegotiationPolicy.negotiate(gameCareer, createGameStat(49, 75), 12);

        // then
        assertThat(healthyResult.raiseRate()).isEqualTo(10);
        assertThat(lowHealthResult.raiseRate()).isEqualTo(8);
    }

    @DisplayName("협상 준비도 점수 구간이 높아질수록 추가 인상률이 커진다.")
    @Test
    void negotiateByPreparationScoreRange() {
        // given
        final GameStat gameStat = createGameStat(70, 30);

        // when
        final SalaryNegotiationPolicy.NegotiationResult lowPreparationResult =
            salaryNegotiationPolicy.negotiate(createGameCareer(JobType.SMALL_BIZ, 30_000_000, 0, 5), gameStat, 12);
        final SalaryNegotiationPolicy.NegotiationResult midPreparationResult =
            salaryNegotiationPolicy.negotiate(createGameCareer(JobType.SMALL_BIZ, 30_000_000, 0, 6), gameStat, 12);
        final SalaryNegotiationPolicy.NegotiationResult highPreparationResult =
            salaryNegotiationPolicy.negotiate(createGameCareer(JobType.SMALL_BIZ, 30_000_000, 0, 11), gameStat, 12);

        // then
        assertThat(lowPreparationResult.raiseRate()).isEqualTo(5);
        assertThat(midPreparationResult.raiseRate()).isEqualTo(7);
        assertThat(highPreparationResult.raiseRate()).isEqualTo(10);
    }

    @DisplayName("연봉 협상 결과는 새 연봉과 마지막 협상 턴을 반환한다.")
    @Test
    void negotiate() {
        // given
        final GameCareer gameCareer = createGameCareer(JobType.SMALL_BIZ, 30_000_000, 0);
        final GameStat gameStat = createGameStat(70, 75);

        // when
        final SalaryNegotiationPolicy.NegotiationResult result =
            salaryNegotiationPolicy.negotiate(gameCareer, gameStat, 13);

        // then
        assertThat(result.previousSalary()).isEqualTo(30_000_000);
        assertThat(result.newSalary()).isEqualTo(33_000_000);
        assertThat(result.raiseRate()).isEqualTo(10);
        assertThat(result.lastNegotiatedTurn()).isEqualTo(13);
        assertThat(result.message()).isEqualTo("연봉 협상에 성공했습니다!");
    }

    private GameCareer createGameCareer(
        final JobType jobType,
        final int salary,
        final int lastNegotiatedTurn
    ) {
        return createGameCareer(jobType, salary, lastNegotiatedTurn, 0);
    }

    private GameCareer createGameCareer(
        final JobType jobType,
        final int salary,
        final int lastNegotiatedTurn,
        final int negotiationPreparationScore
    ) {
        return GameCareer.builder()
            .gameId(1001)
            .jobType(jobType)
            .jobTitle("사원")
            .salary(salary)
            .tenureTurns(24)
            .recentStudyCount(0)
            .recentNetworkingCount(0)
            .negotiationPreparationScore(negotiationPreparationScore)
            .lastNegotiatedTurn(lastNegotiatedTurn)
            .employmentStatus(EmploymentStatus.EMPLOYED)
            .probationEndTurn(null)
            .rehireAvailableTurn(null)
            .remainingUnemploymentBenefitTurns(0)
            .salaryBeforeResignation(null)
            .build();
    }

    private GameStat createGameStat(final int health, final int knowledge) {
        return GameStat.builder()
            .gameId(1001)
            .health(health)
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
