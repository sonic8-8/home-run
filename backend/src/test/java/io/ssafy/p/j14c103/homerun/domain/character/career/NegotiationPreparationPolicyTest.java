package io.ssafy.p.j14c103.homerun.domain.character.career;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NegotiationPreparationPolicyTest {

    private final NegotiationPreparationPolicy negotiationPreparationPolicy =
        new NegotiationPreparationPolicy();

    @DisplayName("공부와 네트워킹 누적에 지식 구간 보너스를 더해 협상 준비도를 계산한다.")
    @Test
    void calculate() {
        // given
        final GameCareer gameCareer = createGameCareer(1, 1);
        final GameStat gameStat = createGameStat(75);

        // when
        final NegotiationPreparationPolicy.PreparationResult result =
            negotiationPreparationPolicy.calculate(
                gameCareer,
                gameStat,
                List.of(ActionType.STUDY, ActionType.NETWORKING, ActionType.REST),
                List.of(ActionType.REST, ActionType.REST, ActionType.REST)
            );

        // then
        assertThat(result.recentStudyCount()).isEqualTo(2);
        assertThat(result.recentNetworkingCount()).isEqualTo(2);
        assertThat(result.negotiationPreparationScore()).isEqualTo(15);
    }

    @DisplayName("12턴 창에서 빠지는 행동은 협상 준비도 카운터에서 제외한다.")
    @Test
    void calculateWithExpiredTurnActions() {
        // given
        final GameCareer gameCareer = createGameCareer(2, 2);
        final GameStat gameStat = createGameStat(45);

        // when
        final NegotiationPreparationPolicy.PreparationResult result =
            negotiationPreparationPolicy.calculate(
                gameCareer,
                gameStat,
                List.of(ActionType.REST, ActionType.HOBBY, ActionType.REST),
                List.of(ActionType.STUDY, ActionType.NETWORKING, ActionType.REST)
            );

        // then
        assertThat(result.recentStudyCount()).isEqualTo(1);
        assertThat(result.recentNetworkingCount()).isEqualTo(1);
        assertThat(result.negotiationPreparationScore()).isEqualTo(7);
    }

    private GameCareer createGameCareer(
        final int recentStudyCount,
        final int recentNetworkingCount
    ) {
        return GameCareer.builder()
            .gameId(1001)
            .jobType(JobType.SMALL_BIZ)
            .jobTitle("사원")
            .salary(30_000_000)
            .tenureTurns(24)
            .recentStudyCount(recentStudyCount)
            .recentNetworkingCount(recentNetworkingCount)
            .negotiationPreparationScore(0)
            .lastNegotiatedTurn(0)
            .employmentStatus(EmploymentStatus.EMPLOYED)
            .probationEndTurn(null)
            .rehireAvailableTurn(null)
            .remainingUnemploymentBenefitTurns(0)
            .salaryBeforeResignation(null)
            .build();
    }

    private GameStat createGameStat(final int knowledge) {
        return GameStat.builder()
            .gameId(1001)
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
