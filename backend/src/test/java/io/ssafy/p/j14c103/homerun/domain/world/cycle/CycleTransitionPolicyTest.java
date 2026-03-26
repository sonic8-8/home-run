package io.ssafy.p.j14c103.homerun.domain.world.cycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CycleTransitionPolicyTest {

    private final CycleTransitionPolicy cycleTransitionPolicy = new CycleTransitionPolicy();

    @DisplayName("남은 턴이 남아 있으면 현재 세부 사이클을 유지하고 카운트다운만 감소한다")
    @Test
    void nextStateKeepsCurrentCycleWhileRemainingTurnsRemain() {
        // given
        final CycleState currentState = CycleState.of(
            CyclePhase.CRISIS,
            CycleType.CYCLE_STAGFLATION,
            5
        );

        // when
        final CycleState nextState = cycleTransitionPolicy.nextState(
            currentState,
            CycleDecisionRolls.of(100, 100, 100)
        );

        // then
        assertThat(nextState.getPhase()).isEqualTo(CyclePhase.CRISIS);
        assertThat(nextState.getType()).isEqualTo(CycleType.CYCLE_STAGFLATION);
        assertThat(nextState.getRemainingTurns()).isEqualTo(4);
    }

    @DisplayName("BOOM 상태는 문서의 가중치 구간에 따라 다음 상태를 결정한다")
    @Test
    void nextPhaseFromBoom() {
        // given
        CyclePhase currentPhase = CyclePhase.BOOM;

        // when
        CyclePhase boomAtLowerBound = cycleTransitionPolicy.nextPhase(currentPhase, 1);
        CyclePhase boomAtUpperBound = cycleTransitionPolicy.nextPhase(currentPhase, 55);
        CyclePhase crisisAtLowerBound = cycleTransitionPolicy.nextPhase(currentPhase, 56);
        CyclePhase crisisAtUpperBound = cycleTransitionPolicy.nextPhase(currentPhase, 80);
        CyclePhase recoveryAtLowerBound = cycleTransitionPolicy.nextPhase(currentPhase, 81);
        CyclePhase recoveryAtUpperBound = cycleTransitionPolicy.nextPhase(currentPhase, 100);

        // then
        assertThat(boomAtLowerBound).isEqualTo(CyclePhase.BOOM);
        assertThat(boomAtUpperBound).isEqualTo(CyclePhase.BOOM);
        assertThat(crisisAtLowerBound).isEqualTo(CyclePhase.CRISIS);
        assertThat(crisisAtUpperBound).isEqualTo(CyclePhase.CRISIS);
        assertThat(recoveryAtLowerBound).isEqualTo(CyclePhase.RECOVERY);
        assertThat(recoveryAtUpperBound).isEqualTo(CyclePhase.RECOVERY);
    }

    @DisplayName("CRISIS 상태는 문서의 가중치 구간에 따라 다음 상태를 결정한다")
    @Test
    void nextPhaseFromCrisis() {
        // given
        CyclePhase currentPhase = CyclePhase.CRISIS;

        // when
        CyclePhase boomAtLowerBound = cycleTransitionPolicy.nextPhase(currentPhase, 1);
        CyclePhase boomAtUpperBound = cycleTransitionPolicy.nextPhase(currentPhase, 15);
        CyclePhase crisisAtLowerBound = cycleTransitionPolicy.nextPhase(currentPhase, 16);
        CyclePhase crisisAtUpperBound = cycleTransitionPolicy.nextPhase(currentPhase, 40);
        CyclePhase recoveryAtLowerBound = cycleTransitionPolicy.nextPhase(currentPhase, 41);
        CyclePhase recoveryAtUpperBound = cycleTransitionPolicy.nextPhase(currentPhase, 100);

        // then
        assertThat(boomAtLowerBound).isEqualTo(CyclePhase.BOOM);
        assertThat(boomAtUpperBound).isEqualTo(CyclePhase.BOOM);
        assertThat(crisisAtLowerBound).isEqualTo(CyclePhase.CRISIS);
        assertThat(crisisAtUpperBound).isEqualTo(CyclePhase.CRISIS);
        assertThat(recoveryAtLowerBound).isEqualTo(CyclePhase.RECOVERY);
        assertThat(recoveryAtUpperBound).isEqualTo(CyclePhase.RECOVERY);
    }

    @DisplayName("RECOVERY 상태는 문서의 가중치 구간에 따라 다음 상태를 결정한다")
    @Test
    void nextPhaseFromRecovery() {
        // given
        CyclePhase currentPhase = CyclePhase.RECOVERY;

        // when
        CyclePhase boomAtLowerBound = cycleTransitionPolicy.nextPhase(currentPhase, 1);
        CyclePhase boomAtUpperBound = cycleTransitionPolicy.nextPhase(currentPhase, 65);
        CyclePhase crisisAtLowerBound = cycleTransitionPolicy.nextPhase(currentPhase, 66);
        CyclePhase crisisAtUpperBound = cycleTransitionPolicy.nextPhase(currentPhase, 75);
        CyclePhase recoveryAtLowerBound = cycleTransitionPolicy.nextPhase(currentPhase, 76);
        CyclePhase recoveryAtUpperBound = cycleTransitionPolicy.nextPhase(currentPhase, 100);

        // then
        assertThat(boomAtLowerBound).isEqualTo(CyclePhase.BOOM);
        assertThat(boomAtUpperBound).isEqualTo(CyclePhase.BOOM);
        assertThat(crisisAtLowerBound).isEqualTo(CyclePhase.CRISIS);
        assertThat(crisisAtUpperBound).isEqualTo(CyclePhase.CRISIS);
        assertThat(recoveryAtLowerBound).isEqualTo(CyclePhase.RECOVERY);
        assertThat(recoveryAtUpperBound).isEqualTo(CyclePhase.RECOVERY);
    }

    @DisplayName("세부 사이클 종료 시 BOOM에서 RECOVERY로 전환되면 RATE_HIKE가 선택되고 최소 지속 턴이 부여된다")
    @Test
    void nextStateFromBoomToRecovery() {
        // given
        final CycleState currentState = CycleState.of(
            CyclePhase.BOOM,
            CycleType.CYCLE_BOOM,
            1
        );

        // when
        final CycleState nextState = cycleTransitionPolicy.nextState(
            currentState,
            CycleDecisionRolls.of(81, 1, 1)
        );

        // then
        assertThat(nextState.getPhase()).isEqualTo(CyclePhase.RECOVERY);
        assertThat(nextState.getType()).isEqualTo(CycleType.CYCLE_RATE_HIKE);
        assertThat(nextState.getRemainingTurns()).isEqualTo(18);
    }

    @DisplayName("CRISIS 유지 구간에서는 subtype roll에 따라 첫 위기 타입과 마지막 위기 타입을 선택한다")
    @Test
    void nextStateWithinCrisisSubtypeBoundaries() {
        // given
        final CycleState currentState = CycleState.of(
            CyclePhase.CRISIS,
            CycleType.CYCLE_PANDEMIC,
            1
        );

        // when
        final CycleState firstCrisisType = cycleTransitionPolicy.nextState(
            currentState,
            CycleDecisionRolls.of(16, 1, 1)
        );
        final CycleState lastCrisisType = cycleTransitionPolicy.nextState(
            currentState,
            CycleDecisionRolls.of(16, 100, 100)
        );

        // then
        assertThat(firstCrisisType.getType()).isEqualTo(CycleType.CYCLE_FINANCIAL_CRISIS);
        assertThat(firstCrisisType.getRemainingTurns()).isEqualTo(30);
        assertThat(lastCrisisType.getType()).isEqualTo(CycleType.CYCLE_GREAT_DEPRESSION);
        assertThat(lastCrisisType.getRemainingTurns()).isEqualTo(60);
    }

    @DisplayName("RECOVERY 유지 구간에서는 RATE_HIKE와 GEOPOLITICAL을 50대50 경계로 선택한다")
    @Test
    void nextStateWithinRecoverySubtypeBoundaries() {
        // given
        final CycleState currentState = CycleState.of(
            CyclePhase.RECOVERY,
            CycleType.CYCLE_RATE_HIKE,
            1
        );

        // when
        final CycleState rateHike = cycleTransitionPolicy.nextState(
            currentState,
            CycleDecisionRolls.of(76, 50, 1)
        );
        final CycleState geopolitical = cycleTransitionPolicy.nextState(
            currentState,
            CycleDecisionRolls.of(76, 51, 1)
        );

        // then
        assertThat(rateHike.getType()).isEqualTo(CycleType.CYCLE_RATE_HIKE);
        assertThat(geopolitical.getType()).isEqualTo(CycleType.CYCLE_GEOPOLITICAL);
    }

    @DisplayName("랜덤값이 1 미만 또는 100 초과면 예외가 발생한다")
    @Test
    void nextPhaseWithInvalidRoll() {
        // given
        CyclePhase currentPhase = CyclePhase.BOOM;

        // when // then
        assertThatThrownBy(() -> cycleTransitionPolicy.nextPhase(currentPhase, 0))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.WORLD_CYCLE_INPUT_INVALID);
        assertThatThrownBy(() -> cycleTransitionPolicy.nextPhase(currentPhase, 101))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.WORLD_CYCLE_INPUT_INVALID);
    }

    @DisplayName("세부 사이클 판정 입력이 잘못되면 예외가 발생한다")
    @Test
    void nextStateWithInvalidRoll() {
        // given
        final CycleState currentState = CycleState.of(
            CyclePhase.BOOM,
            CycleType.CYCLE_BOOM,
            1
        );

        // when // then
        assertThatThrownBy(() -> cycleTransitionPolicy.nextState(currentState, CycleDecisionRolls.of(0, 1, 1)))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.WORLD_CYCLE_INPUT_INVALID);
        assertThatThrownBy(() -> cycleTransitionPolicy.nextState(currentState, CycleDecisionRolls.of(1, 101, 1)))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.WORLD_CYCLE_INPUT_INVALID);
        assertThatThrownBy(() -> cycleTransitionPolicy.nextState(currentState, CycleDecisionRolls.of(1, 1, 0)))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.WORLD_CYCLE_INPUT_INVALID);
    }

    @DisplayName("상태별 설명 문구를 반환한다")
    @Test
    void descriptionOf() {
        // when
        String boomDescription = cycleTransitionPolicy.descriptionOf(CyclePhase.BOOM);
        String crisisDescription = cycleTransitionPolicy.descriptionOf(CyclePhase.CRISIS);
        String recoveryDescription = cycleTransitionPolicy.descriptionOf(CyclePhase.RECOVERY);

        // then
        assertThat(boomDescription).isEqualTo("경기 호황기");
        assertThat(crisisDescription).isEqualTo("경기 위기");
        assertThat(recoveryDescription).isEqualTo("경기 회복기");
    }
}
