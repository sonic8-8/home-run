package io.ssafy.p.j14c103.homerun.api.service.character.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import io.ssafy.p.j14c103.homerun.api.service.character.schedule.request.TurnSlotPreviewRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.schedule.response.TurnSlotPreviewResponse;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionCategory;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import io.ssafy.p.j14c103.homerun.support.IntegrationTestSupport;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class TurnSlotPreviewServiceTest extends IntegrationTestSupport {

    @Autowired
    private TurnSlotPreviewService turnSlotPreviewService;

    @DisplayName("슬롯 preview 서비스는 강제 행동 여부와 합산 preview를 반환한다.")
    @Test
    void preview() {
        // given
        final TurnSlotPreviewRequest request = TurnSlotPreviewRequest.of(
            createGameStat(true),
            List.of(
                TurnSlotPreviewRequest.TurnSlotRequest.of(0, ActionType.REST),
                TurnSlotPreviewRequest.TurnSlotRequest.of(1, ActionType.SIDE_JOB),
                TurnSlotPreviewRequest.TurnSlotRequest.of(2, ActionType.REST)
            )
        );

        // when
        final TurnSlotPreviewResponse response = turnSlotPreviewService.preview(request);

        // then
        assertThat(response.slots())
            .extracting(
                TurnSlotPreviewResponse.TurnSlotResponse::slotIndex,
                TurnSlotPreviewResponse.TurnSlotResponse::actionType,
                TurnSlotPreviewResponse.TurnSlotResponse::actionCategory,
                TurnSlotPreviewResponse.TurnSlotResponse::forcedAction
            )
            .containsExactly(
                tuple(0, ActionType.REST, ActionCategory.ACTIVITY, true),
                tuple(1, ActionType.SIDE_JOB, ActionCategory.ACTIVITY, false),
                tuple(2, ActionType.REST, ActionCategory.ACTIVITY, true)
            );
        assertThat(response.statPreview().healthDelta()).isEqualTo(3);
        assertThat(response.statPreview().fatigueDelta()).isEqualTo(-14);
        assertThat(response.statPreview().stressDelta()).isEqualTo(-8);
        assertThat(response.statPreview().happinessDelta()).isEqualTo(4);
        assertThat(response.statPreview().knowledgeDelta()).isZero();
        assertThat(response.cashPreview().minimumCashDelta()).isEqualTo(430_000);
        assertThat(response.cashPreview().maximumCashDelta()).isEqualTo(430_000);
        assertThat(response.cashPreview().rangePreview()).isFalse();
    }

    @DisplayName("번아웃 제약을 어긴 슬롯 요청이면 예외가 발생한다.")
    @Test
    void previewWithInvalidBurnoutSlots() {
        // given
        final TurnSlotPreviewRequest request = TurnSlotPreviewRequest.of(
            createGameStat(true),
            List.of(
                TurnSlotPreviewRequest.TurnSlotRequest.of(0, ActionType.STUDY),
                TurnSlotPreviewRequest.TurnSlotRequest.of(1, ActionType.SIDE_JOB),
                TurnSlotPreviewRequest.TurnSlotRequest.of(2, ActionType.REST)
            )
        );

        // when & then
        assertThatThrownBy(() -> turnSlotPreviewService.preview(request))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.CHARACTER_REQUEST_INVALID);
    }

    private GameStat createGameStat(final boolean burnout) {
        return GameStat.builder()
            .gameId(1001)
            .health(70)
            .fatigue(burnout ? 85 : 20)
            .stress(burnout ? 82 : 20)
            .happiness(50)
            .knowledge(50)
            .burnout(burnout)
            .burnoutStartedTurn(burnout ? 9 : null)
            .hospitalizedUntilTurn(null)
            .build();
    }
}
