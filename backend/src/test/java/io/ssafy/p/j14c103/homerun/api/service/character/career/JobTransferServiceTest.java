package io.ssafy.p.j14c103.homerun.api.service.character.career;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.character.career.request.JobTransferServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.career.response.JobTransferServiceResponse;
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
class JobTransferServiceTest {

    @Autowired
    private JobTransferService jobTransferService;

    @Autowired
    private GameplayHistoryRepository gameplayHistoryRepository;

    @DisplayName("선택한 오퍼를 수락하면 커리어 상태와 응답 계약이 함께 갱신된다.")
    @Test
    void transfer() {
        // given
        final GameCareer gameCareer = createCareer(JobType.SMALL_BIZ, 30_000_000);
        final JobTransferServiceRequest request = JobTransferServiceRequest.of(
            gameCareer,
            createStat(100),
            2,
            "OFFER-004",
            15
        );

        // when
        final JobTransferServiceResponse response = jobTransferService.transfer(request);

        // then
        assertThat(response.getPreviousJobType()).isEqualTo(JobType.SMALL_BIZ);
        assertThat(response.getNewJobType()).isEqualTo(JobType.LARGE_BIZ);
        assertThat(response.getNewJobTitle()).isEqualTo("수습/인턴");
        assertThat(response.getNewSalary()).isEqualTo(45_000_000);
        assertThat(response.getProbationEndTurn()).isEqualTo(17);
        assertThat(response.isTenureReset()).isTrue();
        assertThat(response.getMessage()).isEqualTo("OO 대기업으로 이직했습니다.");
        assertThat(gameCareer.getJobType()).isEqualTo(JobType.LARGE_BIZ);
        assertThat(gameCareer.getJobTitle()).isEqualTo("수습/인턴");
        assertThat(gameCareer.getSalary()).isEqualTo(45_000_000);
        assertThat(gameCareer.getTenureTurns()).isZero();
        assertThat(gameCareer.getProbationEndTurn()).isEqualTo(17);

        final List<GameplayHistory> histories =
            gameplayHistoryRepository.findAllByGameIdOrderByOccurredTurnAscHistoryIdAsc(1);
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getTableName()).isEqualTo("게임커리어");
        assertThat(histories.get(0).getColumnName())
            .isEqualTo("직업유형,직급,연봉,근속턴수,고용상태,수습종료턴,재취업가능턴,실업급여잔여턴수,퇴사전연봉");
        assertThat(histories.get(0).getBeforeValue()).contains("\"jobType\":\"SMALL_BIZ\"");
        assertThat(histories.get(0).getAfterValue()).contains("\"jobType\":\"LARGE_BIZ\"");
        assertThat(histories.get(0).getSummary()).isEqualTo("OO 대기업으로 이직했습니다.");
    }

    @DisplayName("오퍼 목록에 없는 offerId를 수락하면 예외가 발생한다.")
    @Test
    void transferWithInvalidOfferId() {
        // given
        final JobTransferServiceRequest request = JobTransferServiceRequest.of(
            createCareer(JobType.SMALL_BIZ, 30_000_000),
            createStat(100),
            2,
            "OFFER-999",
            15
        );

        // when & then
        assertThatThrownBy(() -> jobTransferService.transfer(request))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.CHARACTER_REQUEST_INVALID);
    }

    @DisplayName("실직 상태에서 재취업 오퍼를 수락하면 재취업 이력과 메시지를 반환한다.")
    @Test
    void transferForUnemployedCareer() {
        // given
        final GameCareer gameCareer = createUnemployedCareer(JobType.SMALL_BIZ, 40_000_000, 12);
        final JobTransferServiceRequest request = JobTransferServiceRequest.of(
            gameCareer,
            createStat(60),
            1,
            "OFFER-002",
            12
        );

        // when
        final JobTransferServiceResponse response = jobTransferService.transfer(request);

        // then
        assertThat(response.getPreviousJobType()).isEqualTo(JobType.SMALL_BIZ);
        assertThat(response.getNewJobType()).isEqualTo(JobType.MID_BIZ);
        assertThat(response.getNewSalary()).isEqualTo(36_000_000);
        assertThat(response.getProbationEndTurn()).isEqualTo(14);
        assertThat(response.getMessage()).isEqualTo("OO 중견기업에 재취업했습니다.");
        assertThat(gameCareer.getEmploymentStatus()).isEqualTo(EmploymentStatus.PROBATION);
        assertThat(gameCareer.getRehireAvailableTurn()).isNull();
        assertThat(gameCareer.getRemainingUnemploymentBenefitTurns()).isZero();
        assertThat(gameCareer.getSalaryBeforeResignation()).isNull();

        final List<GameplayHistory> histories =
            gameplayHistoryRepository.findAllByGameIdOrderByOccurredTurnAscHistoryIdAsc(1);
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getSummary()).isEqualTo("OO 중견기업에 재취업했습니다.");
        assertThat(histories.get(0).getBeforeValue()).contains("\"employmentStatus\":\"UNEMPLOYED\"");
        assertThat(histories.get(0).getAfterValue()).contains("\"employmentStatus\":\"PROBATION\"");
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
            .rehireAvailableTurn(null)
            .remainingUnemploymentBenefitTurns(0)
            .salaryBeforeResignation(null)
            .build();
    }

    private GameCareer createUnemployedCareer(
        final JobType jobType,
        final int salaryBeforeResignation,
        final int rehireAvailableTurn
    ) {
        return GameCareer.builder()
            .gameId(1)
            .jobType(jobType)
            .jobTitle("사원")
            .salary(salaryBeforeResignation)
            .tenureTurns(14)
            .recentStudyCount(1)
            .recentNetworkingCount(1)
            .negotiationPreparationScore(0)
            .lastNegotiatedTurn(0)
            .employmentStatus(EmploymentStatus.UNEMPLOYED)
            .probationEndTurn(null)
            .rehireAvailableTurn(rehireAvailableTurn)
            .remainingUnemploymentBenefitTurns(3)
            .salaryBeforeResignation(salaryBeforeResignation)
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
