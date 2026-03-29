package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.SessionStatus;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.ActionCategory;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.ActionType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.GameSessionTurnSlotRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.GameTurnSlot;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.TurnDraft;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.TurnDraftRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.TurnDraftSlot;
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
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class GameTurnCommitGuardServiceTest extends IntegrationTestSupport {

    @Autowired
    private GameTurnCommitGuardService gameTurnCommitGuardService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private GameSessionTurnSlotRepository gameSessionTurnSlotRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private TurnDraftRepository turnDraftRepository;

    @AfterEach
    void tearDown() {
        gameSessionTurnSlotRepository.deleteAllInBatch();
        gameSessionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("커밋 가드는 row lock으로 세션과 draft를 확인한 뒤 커밋 가능한 문맥을 반환한다.")
    @Test
    void guard() {
        // given
        final User user = saveUser("turn-commit-guard@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), SessionStatus.IN_PROGRESS, 5)
        );
        final TurnDraft turnDraft = createTurnDraft(gameSession.getGameSessionId(), 5);
        given(turnDraftRepository.findBySessionId(gameSession.getGameSessionId()))
            .willReturn(Optional.of(turnDraft));

        // when
        final GameTurnCommitGuardService.CommitTurnGuardResult result =
            gameTurnCommitGuardService.guard(user.getId(), gameSession.getGameSessionId());

        // then
        assertThat(result.getGameSession().getGameSessionId()).isEqualTo(gameSession.getGameSessionId());
        assertThat(result.getTurnDraft().getTurnNumber()).isEqualTo(5);
    }

    @DisplayName("종료된 세션 커밋 시도는 GAME_SESSION_CLOSED가 발생한다.")
    @Test
    void guardClosedSession() {
        // given
        final User user = saveUser("turn-commit-closed@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), SessionStatus.CLEAR, 5)
        );

        // when & then
        assertThatThrownBy(() -> gameTurnCommitGuardService.guard(
            user.getId(),
            gameSession.getGameSessionId()
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_CLOSED);
        then(turnDraftRepository).shouldHaveNoInteractions();
    }

    @DisplayName("현재 턴에 이미 확정 슬롯이 있으면 GAME_TURN_ALREADY_COMMITTED가 발생한다.")
    @Test
    void guardAlreadyCommittedTurn() {
        // given
        final User user = saveUser("turn-commit-duplicate@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), SessionStatus.IN_PROGRESS, 5)
        );
        gameSessionTurnSlotRepository.saveAndFlush(GameTurnSlot.create(
            gameSession.getGameSessionId(),
            5,
            0,
            ActionType.STUDY,
            ActionCategory.ACTIVITY,
            false
        ));

        // when & then
        assertThatThrownBy(() -> gameTurnCommitGuardService.guard(
            user.getId(),
            gameSession.getGameSessionId()
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_TURN_ALREADY_COMMITTED);
        then(turnDraftRepository).shouldHaveNoInteractions();
    }

    @DisplayName("커밋할 draft가 없으면 GAME_TURN_DRAFT_NOT_FOUND가 발생한다.")
    @Test
    void guardWithoutDraft() {
        // given
        final User user = saveUser("turn-commit-no-draft@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), SessionStatus.IN_PROGRESS, 5)
        );
        given(turnDraftRepository.findBySessionId(gameSession.getGameSessionId()))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> gameTurnCommitGuardService.guard(
            user.getId(),
            gameSession.getGameSessionId()
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_TURN_DRAFT_NOT_FOUND);
    }

    @DisplayName("세션 현재 턴과 draft 턴이 다르면 GAME_TURN_DRAFT_MISMATCH가 발생한다.")
    @Test
    void guardTurnMismatch() {
        // given
        final User user = saveUser("turn-commit-mismatch@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), SessionStatus.IN_PROGRESS, 5)
        );
        given(turnDraftRepository.findBySessionId(gameSession.getGameSessionId()))
            .willReturn(Optional.of(createTurnDraft(gameSession.getGameSessionId(), 4)));

        // when & then
        assertThatThrownBy(() -> gameTurnCommitGuardService.guard(
            user.getId(),
            gameSession.getGameSessionId()
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_TURN_DRAFT_MISMATCH);
    }

    @DisplayName("존재하지 않는 사용자로 커밋 시도는 USER_NOT_FOUND가 발생한다.")
    @Test
    void guardUnknownUser() {
        // when & then
        assertThatThrownBy(() -> gameTurnCommitGuardService.guard(9999L, 1L))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.USER_NOT_FOUND);
        then(turnDraftRepository).shouldHaveNoInteractions();
    }

    @DisplayName("존재하지 않는 세션 커밋 시도는 GAME_SESSION_NOT_FOUND가 발생한다.")
    @Test
    void guardUnknownSession() {
        // given
        final User user = saveUser("turn-commit-not-found@example.com");

        // when & then
        assertThatThrownBy(() -> gameTurnCommitGuardService.guard(user.getId(), 9999L))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_NOT_FOUND);
        then(turnDraftRepository).shouldHaveNoInteractions();
    }

    @DisplayName("다른 사용자의 세션 커밋 시도는 GAME_SESSION_FORBIDDEN이 발생한다.")
    @Test
    void guardOtherUsersSession() {
        // given
        final User requester = saveUser("turn-commit-requester@example.com");
        final User owner = saveUser("turn-commit-owner@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(owner.getId(), SessionStatus.IN_PROGRESS, 5)
        );

        // when & then
        assertThatThrownBy(() -> gameTurnCommitGuardService.guard(
            requester.getId(),
            gameSession.getGameSessionId()
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_FORBIDDEN);
        then(turnDraftRepository).shouldHaveNoInteractions();
    }

    private User saveUser(final String email) {
        return userRepository.save(User.register(Email.of(email), "tester", "hashed-password"));
    }

    private TurnDraft createTurnDraft(final Long sessionId, final Integer turnNumber) {
        return TurnDraft.of(
            sessionId,
            turnNumber,
            List.of(
                TurnDraftSlot.of(0, ActionType.STUDY),
                TurnDraftSlot.of(1, ActionType.REST),
                TurnDraftSlot.of(2, ActionType.SIDE_JOB)
            ),
            Money.of(300_000L),
            Map.of(
                "health", 3,
                "fatigue", -10,
                "stress", -8,
                "happiness", 4,
                "knowledge", 8
            )
        );
    }

    private GameSession createGameSession(
        final Long userId,
        final SessionStatus sessionStatus,
        final Integer currentTurn
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
            LocalDate.of(2026, 1, 1),
            CyclePhase.BOOM
        );
        gameSession.advanceTurn(
            currentTurn,
            LocalDate.of(2026, 1, 1),
            Money.of(2_000_000L),
            Money.of(2_000_000L),
            CyclePhase.BOOM
        );
        if (sessionStatus != SessionStatus.IN_PROGRESS) {
            gameSession.markEnding(sessionStatus);
        }
        return gameSession;
    }
}
