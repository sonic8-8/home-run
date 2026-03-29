package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.world.response.GameTurnWorldStateResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GameTurnWorldStateServiceTest extends IntegrationTestSupport {

    @Autowired
    private GameTurnWorldStateService gameTurnWorldStateService;

    @DisplayName("게임 턴 world state 조회는 경제 사이클 단계와 설명을 반환한다.")
    @Test
    void getWorldState() {
        // given
        final GameSession gameSession = createGameSession(12, CyclePhase.BOOM);

        // when
        final GameTurnWorldStateResponse response = gameTurnWorldStateService.getWorldState(
            gameSession
        );

        // then
        assertThat(response.getPhase()).isEqualTo(CyclePhase.BOOM);
        assertThat(response.getDescription()).isEqualTo("경기 호황기");
    }

    @DisplayName("게임 턴 world state 조회 중 세션의 경제 사이클 값이 없으면 WORLD_CYCLE_STATE_INVALID가 발생한다.")
    @Test
    void getWorldStateWithInvalidCycleState() {
        // given
        final GameSession gameSession = createGameSession(12, null);

        // when & then
        assertThatThrownBy(() -> gameTurnWorldStateService.getWorldState(gameSession))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.WORLD_CYCLE_STATE_INVALID);
    }

    private GameSession createGameSession(final int currentTurn, final CyclePhase cyclePhase) {
        final GameSession gameSession = GameSession.create(
            1L,
            1,
            "윤서",
            CharacterType.FEMALE,
            JobType.STARTUP,
            HousingType.STUDIO,
            "11",
            "11680",
            101L,
            DataSourceType.PROFILE
        );
        gameSession.initializeCapital(
            Money.of(2_000_000L),
            Money.of(2_000_000L),
            LocalDate.of(2026, 1, 1),
            cyclePhase
        );
        gameSession.advanceTurn(
            currentTurn,
            LocalDate.of(2026, 1, 1),
            Money.of(2_000_000L),
            Money.of(2_000_000L),
            cyclePhase
        );
        return gameSession;
    }
}
