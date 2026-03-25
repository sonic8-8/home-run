package io.ssafy.p.j14c103.homerun.domain.gamesession.turn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ActionResolutionPolicyTest {

    private final ActionCatalog actionCatalog = new ActionCatalog();
    private final ActionResolutionPolicy actionResolutionPolicy = new ActionResolutionPolicy(
        actionCatalog,
        new SideJobIncomePolicy()
    );

    @DisplayName("기본 상태에서는 모든 행동이 선택 가능하다.")
    @Test
    void resolvePreviewActions() {
        // given
        final GameStat gameStat = createGameStat(60, 30, 25, 55, 50, false, null);

        // when
        final List<ActionResolutionPolicy.ResolvedAction> result =
            actionResolutionPolicy.resolvePreviewActions(gameStat, 12);

        // then
        assertThat(result)
            .extracting(
                ActionResolutionPolicy.ResolvedAction::actionType,
                ActionResolutionPolicy.ResolvedAction::disabled,
                ActionResolutionPolicy.ResolvedAction::forced
            )
            .containsExactly(
                tuple(ActionType.STUDY, false, false),
                tuple(ActionType.EXERCISE, false, false),
                tuple(ActionType.REST, false, false),
                tuple(ActionType.HOBBY, false, false),
                tuple(ActionType.MEET_FRIEND, false, false),
                tuple(ActionType.NETWORKING, false, false),
                tuple(ActionType.SIDE_JOB, false, false)
            );
    }

    @DisplayName("번아웃 상태에서는 REST만 강제 행동으로 표시된다.")
    @Test
    void resolvePreviewActionsWithBurnout() {
        // given
        final GameStat gameStat = createGameStat(70, 85, 82, 50, 50, true, null);

        // when
        final List<ActionResolutionPolicy.ResolvedAction> result =
            actionResolutionPolicy.resolvePreviewActions(gameStat, 12);

        // then
        assertThat(findAction(result, ActionType.REST).forced()).isTrue();
        assertThat(findAction(result, ActionType.REST).disabled()).isFalse();
        assertThat(findAction(result, ActionType.STUDY).forced()).isFalse();
        assertThat(findAction(result, ActionType.STUDY).disabled()).isFalse();
    }

    @DisplayName("입원 상태에서는 REST만 강제 행동이고 나머지는 비활성화된다.")
    @Test
    void resolvePreviewActionsWithHospitalization() {
        // given
        final GameStat gameStat = createGameStat(40, 30, 20, 55, 45, false, 12);

        // when
        final List<ActionResolutionPolicy.ResolvedAction> result =
            actionResolutionPolicy.resolvePreviewActions(gameStat, 12);

        // then
        assertThat(findAction(result, ActionType.REST).forced()).isTrue();
        assertThat(findAction(result, ActionType.REST).disabled()).isFalse();
        assertThat(findAction(result, ActionType.STUDY).disabled()).isTrue();
        assertThat(findAction(result, ActionType.SIDE_JOB).disabled()).isTrue();
    }

    @DisplayName("저지식 부업 preview는 범위 cash preview를 사용한다.")
    @Test
    void resolvePreviewActionForLowKnowledgeSideJob() {
        // given
        final GameStat gameStat = createGameStat(60, 30, 20, 55, 10, false, null);

        // when
        final ActionResolutionPolicy.ResolvedAction result = actionResolutionPolicy
            .resolvePreviewAction(actionCatalog.getDefinition(ActionType.SIDE_JOB), gameStat, 12);

        // then
        assertThat(result.cashPreview().minAmount()).isEqualTo(200_000);
        assertThat(result.cashPreview().maxAmount()).isEqualTo(350_000);
        assertThat(result.cashPreview().rangePreview()).isTrue();
    }

    @DisplayName("행동 정책 입력이 잘못되면 예외가 발생한다.")
    @Test
    void resolvePreviewActionsWithInvalidInput() {
        assertThatThrownBy(() -> actionResolutionPolicy.resolvePreviewActions(null, 12))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);

        assertThatThrownBy(() -> actionResolutionPolicy.resolvePreviewActions(
            createGameStat(60, 30, 20, 55, 50, false, null),
            -1
        ))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.CHARACTER_TURN_INVALID);
    }

    private GameStat createGameStat(
        final int health,
        final int fatigue,
        final int stress,
        final int happiness,
        final int knowledge,
        final boolean burnout,
        final Integer hospitalizedUntilTurn
    ) {
        return GameStat.builder()
            .gameId(1001)
            .health(health)
            .fatigue(fatigue)
            .stress(stress)
            .happiness(happiness)
            .knowledge(knowledge)
            .burnout(burnout)
            .burnoutStartedTurn(burnout ? 10 : null)
            .hospitalizedUntilTurn(hospitalizedUntilTurn)
            .build();
    }

    private ActionResolutionPolicy.ResolvedAction findAction(
        final List<ActionResolutionPolicy.ResolvedAction> actions,
        final ActionType actionType
    ) {
        return actions.stream()
            .filter(action -> action.actionType() == actionType)
            .findFirst()
            .orElseThrow();
    }
}
