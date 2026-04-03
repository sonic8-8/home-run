package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.api.service.world.result.GameWorldResult;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.GameStatRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareerRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousing;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousingRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleState;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleType;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventPresentationType;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventTriggerType;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEvent;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEventRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.domain.world.news.NewsMaster;
import io.ssafy.p.j14c103.homerun.domain.world.news.NewsMasterRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GameWorldResultServiceTest extends IntegrationTestSupport {

    @Autowired
    private GameWorldResultService gameWorldResultService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private GameHousingRepository gameHousingRepository;

    @Autowired
    private GameStatRepository gameStatRepository;

    @Autowired
    private GameCareerRepository gameCareerRepository;

    @Autowired
    private NewsMasterRepository newsMasterRepository;

    @Autowired
    private GameEventRepository gameEventRepository;

    @AfterEach
    void tearDown() {
        gameHousingRepository.deleteAllInBatch();
        gameCareerRepository.deleteAllInBatch();
        gameStatRepository.deleteAllInBatch();
        gameSessionRepository.deleteAllInBatch();
        gameEventRepository.deleteAllInBatch();
        newsMasterRepository.deleteAllInBatch();
    }

    @DisplayName("턴 커밋용 world result는 cycle 변화에 맞는 뉴스와 이벤트 후보를 materialize한다")
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
        gameStatRepository.saveAndFlush(GameStat.create(
            Math.toIntExact(gameSession.getGameSessionId()),
            70,
            10,
            10,
            50,
            70,
            12
        ));
        gameCareerRepository.saveAndFlush(GameCareer.create(
            Math.toIntExact(gameSession.getGameSessionId()),
            JobType.STARTUP,
            "사원",
            31_000_000,
            12,
            0,
            0,
            0,
            0,
            EmploymentStatus.EMPLOYED,
            null,
            null,
            0,
            null
        ));
        newsMasterRepository.saveAndFlush(NewsMaster.createAiNews(
            "NEWS-001",
            "호황 과열 경보",
            "negative",
            "테스트 언론",
            "테스트 기사 본문",
            "BOOM_TO_CRISIS",
            "테스트 사유",
            null,
            null,
            null,
            null
        ));
        gameEventRepository.saveAndFlush(GameEvent.create(
            "PHONE",
            "EVT-WORLD-001",
            "월드 이벤트",
            EventPresentationType.PHONE,
            EventTriggerType.PROBABILITY,
            BigDecimal.ONE,
            false,
            null,
            null,
            null,
            "월드 이벤트 설명",
            true
        ));

        // when
        GameWorldResult result = gameWorldResultService.buildWorldResult(
            gameSession.getGameSessionId(),
            60
        );

        // then
        assertThat(result.getCycleResult()).isNotNull();
        assertThat(result.getCycleResult().getNextPhase()).isEqualTo(CyclePhase.CRISIS);
        assertThat(result.getCycleResult().getDescription()).isEqualTo("경기 위기");
        assertThat(result.getNewsCandidates())
            .singleElement()
            .extracting(GameWorldResult.NewsCandidate::getNewsId, GameWorldResult.NewsCandidate::getHeadline)
            .containsExactly("NEWS-001", "호황 과열 경보");
        assertThat(result.getEventCandidates())
            .singleElement()
            .extracting(
                GameWorldResult.EventCandidate::getEventCode,
                GameWorldResult.EventCandidate::getEventName
            )
            .containsExactly("EVT-WORLD-001", "월드 이벤트");
        assertThat(result.getHousingSnapshot()).isNotNull();
        assertThat(result.getHousingSnapshot().getCurrentHousingType()).isEqualTo(HousingType.STUDIO);
        assertThat(result.getHousingSnapshot().getCurrentPropertyId()).isEqualTo(201L);
        assertThat(result.getHousingSnapshot().getTargetPropertyId()).isEqualTo(101L);
        assertThat(result.getHousingSnapshot().isHasHousingLossSignal()).isFalse();
    }

    @DisplayName("자가 주택인데 현재 매물 연결이 끊기면 housing loss signal을 유지한다")
    @Test
    void buildWorldResultWithOwnedHousingLoss() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(12, CycleState.of(CyclePhase.BOOM, CycleType.CYCLE_BOOM, 1), 101L)
        );
        gameHousingRepository.saveAndFlush(GameHousing.create(
            gameSession.getGameSessionId(),
            HousingType.OWNED_APT,
            Money.zero(),
            Money.zero(),
            Money.zero(),
            null
        ));

        // when
        final GameWorldResult result = gameWorldResultService.buildWorldResult(
            gameSession.getGameSessionId(),
            60
        );

        // then
        assertThat(result.getHousingSnapshot().getCurrentHousingType()).isEqualTo(HousingType.OWNED_APT);
        assertThat(result.getHousingSnapshot().getCurrentPropertyId()).isNull();
        assertThat(result.getHousingSnapshot().isHasHousingLossSignal()).isTrue();
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
            1
        );

        // then
        assertThat(result.getCycleResult().getNextPhase()).isEqualTo(CyclePhase.BOOM);
        assertThat(result.getCycleResult().getNextType()).isEqualTo(CycleType.CYCLE_BOOM);
        assertThat(result.getCycleResult().getRemainingTurns()).isBetween(24, 48);
        assertThat(result.getNewsCandidates()).isEmpty();
        assertThat(result.getEventCandidates()).isEmpty();
        assertThat(result.getHousingSnapshot()).isNotNull();
        assertThat(result.getHousingSnapshot().getCurrentHousingType()).isNull();
        assertThat(result.getHousingSnapshot().getCurrentPropertyId()).isNull();
        assertThat(result.getHousingSnapshot().getTargetPropertyId()).isEqualTo(101L);
        assertThat(result.getHousingSnapshot().isHasHousingLossSignal()).isTrue();
    }

    @DisplayName("존재하지 않는 세션이면 world 세션 조회 에러를 던진다")
    @Test
    void buildWorldResultWithUnknownSession() {
        final Long unknownSessionId = 9999L;

        // when
        // then
        assertThatThrownBy(() -> gameWorldResultService.buildWorldResult(
            unknownSessionId,
            50
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
            50
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
