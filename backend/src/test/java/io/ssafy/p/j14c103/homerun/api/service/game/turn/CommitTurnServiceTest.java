package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.ssafy.p.j14c103.homerun.api.service.game.turn.response.CommitTurnResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.GameWorldResultService;
import io.ssafy.p.j14c103.homerun.api.service.world.result.GameWorldResult;
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
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleState;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class CommitTurnServiceTest {

    @Autowired
    private CommitTurnService commitTurnService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private GameSessionTurnSlotRepository gameTurnSlotRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private TurnDraftRepository turnDraftRepository;

    @MockitoBean
    private GameWorldResultService gameWorldResultService;

    @AfterEach
    void tearDown() {
        gameTurnSlotRepository.deleteAllInBatch();
        gameSessionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("턴 커밋은 draft를 확정 슬롯으로 저장하고 세션을 다음 턴으로 전진시킨 뒤 draft를 제거한다.")
    @Test
    void commitTurn() {
        // given
        final User user = saveUser("turn-commit@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), 5)
        );
        final TurnDraft turnDraft = createTurnDraft(gameSession.getGameSessionId(), 5);
        given(turnDraftRepository.findBySessionId(gameSession.getGameSessionId()))
            .willReturn(Optional.of(turnDraft));
        given(gameWorldResultService.buildWorldResult(gameSession.getGameSessionId(), 50))
            .willReturn(GameWorldResult.of(
                GameWorldResult.CycleResult.of(
                    CyclePhase.RECOVERY,
                    CycleType.CYCLE_RATE_HIKE,
                    18,
                    "경기 회복기"
                ),
                List.of(),
                List.of(),
                GameWorldResult.HousingSnapshot.empty()
            ));

        // when
        final CommitTurnResponse response = commitTurnService.commitTurn(
            user.getId(),
            gameSession.getGameSessionId()
        );

        // then
        assertThat(response.getTurnNumber()).isEqualTo(5);
        assertThat(response.getSettlementLog()).hasSize(2);
        assertThat(response.getUpdatedAssets().getCash()).isEqualTo(2_300_000L);
        assertThat(response.getUpdatedAssets().getNetAssets()).isEqualTo(2_300_000L);
        assertThat(response.getStatChanges().getHealth()).isEqualTo(3);
        assertThat(response.getStatChanges().getFatigue()).isEqualTo(-14);
        assertThat(response.getStatChanges().getKnowledge()).isEqualTo(8);
        assertThat(response.getFlags().isHasEvent()).isFalse();

        assertThat(gameTurnSlotRepository.findAllByGameSessionIdAndTurnNumberOrderBySlotIndex(
                gameSession.getGameSessionId(),
                5
            ))
            .extracting(
                GameTurnSlot::getSlotIndex,
                GameTurnSlot::getActionType,
                GameTurnSlot::getActionCategory,
                GameTurnSlot::isForcedAction
            )
            .containsExactly(
                tuple(0, ActionType.STUDY, ActionCategory.ACTIVITY, false),
                tuple(1, ActionType.HOBBY, ActionCategory.SHOPPING, false),
                tuple(2, ActionType.SIDE_JOB, ActionCategory.ACTIVITY, false)
            );

        final GameSession updated = gameSessionRepository.findById(gameSession.getGameSessionId())
            .orElseThrow();
        assertThat(updated.getCurrentTurn()).isEqualTo(6);
        assertThat(updated.getCurrentDate()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(updated.getCyclePhase()).isEqualTo(CyclePhase.RECOVERY);
        assertThat(updated.getCycleType()).isEqualTo(CycleType.CYCLE_RATE_HIKE);
        assertThat(updated.getCycleRemainingTurns()).isEqualTo(18);
        assertThat(updated.getCashBalance()).isEqualTo(Money.of(2_300_000L));
        assertThat(updated.getTotalAssets()).isEqualTo(Money.of(2_300_000L));
        assertThat(updated.getNetWorth()).isEqualTo(Money.of(2_300_000L));
        assertThat(updated.getSessionStatus()).isEqualTo(SessionStatus.IN_PROGRESS);

        then(gameWorldResultService).should().buildWorldResult(gameSession.getGameSessionId(), 50);
        then(turnDraftRepository).should().deleteBySessionId(gameSession.getGameSessionId());
    }

    private User saveUser(final String email) {
        return userRepository.save(User.register(Email.of(email), "tester", "hashed-password"));
    }

    private GameSession createGameSession(final Long userId, final Integer currentTurn) {
        final CycleState currentCycleState = CycleState.of(
            CyclePhase.BOOM,
            CycleType.CYCLE_BOOM,
            24
        );
        final GameSession gameSession = GameSession.create(
            userId,
            1,
            "커밋 세션",
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
            Money.of(2_000_000L),
            LocalDate.of(2026, 1, 1),
            currentCycleState
        );
        gameSession.advanceTurn(
            currentTurn,
            LocalDate.of(2026, 1, 1),
            Money.of(2_000_000L),
            Money.of(2_000_000L),
            Money.of(2_000_000L),
            currentCycleState
        );
        return gameSession;
    }

    private TurnDraft createTurnDraft(final Long sessionId, final Integer turnNumber) {
        return TurnDraft.of(
            sessionId,
            turnNumber,
            List.of(
                TurnDraftSlot.of(0, ActionType.STUDY),
                TurnDraftSlot.of(1, ActionType.HOBBY),
                TurnDraftSlot.of(2, ActionType.SIDE_JOB)
            ),
            Money.of(300_000L),
            Map.of(
                "health", 3,
                "fatigue", -14,
                "stress", -8,
                "happiness", 4,
                "knowledge", 8
            )
        );
    }
}
