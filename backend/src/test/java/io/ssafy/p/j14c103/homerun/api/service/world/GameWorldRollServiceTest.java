package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameWorldRollServiceTest {

    private final GameWorldRollService gameWorldRollService = new GameWorldRollService();

    @DisplayName("currentTurn이 0인 새 세션도 월드 roll을 계산할 수 있다.")
    @Test
    void resolveTurnRollForFreshSession() {
        final int roll = gameWorldRollService.resolveTurnRoll(66L, 0);

        assertThat(roll).isBetween(1, 100);
    }

    @DisplayName("turnNumber가 음수면 WORLD_RESULT_INVALID가 발생한다.")
    @Test
    void resolveTurnRollWithNegativeTurn() {
        assertThatThrownBy(() -> gameWorldRollService.resolveTurnRoll(66L, -1))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.WORLD_RESULT_INVALID);
    }
}
