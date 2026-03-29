package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.world.result.GameWorldResult;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousing;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousingRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleDecisionRolls;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleState;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleType;
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
class GameWorldResultServiceTest {

    @Autowired
    private GameWorldResultService gameWorldResultService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private GameHousingRepository gameHousingRepository;

    @AfterEach
    void tearDown() {
        gameHousingRepository.deleteAllInBatch();
        gameSessionRepository.deleteAllInBatch();
    }

    @DisplayName("턴 커밋용 world result를 누락 필드 없이 조립한다")
    @Test
    void buildWorldResult() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(12, CycleState.of(CyclePhase.BOOM, CycleType.CYCLE_BOOM, 1), 101L)
        );
        final GameHousing gameHousing = GameHousing.create(
            gameSession.getGameSessionId(),
            HousingType.STUDIO,
            Money.of(10_000_000L),
            Money.of(500_000L),
            Money.of(80_000L),
            201L
        );
        gameHousingRepository.saveAndFlush(gameHousing);

        // when
        GameWorldResult result = gameWorldResultService.buildWorldResult(
            gameSession.getGameSessionId(),
            CycleDecisionRolls.of(81, 1, 1)
        );

        // then
        assertThat(result.getCycleResult()).isNotNull();
        assertThat(result.getCycleResult().getNextPhase()).isEqualTo(CyclePhase.RECOVERY);
        assertThat(result.getCycleResult().getNextType()).isEqualTo(CycleType.CYCLE_RATE_HIKE);
        assertThat(result.getCycleResult().getRemainingTurns()).isEqualTo(18);
        assertThat(result.getCycleResult().getDescription()).isEqualTo("경기 회복기");
        assertThat(result.getNewsCandidates()).isEmpty();
        assertThat(result.getEventCandidates()).isEmpty();
        assertThat(result.getHousingSnapshot()).isNotNull();
        assertThat(result.getHousingSnapshot().getCurrentHousingType()).isEqualTo(HousingType.STUDIO);
        assertThat(result.getHousingSnapshot().getCurrentPropertyId()).isEqualTo(201L);
        assertThat(result.getHousingSnapshot().getTargetPropertyId()).isEqualTo(101L);
        assertThat(result.getHousingSnapshot().isHasHousingLossSignal()).isFalse();
    }

    @DisplayName("현재 주거 데이터가 없어도 기본 housing snapshot을 포함한 world result를 반환한다")
    @Test
    void buildWorldResultWithoutHousing() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(12, CycleState.of(CyclePhase.RECOVERY, CycleType.CYCLE_RATE_HIKE, 1), 101L)
        );

        // when
        GameWorldResult result = gameWorldResultService.buildWorldResult(
            gameSession.getGameSessionId(),
            CycleDecisionRolls.of(1, 1, 1)
        );

        // then
        assertThat(result.getCycleResult().getNextPhase()).isEqualTo(CyclePhase.BOOM);
        assertThat(result.getCycleResult().getNextType()).isEqualTo(CycleType.CYCLE_BOOM);
        assertThat(result.getCycleResult().getRemainingTurns()).isEqualTo(24);
        assertThat(result.getNewsCandidates()).isEmpty();
        assertThat(result.getEventCandidates()).isEmpty();
        assertThat(result.getHousingSnapshot()).isNotNull();
        assertThat(result.getHousingSnapshot().getCurrentHousingType()).isNull();
        assertThat(result.getHousingSnapshot().getCurrentPropertyId()).isNull();
        assertThat(result.getHousingSnapshot().getTargetPropertyId()).isEqualTo(101L);
        assertThat(result.getHousingSnapshot().isHasHousingLossSignal()).isFalse();
    }

    @DisplayName("존재하지 않는 세션이면 world 세션 조회 에러를 던진다")
    @Test
    void buildWorldResultWithUnknownSession() {
        final Long unknownSessionId = 9999L;

        // when
        // then
        assertThatThrownBy(() -> gameWorldResultService.buildWorldResult(
            unknownSessionId,
            CycleDecisionRolls.of(50, 50, 50)
        ))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.WORLD_SESSION_NOT_FOUND);
    }

    @DisplayName("세션의 경제 사이클 값이 잘못되면 world cycle 상태 에러를 던진다")
    @Test
    void buildWorldResultWithInvalidCycleState() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(12, (CyclePhase) null, 101L)
        );

        // when
        // then
        assertThatThrownBy(() -> gameWorldResultService.buildWorldResult(
            gameSession.getGameSessionId(),
            CycleDecisionRolls.of(50, 50, 50)
        ))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.WORLD_CYCLE_STATE_INVALID);
    }

    private GameSession createGameSession(
        final int currentTurn,
        final CycleState cycleState,
        final Long targetPropertyId
    ) {
        final GameSession gameSession = GameSession.create(
            1L,
            1,
            "윤서",
            CharacterType.FEMALE,
            JobType.STARTUP,
            HousingType.STUDIO,
            "SEOUL",
            "GANGNAM",
            targetPropertyId,
            DataSourceType.PROFILE
        );
        gameSession.initializeCapital(
            Money.of(2_000_000L),
            Money.of(2_000_000L),
            LocalDate.of(2026, 1, 1),
            cycleState
        );
        gameSession.advanceTurn(
            currentTurn,
            LocalDate.of(2026, 1, 1),
            Money.of(2_000_000L),
            Money.of(2_000_000L),
            cycleState
        );
        return gameSession;
    }

    private GameSession createGameSession(
        final int currentTurn,
        final CyclePhase cyclePhase,
        final Long targetPropertyId
    ) {
        final GameSession gameSession = GameSession.create(
            1L,
            1,
            "윤서",
            CharacterType.FEMALE,
            JobType.STARTUP,
            HousingType.STUDIO,
            "SEOUL",
            "GANGNAM",
            targetPropertyId,
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
