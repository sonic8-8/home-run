package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.ssafy.p.j14c103.homerun.api.service.world.LatestTurnNewsService;
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
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class GameTurnNewsServiceTest extends IntegrationTestSupport {

    @Autowired
    private GameTurnNewsService gameTurnNewsService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private LatestTurnNewsService latestTurnNewsService;

    @AfterEach
    void tearDown() {
        gameSessionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("본인 세션의 최신 턴 뉴스 조회는 world provider 응답을 반환한다.")
    @Test
    void getLatestTurnNews() {
        // given
        final User user = saveUser("turn-news-user@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 12, LocalDate.of(2026, 1, 1), CyclePhase.BOOM)
        );
        final LatestTurnNewsResponse response = LatestTurnNewsResponse.of(
            12,
            LocalDate.of(2026, 1, 1),
            List.of(LatestTurnNewsResponse.NewsItemResponse.of(
                "01500801.20200519071906001",
                "부동산 시장 과열 경고",
                "시장 과열 신호가 확인됐다.",
                "영남일보",
                LocalDate.of(2026, 1, 1),
                "BOOM_TO_CRISIS"
            ))
        );
        given(latestTurnNewsService.getLatestTurnNews(gameSession.getGameSessionId()))
            .willReturn(response);

        // when
        final LatestTurnNewsResponse result = gameTurnNewsService.getLatestTurnNews(
            user.getId(),
            gameSession.getGameSessionId()
        );

        // then
        assertThat(result.getTurnNumber()).isEqualTo(12);
        assertThat(result.getCurrentDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(result.getNews()).hasSize(1);
        then(latestTurnNewsService).should().getLatestTurnNews(gameSession.getGameSessionId());
    }

    @DisplayName("다른 사용자의 세션 최신 턴 뉴스 조회는 GAME_SESSION_FORBIDDEN이 발생한다.")
    @Test
    void getOtherUsersLatestTurnNews() {
        // given
        final User requester = saveUser("turn-news-requester@example.com");
        final User owner = saveUser("turn-news-owner@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(owner.getId(), 12, LocalDate.of(2026, 1, 1), CyclePhase.BOOM)
        );

        // when & then
        assertThatThrownBy(() -> gameTurnNewsService.getLatestTurnNews(
            requester.getId(),
            gameSession.getGameSessionId()
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_FORBIDDEN);
        then(latestTurnNewsService).shouldHaveNoInteractions();
    }

    @DisplayName("존재하지 않는 세션의 최신 턴 뉴스 조회는 GAME_SESSION_NOT_FOUND가 발생한다.")
    @Test
    void getUnknownLatestTurnNews() {
        // given
        final User requester = saveUser("turn-news-not-found@example.com");

        // when & then
        assertThatThrownBy(() -> gameTurnNewsService.getLatestTurnNews(requester.getId(), 9999L))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_NOT_FOUND);
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
