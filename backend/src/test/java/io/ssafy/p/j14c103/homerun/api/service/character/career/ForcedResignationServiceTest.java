package io.ssafy.p.j14c103.homerun.api.service.character.career;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.character.career.request.ForcedResignationServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.career.response.ForcedResignationServiceResponse;
import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ForcedResignationServiceTest {

    @Autowired
    private ForcedResignationService forcedResignationService;

    @DisplayName("건강 위기 상태면 강제 퇴사와 재취업 대기 상태를 반영한다.")
    @Test
    void forceResign() {
        // given
        final GameCareer gameCareer = createCareer();
        final ForcedResignationServiceRequest request = ForcedResignationServiceRequest.of(
            gameCareer,
            createRiskStat(45),
            10
        );

        // when
        final ForcedResignationServiceResponse response = forcedResignationService.forceResign(
            request
        );

        // then
        assertThat(response.isForcedResigned()).isTrue();
        assertThat(response.getEmploymentStatus()).isEqualTo(EmploymentStatus.UNEMPLOYED);
        assertThat(response.getRehireAvailableTurn()).isEqualTo(12);
        assertThat(response.getRemainingUnemploymentBenefitTurns()).isEqualTo(3);
        assertThat(response.getSalaryBeforeResignation()).isEqualTo(36_000_000);
        assertThat(response.getMessage()).isEqualTo("건강 악화로 강제 퇴사했습니다.");
        assertThat(gameCareer.getProbationEndTurn()).isNull();
    }

    @DisplayName("건강 위기 상태가 아니면 강제 퇴사를 처리하지 않는다.")
    @Test
    void forceResignWithSafeHealth() {
        // given
        final ForcedResignationServiceRequest request = ForcedResignationServiceRequest.of(
            createCareer(),
            createSafeStat(),
            10
        );

        // when & then
        assertThatThrownBy(() -> forcedResignationService.forceResign(request))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.CHARACTER_REQUEST_INVALID);
    }

    private GameCareer createCareer() {
        return GameCareer.builder()
            .gameId(1)
            .jobType(JobType.STARTUP)
            .jobTitle("사원")
            .salary(36_000_000)
            .tenureTurns(10)
            .recentStudyCount(0)
            .recentNetworkingCount(0)
            .negotiationPreparationScore(0)
            .lastNegotiatedTurn(0)
            .employmentStatus(EmploymentStatus.PROBATION)
            .probationEndTurn(11)
            .rehireAvailableTurn(null)
            .remainingUnemploymentBenefitTurns(0)
            .salaryBeforeResignation(null)
            .build();
    }

    private GameStat createRiskStat(final int knowledge) {
        return GameStat.builder()
            .gameId(1)
            .health(9)
            .fatigue(20)
            .stress(20)
            .happiness(50)
            .knowledge(knowledge)
            .burnout(false)
            .burnoutStartedTurn(null)
            .hospitalizedUntilTurn(null)
            .build();
    }

    private GameStat createSafeStat() {
        return GameStat.builder()
            .gameId(1)
            .health(10)
            .fatigue(20)
            .stress(20)
            .happiness(50)
            .knowledge(45)
            .burnout(false)
            .burnoutStartedTurn(null)
            .hospitalizedUntilTurn(null)
            .build();
    }
}
