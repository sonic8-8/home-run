package io.ssafy.p.j14c103.homerun.api.service.world;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.ssafy.p.j14c103.homerun.api.service.world.response.LatestTurnNewsResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.history.news.GameNewsLog;
import io.ssafy.p.j14c103.homerun.domain.history.news.GameNewsLogRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.domain.world.news.NewsMaster;
import io.ssafy.p.j14c103.homerun.domain.world.news.NewsMasterRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class LatestTurnNewsServiceTest {

    @Autowired
    private LatestTurnNewsService latestTurnNewsService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private GameNewsLogRepository gameNewsLogRepository;

    @Autowired
    private NewsMasterRepository newsMasterRepository;

    @MockitoBean
    private LatestTurnNewsPhaseService latestTurnNewsPhaseService;

    @AfterEach
    void tearDown() {
        gameNewsLogRepository.deleteAllInBatch();
        newsMasterRepository.deleteAllInBatch();
        gameSessionRepository.deleteAllInBatch();
    }

    @DisplayName("같은 턴에 저장된 뉴스 로그가 있으면 저장된 같은 뉴스를 반환한다")
    @Test
    void getLatestTurnNewsWithSavedLog() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(12, LocalDate.of(2026, 1, 1), CyclePhase.BOOM)
        );
        final NewsMaster savedNews = newsMasterRepository.saveAndFlush(
            createNewsMaster("NEWS-001", "BOOM_TO_CRISIS", "저장된 뉴스")
        );
        gameNewsLogRepository.saveAndFlush(
            GameNewsLog.create(
                gameSession.getGameSessionId(),
                12,
                savedNews.getNewsId(),
                savedNews.getTitle(),
                LocalDate.of(2026, 1, 1)
            )
        );

        // when
        final LatestTurnNewsResponse response = latestTurnNewsService.getLatestTurnNews(
            gameSession.getGameSessionId()
        );

        // then
        assertThat(response.getTurnNumber()).isEqualTo(12);
        assertThat(response.getCurrentDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(response.getNews()).hasSize(1);
        assertThat(response.getNews().get(0).getNewsId()).isEqualTo("NEWS-001");
        assertThat(response.getNews().get(0).getHeadline()).isEqualTo("저장된 뉴스");
        assertThat(response.getNews().get(0).getEconomicCycleType()).isEqualTo("BOOM_TO_CRISIS");
        then(latestTurnNewsPhaseService).shouldHaveNoInteractions();
    }

    @DisplayName("같은 턴 저장 뉴스가 없으면 phase를 계산해 뉴스를 고르고 로그를 저장한다")
    @Test
    void getLatestTurnNewsWithoutSavedLog() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(12, LocalDate.of(2026, 1, 1), CyclePhase.BOOM)
        );
        newsMasterRepository.saveAndFlush(createNewsMaster("NEWS-001", "BOOM_TO_CRISIS", "과열 경고"));
        given(latestTurnNewsPhaseService.resolve(CyclePhase.BOOM))
            .willReturn(LatestTurnNewsPhaseService.PhaseResolution.of(
                CyclePhase.BOOM,
                CyclePhase.CRISIS,
                "BOOM_TO_CRISIS"
            ));

        // when
        final LatestTurnNewsResponse response = latestTurnNewsService.getLatestTurnNews(
            gameSession.getGameSessionId()
        );

        // then
        assertThat(response.getTurnNumber()).isEqualTo(12);
        assertThat(response.getNews()).hasSize(1);
        assertThat(response.getNews().get(0).getNewsId()).isEqualTo("NEWS-001");
        assertThat(gameNewsLogRepository
            .findAllByGameSessionIdAndTurnNumberOrderByGameNewsLogIdAsc(
                gameSession.getGameSessionId(),
                12
            ))
            .hasSize(1)
            .first()
            .extracting(GameNewsLog::getNewsId, GameNewsLog::getPublishedDate)
            .containsExactly("NEWS-001", LocalDate.of(2026, 1, 1));
    }

    @DisplayName("세션의 경제 사이클 값이 없고 저장된 뉴스도 없으면 world cycle 상태 에러를 던진다")
    @Test
    void getLatestTurnNewsWithInvalidCycleState() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(12, LocalDate.of(2026, 1, 1), null)
        );
        given(latestTurnNewsPhaseService.resolve(null))
            .willThrow(new HomerunException(ErrorCode.WORLD_CYCLE_STATE_INVALID));

        // when
        // then
        assertThatThrownBy(() -> latestTurnNewsService.getLatestTurnNews(gameSession.getGameSessionId()))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.WORLD_CYCLE_STATE_INVALID);
    }

    @DisplayName("phase에 맞는 뉴스 후보가 없으면 서버 설정 에러를 던진다")
    @Test
    void getLatestTurnNewsWithoutMatchingNewsCandidate() {
        // given
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(12, LocalDate.of(2026, 1, 1), CyclePhase.BOOM)
        );
        given(latestTurnNewsPhaseService.resolve(CyclePhase.BOOM))
            .willReturn(LatestTurnNewsPhaseService.PhaseResolution.of(
                CyclePhase.BOOM,
                CyclePhase.CRISIS,
                "BOOM_TO_CRISIS"
            ));

        // when
        // then
        assertThatThrownBy(() -> latestTurnNewsService.getLatestTurnNews(gameSession.getGameSessionId()))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private GameSession createGameSession(
        final int currentTurn,
        final LocalDate currentDate,
        final CyclePhase cyclePhase
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
            101L,
            DataSourceType.PROFILE
        );
        gameSession.initializeCapital(
            Money.of(2_000_000L),
            Money.of(2_000_000L),
            currentDate,
            cyclePhase
        );
        gameSession.advanceTurn(
            currentTurn,
            currentDate,
            Money.of(2_000_000L),
            Money.of(2_000_000L),
            cyclePhase
        );
        return gameSession;
    }

    private NewsMaster createNewsMaster(
        final String newsId,
        final String economicCycleType,
        final String title
    ) {
        return NewsMaster.createAiNews(
            newsId,
            title,
            "negative",
            "테스트 언론",
            "테스트 기사 본문",
            economicCycleType,
            "테스트 사유",
            null,
            null,
            null,
            null
        );
    }
}
