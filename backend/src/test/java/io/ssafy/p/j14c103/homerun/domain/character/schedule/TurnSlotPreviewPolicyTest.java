package io.ssafy.p.j14c103.homerun.domain.character.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TurnSlotPreviewPolicyTest {

    private final TurnSlotPreviewPolicy turnSlotPreviewPolicy = new TurnSlotPreviewPolicy();

    @DisplayName("정상 슬롯 3개를 입력하면 preview 합산 결과를 반환한다.")
    @Test
    void preview() {
        // given
        final GameStat gameStat = createGameStat(50, false);
        final List<TurnSlotPreviewPolicy.RequestedSlot> requestedSlots = List.of(
            TurnSlotPreviewPolicy.RequestedSlot.of(0, ActionType.STUDY),
            TurnSlotPreviewPolicy.RequestedSlot.of(1, ActionType.REST),
            TurnSlotPreviewPolicy.RequestedSlot.of(2, ActionType.SIDE_JOB)
        );

        // when
        final TurnSlotPreviewPolicy.PreviewResult result = turnSlotPreviewPolicy.preview(
            gameStat,
            requestedSlots
        );

        // then
        assertThat(result.slots())
            .extracting(
                TurnSlotPreviewPolicy.PreviewSlot::slotIndex,
                TurnSlotPreviewPolicy.PreviewSlot::actionType,
                TurnSlotPreviewPolicy.PreviewSlot::actionCategory,
                TurnSlotPreviewPolicy.PreviewSlot::forcedAction
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

    @DisplayName("슬롯이 3개가 아니면 예외가 발생한다.")
    @Test
    void previewWithInvalidSlotCount() {
        // given
        final GameStat gameStat = createGameStat(50, false);

        // when & then
        assertThatThrownBy(() -> turnSlotPreviewPolicy.preview(
            gameStat,
            List.of(
                TurnSlotPreviewPolicy.RequestedSlot.of(0, ActionType.STUDY),
                TurnSlotPreviewPolicy.RequestedSlot.of(1, ActionType.REST)
            )
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.CHARACTER_REQUEST_INVALID);
    }

    @DisplayName("슬롯 인덱스가 중복되면 예외가 발생한다.")
    @Test
    void previewWithDuplicateSlotIndex() {
        // given
        final GameStat gameStat = createGameStat(50, false);

        // when & then
        assertThatThrownBy(() -> turnSlotPreviewPolicy.preview(
            gameStat,
            List.of(
                TurnSlotPreviewPolicy.RequestedSlot.of(0, ActionType.STUDY),
                TurnSlotPreviewPolicy.RequestedSlot.of(0, ActionType.REST),
                TurnSlotPreviewPolicy.RequestedSlot.of(2, ActionType.SIDE_JOB)
            )
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.CHARACTER_REQUEST_INVALID);
    }

    @DisplayName("번아웃 상태에서는 REST 두 슬롯을 강제 행동으로 표시한다.")
    @Test
    void previewWithBurnout() {
        // given
        final GameStat gameStat = createGameStat(50, true);
        final List<TurnSlotPreviewPolicy.RequestedSlot> requestedSlots = List.of(
            TurnSlotPreviewPolicy.RequestedSlot.of(0, ActionType.REST),
            TurnSlotPreviewPolicy.RequestedSlot.of(1, ActionType.STUDY),
            TurnSlotPreviewPolicy.RequestedSlot.of(2, ActionType.REST)
        );

        // when
        final TurnSlotPreviewPolicy.PreviewResult result = turnSlotPreviewPolicy.preview(
            gameStat,
            requestedSlots
        );

        // then
        assertThat(result.slots())
            .extracting(
                TurnSlotPreviewPolicy.PreviewSlot::slotIndex,
                TurnSlotPreviewPolicy.PreviewSlot::actionType,
                TurnSlotPreviewPolicy.PreviewSlot::forcedAction
            )
            .containsExactly(
                tuple(0, ActionType.REST, true),
                tuple(1, ActionType.STUDY, false),
                tuple(2, ActionType.REST, true)
            );
        assertThat(result.statPreview().healthDelta()).isEqualTo(6);
        assertThat(result.statPreview().fatigueDelta()).isEqualTo(-18);
        assertThat(result.statPreview().stressDelta()).isEqualTo(-13);
        assertThat(result.statPreview().happinessDelta()).isEqualTo(4);
        assertThat(result.statPreview().knowledgeDelta()).isZero();
    }

    @DisplayName("번아웃 상태에서 REST가 아닌 행동을 두 개 이상 선택하면 예외가 발생한다.")
    @Test
    void previewWithInvalidBurnoutSlots() {
        // given
        final GameStat gameStat = createGameStat(50, true);

        // when & then
        assertThatThrownBy(() -> turnSlotPreviewPolicy.preview(
            gameStat,
            List.of(
                TurnSlotPreviewPolicy.RequestedSlot.of(0, ActionType.STUDY),
                TurnSlotPreviewPolicy.RequestedSlot.of(1, ActionType.EXERCISE),
                TurnSlotPreviewPolicy.RequestedSlot.of(2, ActionType.REST)
            )
        ))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.CHARACTER_REQUEST_INVALID);
    }

    private GameStat createGameStat(final int knowledge, final boolean burnout) {
        return GameStat.builder()
            .gameId(1001)
            .health(70)
            .fatigue(burnout ? 85 : 20)
            .stress(burnout ? 82 : 20)
            .happiness(50)
            .knowledge(knowledge)
            .burnout(burnout)
            .burnoutStartedTurn(burnout ? 10 : null)
            .hospitalizedUntilTurn(null)
            .build();
    }
}
