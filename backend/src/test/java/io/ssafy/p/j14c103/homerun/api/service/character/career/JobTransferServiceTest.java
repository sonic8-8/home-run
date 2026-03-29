package io.ssafy.p.j14c103.homerun.api.service.character.career;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.character.career.request.JobTransferServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.career.response.JobTransferServiceResponse;
import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class JobTransferServiceTest extends IntegrationTestSupport {

    @Autowired
    private JobTransferService jobTransferService;

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
        assertThat(response.previousJobType()).isEqualTo(JobType.SMALL_BIZ);
        assertThat(response.newJobType()).isEqualTo(JobType.LARGE_BIZ);
        assertThat(response.newJobTitle()).isEqualTo("수습/인턴");
        assertThat(response.newSalary()).isEqualTo(45_000_000);
        assertThat(response.probationEndTurn()).isEqualTo(17);
        assertThat(response.tenureReset()).isTrue();
        assertThat(response.message()).isEqualTo("OO 대기업으로 이직했습니다.");
        assertThat(gameCareer.getJobType()).isEqualTo(JobType.LARGE_BIZ);
        assertThat(gameCareer.getJobTitle()).isEqualTo("수습/인턴");
        assertThat(gameCareer.getSalary()).isEqualTo(45_000_000);
        assertThat(gameCareer.getTenureTurns()).isZero();
        assertThat(gameCareer.getProbationEndTurn()).isEqualTo(17);
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
