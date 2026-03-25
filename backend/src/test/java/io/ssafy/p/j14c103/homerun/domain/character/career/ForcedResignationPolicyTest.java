package io.ssafy.p.j14c103.homerun.domain.character.career;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ForcedResignationPolicyTest {

    private final ForcedResignationPolicy forcedResignationPolicy = new ForcedResignationPolicy();

    @DisplayName("체력이 위험 구간이면 지식에 따라 재취업 가능 턴을 계산한다.")
    @Test
    void apply() {
        // when
        final ForcedResignationPolicy.ForcedResignationResult lowKnowledgeResult =
            forcedResignationPolicy.apply(createCareer(), createStat(10), 10);
        final ForcedResignationPolicy.ForcedResignationResult midKnowledgeResult =
            forcedResignationPolicy.apply(createCareer(), createStat(45), 10);
        final ForcedResignationPolicy.ForcedResignationResult highKnowledgeResult =
            forcedResignationPolicy.apply(createCareer(), createStat(95), 10);

        // then
        assertThat(lowKnowledgeResult.rehireWaitTurns()).isEqualTo(3);
        assertThat(lowKnowledgeResult.rehireAvailableTurn()).isEqualTo(13);
        assertThat(midKnowledgeResult.rehireWaitTurns()).isEqualTo(2);
        assertThat(midKnowledgeResult.rehireAvailableTurn()).isEqualTo(12);
        assertThat(highKnowledgeResult.rehireWaitTurns()).isEqualTo(1);
        assertThat(highKnowledgeResult.rehireAvailableTurn()).isEqualTo(11);
    }

    @DisplayName("강제 퇴사 위험이 아니면 예외가 발생한다.")
    @Test
    void applyWithStableHealth() {
        // when & then
        assertThatThrownBy(() -> forcedResignationPolicy.apply(
            createCareer(),
            createSafeStat(),
            10
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.CHARACTER_REQUEST_INVALID);
    }

    private GameCareer createCareer() {
        return GameCareer.builder()
            .gameId(1)
            .jobType(JobType.MID_BIZ)
            .jobTitle("사원")
            .salary(36_000_000)
            .tenureTurns(8)
            .recentStudyCount(0)
            .recentNetworkingCount(0)
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
            .knowledge(40)
            .burnout(false)
            .burnoutStartedTurn(null)
            .hospitalizedUntilTurn(null)
            .build();
    }
}
