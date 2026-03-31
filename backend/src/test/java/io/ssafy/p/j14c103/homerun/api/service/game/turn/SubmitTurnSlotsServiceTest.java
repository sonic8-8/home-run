package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import io.ssafy.p.j14c103.homerun.api.service.game.port.TurnPreviewCalculator;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.request.SubmitTurnSlotsServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.response.TurnPreviewResponse;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.SessionStatus;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.ActionType;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class SubmitTurnSlotsServiceTest extends IntegrationTestSupport {

    @Autowired
    private SubmitTurnSlotsService submitTurnSlotsService;

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private TurnPreviewCalculator turnPreviewCalculator;

    @MockitoBean
    private TurnDraftRepository turnDraftRepository;

    @AfterEach
    void tearDown() {
        gameSessionRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("턴 슬롯 제출은 preview를 계산하고 draft를 전체 덮어써서 저장한다.")
    @Test
    void submitTurnSlots() {
        // given
        final User user = saveUser("turn-submit-user@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), SessionStatus.IN_PROGRESS, 12)
        );
        final SubmitTurnSlotsServiceRequest request = SubmitTurnSlotsServiceRequest.of(
            List.of(
                SubmitTurnSlotsServiceRequest.TurnSlotRequest.of(0, ActionType.STUDY),
                SubmitTurnSlotsServiceRequest.TurnSlotRequest.of(1, ActionType.REST),
                SubmitTurnSlotsServiceRequest.TurnSlotRequest.of(2, ActionType.SIDE_JOB)
            )
        );
        given(turnPreviewCalculator.calculate(any(GameSession.class), any(SubmitTurnSlotsServiceRequest.class)))
            .willReturn(TurnPreviewCalculator.TurnPreviewResult.of(
                List.of(
                    TurnPreviewCalculator.PreviewSlot.of(0, ActionType.STUDY, false),
                    TurnPreviewCalculator.PreviewSlot.of(1, ActionType.REST, false),
                    TurnPreviewCalculator.PreviewSlot.of(2, ActionType.SIDE_JOB, false)
                ),
                Money.of(430_000L),
                Map.of(
                    "health", 3,
                    "fatigue", -14,
                    "stress", -8,
                    "happiness", 4,
                    "knowledge", 8
                )
            ));

        // when
        final TurnPreviewResponse response = submitTurnSlotsService.submitTurnSlots(
            user.getId(),
            gameSession.getGameSessionId(),
            request
        );

        // then
        assertThat(response.getSlots()).hasSize(3);
        assertThat(response.getPreviewCashChange()).isEqualTo(430_000L);
        assertThat(response.getPreviewCashMinChange()).isEqualTo(430_000L);
        assertThat(response.getPreviewCashMaxChange()).isEqualTo(430_000L);
        assertThat(response.getPreviewStatChanges().getKnowledge()).isEqualTo(8);
        final ArgumentCaptor<TurnDraft> turnDraftCaptor = ArgumentCaptor.forClass(TurnDraft.class);
        then(turnDraftRepository).should().save(turnDraftCaptor.capture());
        final TurnDraft savedDraft = turnDraftCaptor.getValue();
        assertThat(savedDraft.getSessionId()).isEqualTo(gameSession.getGameSessionId());
        assertThat(savedDraft.getTurnNumber()).isEqualTo(12);
        assertThat(savedDraft.getSlots())
            .extracting(TurnDraftSlot::getSlotIndex, TurnDraftSlot::getActionType)
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple(0, ActionType.STUDY),
                org.assertj.core.groups.Tuple.tuple(1, ActionType.REST),
                org.assertj.core.groups.Tuple.tuple(2, ActionType.SIDE_JOB)
            );
        assertThat(savedDraft.getPreviewCashChange()).isEqualTo(Money.of(430_000L));
        assertThat(savedDraft.getPreviewStatChanges()).containsEntry("stress", -8);
    }

    @DisplayName("슬롯이 3개보다 적으면 INVALID_INPUT_VALUE가 발생한다.")
    @Test
    void submitTurnSlotsWithTooFewSlots() {
        // when & then
        assertThatThrownBy(() -> SubmitTurnSlotsServiceRequest.of(
            List.of(
                SubmitTurnSlotsServiceRequest.TurnSlotRequest.of(0, ActionType.STUDY),
                SubmitTurnSlotsServiceRequest.TurnSlotRequest.of(1, ActionType.REST)
            )
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @DisplayName("잘못된 슬롯 인덱스가 있으면 INVALID_INPUT_VALUE가 발생한다.")
    @Test
    void submitTurnSlotsWithInvalidSlotIndex() {
        // when & then
        assertThatThrownBy(() -> SubmitTurnSlotsServiceRequest.of(
            List.of(
                SubmitTurnSlotsServiceRequest.TurnSlotRequest.of(0, ActionType.STUDY),
                SubmitTurnSlotsServiceRequest.TurnSlotRequest.of(1, ActionType.REST),
                SubmitTurnSlotsServiceRequest.TurnSlotRequest.of(3, ActionType.SIDE_JOB)
            )
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @DisplayName("종료된 세션의 턴 슬롯 제출은 GAME_SESSION_CLOSED가 발생한다.")
    @Test
    void submitTurnSlotsToClosedSession() {
        // given
        final User user = saveUser("turn-submit-closed@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), SessionStatus.CLEAR, 12)
        );
        final SubmitTurnSlotsServiceRequest request = validRequest();

        // when & then
        assertThatThrownBy(() -> submitTurnSlotsService.submitTurnSlots(
            user.getId(),
            gameSession.getGameSessionId(),
            request
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GAME_SESSION_CLOSED);
        then(turnPreviewCalculator).shouldHaveNoInteractions();
        then(turnDraftRepository).shouldHaveNoInteractions();
    }

    @DisplayName("draft 저장 중 예외가 나면 호출자에게 그대로 전달된다.")
    @Test
    void submitTurnSlotsWhenDraftSaveFails() {
        // given
        final User user = saveUser("turn-submit-draft-failure@example.com");
        final GameSession gameSession = gameSessionRepository.saveAndFlush(
            createGameSession(user.getId(), SessionStatus.IN_PROGRESS, 12)
        );
        final SubmitTurnSlotsServiceRequest request = validRequest();
        given(turnPreviewCalculator.calculate(any(GameSession.class), any(SubmitTurnSlotsServiceRequest.class)))
            .willReturn(TurnPreviewCalculator.TurnPreviewResult.of(
                List.of(
                    TurnPreviewCalculator.PreviewSlot.of(0, ActionType.STUDY, false),
                    TurnPreviewCalculator.PreviewSlot.of(1, ActionType.REST, false),
                    TurnPreviewCalculator.PreviewSlot.of(2, ActionType.SIDE_JOB, false)
                ),
                Money.of(430_000L),
                Map.of(
                    "health", 3,
                    "fatigue", -14,
                    "stress", -8,
                    "happiness", 4,
                    "knowledge", 8
                )
            ));
        willThrow(new HomerunException(ErrorCode.GLOBAL_SERIALIZATION_ERROR))
            .given(turnDraftRepository)
            .save(any(TurnDraft.class));

        // when & then
        assertThatThrownBy(() -> submitTurnSlotsService.submitTurnSlots(
            user.getId(),
            gameSession.getGameSessionId(),
            request
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.GLOBAL_SERIALIZATION_ERROR);
    }

    private SubmitTurnSlotsServiceRequest validRequest() {
        return SubmitTurnSlotsServiceRequest.of(
            List.of(
                SubmitTurnSlotsServiceRequest.TurnSlotRequest.of(0, ActionType.STUDY),
                SubmitTurnSlotsServiceRequest.TurnSlotRequest.of(1, ActionType.REST),
                SubmitTurnSlotsServiceRequest.TurnSlotRequest.of(2, ActionType.SIDE_JOB)
            )
        );
    }

    private User saveUser(final String email) {
        return userRepository.save(User.register(Email.of(email), "tester", "hashed-password"));
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
            io.ssafy.p.j14c103.homerun.domain.character.career.JobType.STARTUP,
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
