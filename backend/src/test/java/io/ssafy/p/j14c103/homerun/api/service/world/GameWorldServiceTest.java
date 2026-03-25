package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.world.response.GameTurnResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class GameWorldServiceTest {

    @Autowired
    private GameWorldService gameWorldService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @AfterEach
    void tearDown() {
        gameSessionRepository.deleteAllInBatch();
    }

    @DisplayName("세션 스냅샷을 읽어 턴 조회 응답으로 조립한다")
    @Test
    void getTurn() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(12, CyclePhase.BOOM));

        // when
        GameTurnResponse response = gameWorldService.getTurn(gameSession.getGameSessionId());

        // then
        assertThat(response.getTurnNumber()).isEqualTo(12);
        assertThat(response.getCurrentDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(response.getMonth()).isEqualTo(1);
        assertThat(response.getEconomicCycle().getPhase()).isEqualTo(CyclePhase.BOOM);
        assertThat(response.getEconomicCycle().getDescription()).isEqualTo("경기 호황기");
        assertThat(response.getNews()).isEmpty();
    }

    @DisplayName("존재하지 않는 세션이면 world 세션 조회 에러를 던진다")
    @Test
    void getTurnWithUnknownSession() {
        // given
        final Long unknownSessionId = 9999L;

        // when
        // then
        assertThatThrownBy(() -> gameWorldService.getTurn(unknownSessionId))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.WORLD_SESSION_NOT_FOUND);
    }

    @DisplayName("세션의 경제 사이클 값이 잘못되면 world cycle 상태 에러를 던진다")
    @Test
    void getTurnWithInvalidCycleState() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(createGameSession(12, null));

        // when
        // then
        assertThatThrownBy(() -> gameWorldService.getTurn(gameSession.getGameSessionId()))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
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
            "SEOUL",
            "GANGNAM",
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
