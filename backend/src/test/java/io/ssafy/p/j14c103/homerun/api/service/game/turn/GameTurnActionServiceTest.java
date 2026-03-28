package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.SessionStatus;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.ActionCatalog;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.ActionType;
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
class GameTurnActionServiceTest {

    private final ActionCatalog actionCatalog = new ActionCatalog();

    @Autowired
    private GameTurnActionService gameTurnActionService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private AvailableActionReader availableActionReader;

    @AfterEach
    void tearDown() {
        gameSessionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("본인 세션의 선택 가능 행동 조회는 카테고리별 행동 목록을 반환한다.")
    @Test
    void getAvailableActions() {
        // given
        final User user = saveUser("turn-actions-user@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), SessionStatus.IN_PROGRESS)
        );
        given(availableActionReader.read(any(GameSession.class)))
            .willReturn(List.of(
                actionCatalog.getDefinition(ActionType.HOBBY),
                actionCatalog.getDefinition(ActionType.STUDY),
                actionCatalog.getDefinition(ActionType.REST)
            ));

        // when
        final var response = gameTurnActionService.getAvailableActions(
            user.getId(),
            gameSession.getGameSessionId()
        );

        // then
        assertThat(response.getShopping()).hasSize(1);
        assertThat(response.getShopping().get(0).getActionType()).isEqualTo("HOBBY");
        assertThat(response.getShopping().get(0).getEffects().getCash()).isEqualTo(-100_000);
        assertThat(response.getActivities()).hasSize(2);
        assertThat(response.getActivities())
            .extracting(action -> action.getActionType())
            .containsExactly("STUDY", "REST");
        assertThat(response.getActivities().get(0).getIconUrl()).isEqualTo("/images/actions/study.png");
        assertThat(response.getActivities().get(0).getEffects().getKnowledge()).isEqualTo(8);
        then(availableActionReader).should().read(any(GameSession.class));
    }

    @DisplayName("다른 사용자의 세션 행동 목록 조회는 GAME_SESSION_FORBIDDEN이 발생한다.")
    @Test
    void getOtherUsersAvailableActions() {
        // given
        final User requester = saveUser("turn-actions-requester@example.com");
        final User owner = saveUser("turn-actions-owner@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(owner.getId(), SessionStatus.IN_PROGRESS)
        );

        // when & then
        assertThatThrownBy(() -> gameTurnActionService.getAvailableActions(
            requester.getId(),
            gameSession.getGameSessionId()
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_FORBIDDEN);
        then(availableActionReader).shouldHaveNoInteractions();
    }

    @DisplayName("종료된 세션의 행동 목록 조회는 GAME_SESSION_CLOSED가 발생한다.")
    @Test
    void getClosedSessionAvailableActions() {
        // given
        final User user = saveUser("turn-actions-closed@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), SessionStatus.CLEAR)
        );

        // when & then
        assertThatThrownBy(() -> gameTurnActionService.getAvailableActions(
            user.getId(),
            gameSession.getGameSessionId()
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_CLOSED);
        then(availableActionReader).shouldHaveNoInteractions();
    }

    @DisplayName("존재하지 않는 세션의 행동 목록 조회는 GAME_SESSION_NOT_FOUND가 발생한다.")
    @Test
    void getUnknownAvailableActions() {
        // given
        final User user = saveUser("turn-actions-not-found@example.com");

        // when & then
        assertThatThrownBy(() -> gameTurnActionService.getAvailableActions(user.getId(), 9999L))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_NOT_FOUND);
        then(availableActionReader).shouldHaveNoInteractions();
    }

    private User saveUser(final String email) {
        return userRepository.save(User.register(Email.of(email), "tester", "hashed-password"));
    }

    private GameSession createGameSession(final Long userId, final SessionStatus sessionStatus) {
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
            LocalDate.of(2026, 1, 1),
            CyclePhase.BOOM
        );
        if (sessionStatus != SessionStatus.IN_PROGRESS) {
            gameSession.markEnding(sessionStatus);
        }
        return gameSession;
    }
}
