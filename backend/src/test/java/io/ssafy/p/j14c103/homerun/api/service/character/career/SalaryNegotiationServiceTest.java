package io.ssafy.p.j14c103.homerun.api.service.character.career;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.character.career.request.SalaryNegotiationRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.career.response.SalaryNegotiationResultResponse;
import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.history.GameplayHistory;
import io.ssafy.p.j14c103.homerun.domain.history.GameplayHistoryRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SalaryNegotiationServiceTest {

    @Autowired
    private SalaryNegotiationService salaryNegotiationService;

    @Autowired
    private GameplayHistoryRepository gameplayHistoryRepository;

    @DisplayName("연봉 협상 서비스는 협상 결과와 갱신 턴 정보를 반환한다.")
    @Test
    void negotiate() {
        // given
        final SalaryNegotiationRequest request = SalaryNegotiationRequest.of(
            createGameCareer(30_000_000, 0),
            createGameStat(70, 75),
            13
        );

        // when
        final SalaryNegotiationResultResponse response = salaryNegotiationService.negotiate(
            request
        );

        // then
        assertThat(response.success()).isTrue();
        assertThat(response.previousSalary()).isEqualTo(30_000_000);
        assertThat(response.newSalary()).isEqualTo(33_000_000);
        assertThat(response.raiseRate()).isEqualTo(10);
        assertThat(response.lastNegotiatedTurn()).isEqualTo(13);
        assertThat(response.message()).isEqualTo("연봉 협상에 성공했습니다!");
        assertThat(request.gameCareer().getSalary()).isEqualTo(33_000_000);
        assertThat(request.gameCareer().getLastNegotiatedTurn()).isEqualTo(13);

        final List<GameplayHistory> histories =
            gameplayHistoryRepository.findAllByGameIdOrderByOccurredTurnAscHistoryIdAsc(1001);
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getTableName()).isEqualTo("게임커리어");
        assertThat(histories.get(0).getColumnName()).isEqualTo("연봉,마지막협상턴");
        assertThat(histories.get(0).getBeforeValue()).contains("\"salary\":30000000");
        assertThat(histories.get(0).getAfterValue()).contains("\"salary\":33000000");
        assertThat(histories.get(0).getEffectPayload()).contains("\"raiseRate\":10");
        assertThat(histories.get(0).getSummary()).isEqualTo("연봉 협상에 성공했습니다!");
    }

    @DisplayName("연봉 협상 서비스는 12턴 이내 재협상 요청이면 예외를 반환한다.")
    @Test
    void negotiateWithinTwelveTurns() {
        // given
        final SalaryNegotiationRequest request = SalaryNegotiationRequest.of(
            createGameCareer(30_000_000, 10),
            createGameStat(70, 75),
            21
        );

        // when & then
        assertThatThrownBy(() -> salaryNegotiationService.negotiate(request))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.CHARACTER_REQUEST_INVALID);
    }

    private GameCareer createGameCareer(final int salary, final int lastNegotiatedTurn) {
        return GameCareer.builder()
            .gameId(1001)
            .jobType(JobType.SMALL_BIZ)
            .jobTitle("사원")
            .salary(salary)
            .tenureTurns(24)
            .recentStudyCount(0)
            .recentNetworkingCount(0)
            .negotiationPreparationScore(0)
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
