package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.ssafy.p.j14c103.homerun.api.service.game.turn.response.TurnStateResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.LatestTurnNewsService;
import io.ssafy.p.j14c103.homerun.api.service.world.GameTurnWorldStateService;
import io.ssafy.p.j14c103.homerun.api.service.world.response.GameTurnWorldStateResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.response.LatestTurnNewsResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.user.Email;
import io.ssafy.p.j14c103.homerun.domain.user.User;
import io.ssafy.p.j14c103.homerun.domain.user.UserRepository;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class GameTurnStateServiceTest {

    @Autowired
    private GameTurnStateService gameTurnStateService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private GameTurnWorldStateService gameTurnWorldStateService;

    @MockitoBean
    private LatestTurnNewsService latestTurnNewsService;

    @AfterEach
    void tearDown() {
        gameSessionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("본인 세션의 현재 턴 상태 조회는 세션과 월드 fragment를 조합한다.")
    @Test
    void getTurnState() {
        // given
        final User user = saveUser("turn-state-user@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 12, LocalDate.of(2026, 1, 1), CyclePhase.BOOM)
        );
        given(gameTurnWorldStateService.getWorldState(any(GameSession.class)))
            .willReturn(GameTurnWorldStateResponse.of(CyclePhase.BOOM, "경기 호황기"));
        given(latestTurnNewsService.getLatestTurnNews(gameSession.getGameSessionId()))
            .willReturn(LatestTurnNewsResponse.of(
                12,
                LocalDate.of(2026, 1, 1),
                java.util.List.of(
                    LatestTurnNewsResponse.NewsItemResponse.of(
                        "01500801.20200519071906001",
                        "부동산 시장 과열 경고",
                        "시장 과열 신호가 확인됐다.",
                        "영남일보",
                        LocalDate.of(2026, 1, 1),
                        "BOOM_TO_CRISIS"
                    )
                )
            ));

        // when
        final TurnStateResponse response = gameTurnStateService.getTurnState(
            user.getId(),
            gameSession.getGameSessionId()
        );

        // then
        assertThat(response.getTurnNumber()).isEqualTo(12);
        assertThat(response.getCurrentDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(response.getMonth()).isEqualTo(1);
        assertThat(response.getEconomicCycle().getPhase()).isEqualTo(CyclePhase.BOOM);
        assertThat(response.getEconomicCycle().getDescription()).isEqualTo("경기 호황기");
        assertThat(response.getNews()).hasSize(1);
        assertThat(response.getNews().get(0).getNewsId()).isEqualTo("01500801.20200519071906001");
        assertThat(response.getNews().get(0).getHeadline()).isEqualTo("부동산 시장 과열 경고");
        assertThat(response.getNews().get(0).getContent()).isEqualTo("시장 과열 신호가 확인됐다.");
        assertThat(response.getNews().get(0).getSourceName()).isEqualTo("영남일보");
        assertThat(response.getNews().get(0).getPublishedDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(response.getNews().get(0).getEconomicCycleType()).isEqualTo("BOOM_TO_CRISIS");
        then(gameTurnWorldStateService).should().getWorldState(any(GameSession.class));
        then(latestTurnNewsService).should().getLatestTurnNews(gameSession.getGameSessionId());
    }

    @DisplayName("현재 턴 뉴스가 없으면 빈 뉴스 목록을 반환한다.")
    @Test
    void getTurnStateWithoutNews() {
        // given
        final User user = saveUser("turn-state-empty-news@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 12, LocalDate.of(2026, 1, 1), CyclePhase.BOOM)
        );
        given(gameTurnWorldStateService.getWorldState(any(GameSession.class)))
            .willReturn(GameTurnWorldStateResponse.of(CyclePhase.BOOM, "경기 호황기"));
        given(latestTurnNewsService.getLatestTurnNews(gameSession.getGameSessionId()))
            .willReturn(LatestTurnNewsResponse.of(
                12,
                LocalDate.of(2026, 1, 1),
                java.util.List.of()
            ));

        // when
        final TurnStateResponse response = gameTurnStateService.getTurnState(
            user.getId(),
            gameSession.getGameSessionId()
        );

        // then
        assertThat(response.getNews()).isEmpty();
        then(latestTurnNewsService).should().getLatestTurnNews(gameSession.getGameSessionId());
    }

    @DisplayName("다른 사용자의 세션 턴 상태 조회는 GAME_SESSION_FORBIDDEN이 발생한다.")
    @Test
    void getOtherUsersTurnState() {
        // given
        final User requester = saveUser("turn-state-requester@example.com");
        final User owner = saveUser("turn-state-owner@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(owner.getId(), 12, LocalDate.of(2026, 1, 1), CyclePhase.BOOM)
        );

        // when & then
        assertThatThrownBy(() -> gameTurnStateService.getTurnState(
            requester.getId(),
            gameSession.getGameSessionId()
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_FORBIDDEN);
        then(gameTurnWorldStateService).shouldHaveNoInteractions();
        then(latestTurnNewsService).shouldHaveNoInteractions();
    }

    @DisplayName("존재하지 않는 세션의 턴 상태 조회는 GAME_SESSION_NOT_FOUND가 발생한다.")
    @Test
    void getUnknownTurnState() {
        // given
        final User requester = saveUser("turn-state-not-found@example.com");

        // when & then
        assertThatThrownBy(() -> gameTurnStateService.getTurnState(requester.getId(), 9999L))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_NOT_FOUND);
        then(gameTurnWorldStateService).shouldHaveNoInteractions();
        then(latestTurnNewsService).shouldHaveNoInteractions();
    }

    private User saveUser(final String email) {
        return userRepository.save(User.register(Email.of(email), "tester", "hashed-password"));
    }

    private GameSession createGameSession(
        final Long userId,
        final Integer currentTurn,
        final LocalDate currentDate,
        final CyclePhase cyclePhase
    ) {
        final GameSession gameSession = GameSession.create(
            userId,
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
}
