package io.ssafy.p.j14c103.homerun.domain.character;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameStatTest {

    @DisplayName("유효한 초기 스탯으로 게임 스탯을 생성할 수 있다.")
    @Test
    void create() {
        // given

        // when
        final GameStat gameStat = GameStat.create(1, 70, 10, 10, 50, 50, 1);

        // then
        assertThat(gameStat.getGameId()).isEqualTo(1);
        assertThat(gameStat.getHealth()).isEqualTo(70);
        assertThat(gameStat.getFatigue()).isEqualTo(10);
        assertThat(gameStat.getStress()).isEqualTo(10);
        assertThat(gameStat.getHappiness()).isEqualTo(50);
        assertThat(gameStat.getKnowledge()).isEqualTo(50);
        assertThat(gameStat.getBurnout()).isFalse();
        assertThat(gameStat.getBurnoutStartedTurn()).isNull();
        assertThat(gameStat.getHospitalizedUntilTurn()).isNull();
    }

    @DisplayName("초기 생성 시 번아웃 조건을 만족하면 번아웃 상태로 시작한다.")
    @Test
    void createWithBurnout() {
        // given

        // when
        final GameStat gameStat = GameStat.create(1, 70, 80, 85, 50, 50, 3);

        // then
        assertThat(gameStat.getBurnout()).isTrue();
        assertThat(gameStat.getBurnoutStartedTurn()).isEqualTo(3);
    }

    @DisplayName("스탯 변경 시 값은 0에서 100 사이로 보정된다.")
    @Test
    void applyChangeClampStatRange() {
        // given
        final GameStat gameStat = GameStat.create(1, 10, 95, 5, 90, 10, 1);

        // when
        gameStat.applyChange(-30, 20, -50, 30, 200, 2);

        // then
        assertThat(gameStat.getHealth()).isZero();
        assertThat(gameStat.getFatigue()).isEqualTo(100);
        assertThat(gameStat.getStress()).isZero();
        assertThat(gameStat.getHappiness()).isEqualTo(100);
        assertThat(gameStat.getKnowledge()).isEqualTo(100);
    }

    @DisplayName("피로도와 스트레스가 모두 80 이상이면 번아웃으로 전환한다.")
    @Test
    void applyChangeEnterBurnout() {
        // given
        final GameStat gameStat = GameStat.create(1, 70, 70, 72, 50, 50, 1);

        // when
        gameStat.applyChange(0, 10, 8, 0, 0, 4);

        // then
        assertThat(gameStat.getBurnout()).isTrue();
        assertThat(gameStat.getBurnoutStartedTurn()).isEqualTo(4);
    }

    @DisplayName("번아웃 상태에서 피로도 또는 스트레스가 내려가면 번아웃이 해제된다.")
    @Test
    void applyChangeRecoverBurnout() {
        // given
        final GameStat gameStat = GameStat.create(1, 70, 85, 82, 50, 50, 2);

        // when
        gameStat.applyChange(0, -10, 0, 0, 0, 3);

        // then
        assertThat(gameStat.getBurnout()).isFalse();
        assertThat(gameStat.getBurnoutStartedTurn()).isNull();
    }

    @DisplayName("체력이 9 이하이면 강제 퇴사 위험 상태다.")
    @Test
    void isForcedResignationRisk() {
        // given
        final GameStat gameStat = GameStat.create(1, 15, 10, 10, 50, 50, 1);

        // when
        gameStat.applyChange(-6, 0, 0, 0, 0, 2);

        // then
        assertThat(gameStat.isForcedResignationRisk()).isTrue();
    }

    @DisplayName("입원 종료 턴 전까지는 입원 상태로 판단한다.")
    @Test
    void hospitalizeUntil() {
        // given
        final GameStat gameStat = GameStat.create(1, 70, 10, 10, 50, 50, 1);

        // when
        gameStat.hospitalizeUntil(5);

        // then
        assertThat(gameStat.isHospitalizedAt(4)).isTrue();
        assertThat(gameStat.isHospitalizedAt(5)).isTrue();
        assertThat(gameStat.isHospitalizedAt(6)).isFalse();
    }

    @DisplayName("초기 스탯이 범위를 벗어나면 예외가 발생한다.")
    @Test
    void createWithInvalidStatRange() {
        // given

        // when & then
        assertThatThrownBy(() -> GameStat.create(1, 101, 10, 10, 50, 50, 1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("health");
    }
}
