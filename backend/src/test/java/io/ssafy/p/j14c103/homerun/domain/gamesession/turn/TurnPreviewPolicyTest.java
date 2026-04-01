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

class TurnPreviewPolicyTest {

    private final TurnPreviewPolicy turnPreviewPolicy = new TurnPreviewPolicy(
        new ActionResolutionPolicy(new ActionCatalog(), new SideJobIncomePolicy())
    );

    @DisplayName("정상 슬롯 3개를 입력하면 게임 코어 preview 합산 결과를 반환한다.")
    @Test
    void preview() {
        final GameStat gameStat = createGameStat(70, 20, 20, 50, 50, false, null);
        final List<TurnPreviewPolicy.RequestedSlot> requestedSlots = List.of(
            TurnPreviewPolicy.RequestedSlot.of(0, ActionType.STUDY),
            TurnPreviewPolicy.RequestedSlot.of(1, ActionType.REST),
            TurnPreviewPolicy.RequestedSlot.of(2, ActionType.SIDE_JOB)
        );

        final TurnPreviewPolicy.PreviewResult result = turnPreviewPolicy.preview(
            gameStat,
            12,
            requestedSlots
        );

        assertThat(result.slots())
            .extracting(
                TurnPreviewPolicy.PreviewSlot::slotIndex,
                TurnPreviewPolicy.PreviewSlot::actionType,
                TurnPreviewPolicy.PreviewSlot::actionCategory,
                TurnPreviewPolicy.PreviewSlot::forcedAction
            )
            .containsExactly(
                tuple(0, ActionType.STUDY, ActionCategory.ACTIVITY, false),
                tuple(1, ActionType.REST, ActionCategory.ACTIVITY, false),
                tuple(2, ActionType.SIDE_JOB, ActionCategory.ACTIVITY, false)
            );
        assertThat(result.statPreview().healthDelta()).isEqualTo(0);
        assertThat(result.statPreview().fatigueDelta()).isEqualTo(4);
        assertThat(result.statPreview().stressDelta()).isEqualTo(3);
        assertThat(result.statPreview().happinessDelta()).isEqualTo(2);
        assertThat(result.statPreview().knowledgeDelta()).isEqualTo(8);
        assertThat(result.cashPreview().minimumCashDelta()).isEqualTo(430_000);
        assertThat(result.cashPreview().maximumCashDelta()).isEqualTo(430_000);
        assertThat(result.cashPreview().rangePreview()).isFalse();
    }

    @DisplayName("번아웃 상태에서는 REST 두 슬롯을 강제 행동으로 표시하고 지식 증가를 막는다.")
    @Test
    void previewWithBurnout() {
        final GameStat gameStat = createGameStat(70, 85, 82, 50, 50, true, null);
        final List<TurnPreviewPolicy.RequestedSlot> requestedSlots = List.of(
            TurnPreviewPolicy.RequestedSlot.of(0, ActionType.REST),
            TurnPreviewPolicy.RequestedSlot.of(1, ActionType.STUDY),
            TurnPreviewPolicy.RequestedSlot.of(2, ActionType.REST)
        );

        final TurnPreviewPolicy.PreviewResult result = turnPreviewPolicy.preview(
            gameStat,
            12,
            requestedSlots
        );

        assertThat(result.slots())
            .extracting(
                TurnPreviewPolicy.PreviewSlot::slotIndex,
                TurnPreviewPolicy.PreviewSlot::actionType,
                TurnPreviewPolicy.PreviewSlot::forcedAction
            )
            .containsExactly(
                tuple(0, ActionType.REST, true),
                tuple(1, ActionType.STUDY, false),
                tuple(2, ActionType.REST, true)
            );
        assertThat(result.statPreview().knowledgeDelta()).isZero();
    }

    @DisplayName("입원 상태에서는 REST 세 슬롯만 허용하고 모두 강제 행동으로 표시한다.")
    @Test
    void previewWithHospitalization() {
        final GameStat gameStat = createGameStat(40, 30, 20, 55, 45, false, 12);
        final List<TurnPreviewPolicy.RequestedSlot> requestedSlots = List.of(
            TurnPreviewPolicy.RequestedSlot.of(0, ActionType.REST),
            TurnPreviewPolicy.RequestedSlot.of(1, ActionType.REST),
            TurnPreviewPolicy.RequestedSlot.of(2, ActionType.REST)
        );

        final TurnPreviewPolicy.PreviewResult result = turnPreviewPolicy.preview(
            gameStat,
            12,
            requestedSlots
        );

        assertThat(result.slots())
            .extracting(
                TurnPreviewPolicy.PreviewSlot::slotIndex,
                TurnPreviewPolicy.PreviewSlot::actionType,
                TurnPreviewPolicy.PreviewSlot::forcedAction
            )
            .containsExactly(
                tuple(0, ActionType.REST, true),
                tuple(1, ActionType.REST, true),
                tuple(2, ActionType.REST, true)
            );
    }

    @DisplayName("입원 상태에서 REST가 아닌 행동을 제출하면 예외가 발생한다.")
    @Test
    void previewWithInvalidHospitalizedSlots() {
        final GameStat gameStat = createGameStat(40, 30, 20, 55, 45, false, 12);

        assertThatThrownBy(() -> turnPreviewPolicy.preview(
            gameStat,
            12,
            List.of(
                TurnPreviewPolicy.RequestedSlot.of(0, ActionType.REST),
                TurnPreviewPolicy.RequestedSlot.of(1, ActionType.STUDY),
                TurnPreviewPolicy.RequestedSlot.of(2, ActionType.REST)
            )
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @DisplayName("저지식 부업 preview는 range cash preview를 유지한다.")
    @Test
    void previewWithLowKnowledgeSideJob() {
        final GameStat gameStat = createGameStat(70, 20, 20, 50, 10, false, null);

        final TurnPreviewPolicy.PreviewResult result = turnPreviewPolicy.preview(
            gameStat,
            12,
            List.of(
                TurnPreviewPolicy.RequestedSlot.of(0, ActionType.STUDY),
                TurnPreviewPolicy.RequestedSlot.of(1, ActionType.REST),
                TurnPreviewPolicy.RequestedSlot.of(2, ActionType.SIDE_JOB)
            )
        );

        assertThat(result.cashPreview().minimumCashDelta()).isEqualTo(200_000);
        assertThat(result.cashPreview().maximumCashDelta()).isEqualTo(350_000);
        assertThat(result.cashPreview().rangePreview()).isTrue();
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
}
