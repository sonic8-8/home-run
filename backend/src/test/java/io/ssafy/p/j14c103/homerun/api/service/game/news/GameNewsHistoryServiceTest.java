package io.ssafy.p.j14c103.homerun.api.service.game.news;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.ssafy.p.j14c103.homerun.api.service.game.news.response.GameNewsHistoryResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.ending.WorldEndingHistoryProviderService;
import io.ssafy.p.j14c103.homerun.api.service.world.ending.response.WorldEndingHistoryProviderResponse;
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
class GameNewsHistoryServiceTest {

    @Autowired
    private GameNewsHistoryService gameNewsHistoryService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private WorldEndingHistoryProviderService worldEndingHistoryProviderService;

    @AfterEach
    void tearDown() {
        gameSessionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("본인 세션의 뉴스 히스토리 조회는 world provider 응답을 공개 계약으로 변환한다.")
    @Test
    void getNewsHistory() {
        // given
        final User user = saveUser("news-history-user@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 12, LocalDate.of(2026, 1, 1), CyclePhase.BOOM)
        );
        given(worldEndingHistoryProviderService.getEndingHistory(gameSession.getGameSessionId()))
            .willReturn(WorldEndingHistoryProviderResponse.of(
                List.of(
                    WorldEndingHistoryProviderResponse.NewsHistoryItem.of(
                        12,
                        "NEWS-012",
                        "채용 한파 심화",
                        LocalDate.of(2026, 1, 1)
                    ),
                    WorldEndingHistoryProviderResponse.NewsHistoryItem.of(
                        11,
                        "NEWS-011",
                        "기준금리 동결",
                        LocalDate.of(2025, 12, 1)
                    )
                ),
                List.of(),
                List.of(),
                WorldEndingHistoryProviderResponse.HousingSnapshotItem.of(null, null, 101L)
            ));

        // when
        final GameNewsHistoryResponse response = gameNewsHistoryService.getNewsHistory(
            user.getId(),
            gameSession.getGameSessionId()
        );

        // then
        assertThat(response.getNewsHistories()).hasSize(2);
        assertThat(response.getNewsHistories())
            .extracting(GameNewsHistoryResponse.NewsHistoryResponse::getNewsId)
            .containsExactly("NEWS-012", "NEWS-011");
        assertThat(response.getNewsHistories())
            .extracting(GameNewsHistoryResponse.NewsHistoryResponse::getTurnNumber)
            .containsExactly(12, 11);
        then(worldEndingHistoryProviderService).should().getEndingHistory(gameSession.getGameSessionId());
    }

    @DisplayName("다른 사용자의 세션 뉴스 히스토리 조회는 GAME_SESSION_FORBIDDEN이 발생한다.")
    @Test
    void getOtherUsersNewsHistory() {
        // given
        final User requester = saveUser("news-history-requester@example.com");
        final User owner = saveUser("news-history-owner@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(owner.getId(), 12, LocalDate.of(2026, 1, 1), CyclePhase.BOOM)
        );

        // when & then
        assertThatThrownBy(() -> gameNewsHistoryService.getNewsHistory(
            requester.getId(),
            gameSession.getGameSessionId()
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_FORBIDDEN);
        then(worldEndingHistoryProviderService).shouldHaveNoInteractions();
    }

    @DisplayName("존재하지 않는 세션의 뉴스 히스토리 조회는 GAME_SESSION_NOT_FOUND가 발생한다.")
    @Test
    void getUnknownNewsHistory() {
        // given
        final User requester = saveUser("news-history-not-found@example.com");

        // when & then
        assertThatThrownBy(() -> gameNewsHistoryService.getNewsHistory(requester.getId(), 9999L))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_NOT_FOUND);
        then(worldEndingHistoryProviderService).shouldHaveNoInteractions();
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
