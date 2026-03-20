package io.ssafy.p.j14c103.homerun.domain.character.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionResolutionPolicy.ResolvedAction;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ActionResolutionPolicyTest {

    private final ActionResolutionPolicy actionResolutionPolicy =
        new ActionResolutionPolicy(new ActionCatalog(), new SideJobIncomePolicy());

    @DisplayName("실제 부업 효과는 SideJobIncomePolicy 계산 결과를 사용한다.")
    @Test
    void getActualAction() {
        // when
        final ResolvedAction result = actionResolutionPolicy.getActualAction(ActionType.SIDE_JOB, 55);

        // then
        assertThat(result.cashDelta()).isEqualTo(470_000);
    }

    @DisplayName("저지식 부업 actual은 SideJobIncomePolicy의 랜덤 계산 결과를 사용한다.")
    @Test
    void useRandomCashDeltaForLowKnowledgeSideJob() {
        // given
        final ActionResolutionPolicy randomPolicy = new ActionResolutionPolicy(
            new ActionCatalog(),
            new SideJobIncomePolicy((minimumIncome, maximumIncome) -> 250_000)
        );

        // when
        final ResolvedAction result = randomPolicy.getActualAction(ActionType.SIDE_JOB, 10);

        // then
        assertThat(result.cashDelta()).isEqualTo(250_000);
    }

    @DisplayName("저지식 부업 preview는 cash delta 범위를 사용한다.")
    @Test
    void useRangePreviewForLowKnowledgeSideJob() {
        // when
        final ResolvedAction preview = actionResolutionPolicy.getPreviewAction(ActionType.SIDE_JOB, 10);

        // then
        assertThat(preview.cashPreview().minimumCashDelta()).isEqualTo(200_000);
        assertThat(preview.cashPreview().maximumCashDelta()).isEqualTo(350_000);
        assertThat(preview.cashPreview().isRange()).isTrue();
    }

    @DisplayName("중고지식 부업 preview는 actual과 같은 결정형 cash delta를 사용한다.")
    @Test
    void useDeterministicCashDeltaForMidAndHighKnowledgeSideJob() {
        // when
        final ResolvedAction midPreview = actionResolutionPolicy.getPreviewAction(ActionType.SIDE_JOB, 50);
        final ResolvedAction highPreview = actionResolutionPolicy.getPreviewAction(ActionType.SIDE_JOB, 80);
        final ResolvedAction midActual = actionResolutionPolicy.getActualAction(ActionType.SIDE_JOB, 50);
        final ResolvedAction highActual = actionResolutionPolicy.getActualAction(ActionType.SIDE_JOB, 80);

        // then
        assertThat(midPreview.cashPreview().minimumCashDelta()).isEqualTo(430_000);
        assertThat(midPreview.cashPreview().maximumCashDelta()).isEqualTo(430_000);
        assertThat(highPreview.cashPreview().minimumCashDelta()).isEqualTo(790_000);
        assertThat(highPreview.cashPreview().maximumCashDelta()).isEqualTo(790_000);
        assertThat(midActual.cashDelta()).isEqualTo(430_000);
        assertThat(highActual.cashDelta()).isEqualTo(790_000);
    }

    @DisplayName("행동 계산 정책 구성이 잘못되면 에러코드 기반 예외가 발생한다.")
    @Test
    void createWithInvalidPolicy() {
        assertThatThrownBy(() -> new ActionResolutionPolicy(null, new SideJobIncomePolicy()))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.CHARACTER_POLICY_INVALID);
    }
}
