package io.ssafy.p.j14c103.homerun.domain.character.career;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameCareerFactoryTest {

    @DisplayName("초기 커리어 생성은 수습 상태와 종료 턴을 함께 저장한다.")
    @Test
    void create() {
        // given

        // when
        final GameCareer gameCareer = GameCareer.create(
            1,
            JobType.MID_BIZ,
            "수습/인턴",
            32_000_000,
            0,
            0,
            0,
            0,
            0,
            EmploymentStatus.PROBATION,
            6,
            null,
            0,
            null
        );

        // then
        assertThat(gameCareer.getGameId()).isEqualTo(1);
        assertThat(gameCareer.getEmploymentStatus()).isEqualTo(EmploymentStatus.PROBATION);
        assertThat(gameCareer.getProbationEndTurn()).isEqualTo(6);
    }

    @DisplayName("수습 상태의 초기 커리어 생성에서 종료 턴이 없으면 CHARACTER_POLICY_INVALID가 발생한다.")
    @Test
    void createWithoutProbationEndTurn() {
        // when & then
        assertThatThrownBy(() -> GameCareer.create(
            1,
            JobType.MID_BIZ,
            "수습/인턴",
            32_000_000,
            0,
            0,
            0,
            0,
            0,
            EmploymentStatus.PROBATION,
            null,
            null,
            0,
            null
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.CHARACTER_POLICY_INVALID);
    }
}
