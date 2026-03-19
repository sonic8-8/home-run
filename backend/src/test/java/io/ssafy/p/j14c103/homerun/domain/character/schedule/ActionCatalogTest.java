package io.ssafy.p.j14c103.homerun.domain.character.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionCatalog.ActionDefinition;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionCatalog.ResolvedAction;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ActionCatalogTest {

    private final ActionCatalog actionCatalog = new ActionCatalog(new SideJobIncomePolicy());

    @DisplayName("행동 카탈로그는 7개 코어 행동을 순서대로 제공한다.")
    @Test
    void getDefinitions() {
        // when
        final var result = actionCatalog.getDefinitions();

        // then
        assertThat(result)
            .extracting(ActionDefinition::actionType)
            .containsExactly(
                ActionType.STUDY,
                ActionType.EXERCISE,
                ActionType.REST,
                ActionType.HOBBY,
                ActionType.MEET_FRIEND,
                ActionType.NETWORKING,
                ActionType.SIDE_JOB
            );
    }

    @DisplayName("행동 카탈로그는 기획표 기준 메타데이터와 preview delta를 제공한다.")
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("previewActions")
    void getPreviewAction(
        final ActionType actionType,
        final ActionCategory category,
        final String label,
        final String iconKey,
        final int healthDelta,
        final int fatigueDelta,
        final int stressDelta,
        final int happinessDelta,
        final int knowledgeDelta,
        final int cashDelta
    ) {
        // when
        final ResolvedAction result = actionCatalog.getPreviewAction(actionType, 50);

        // then
        assertThat(result.actionType()).isEqualTo(actionType);
        assertThat(result.category()).isEqualTo(category);
        assertThat(result.label()).isEqualTo(label);
        assertThat(result.iconKey()).isEqualTo(iconKey);
        assertThat(result.statDelta().healthDelta()).isEqualTo(healthDelta);
        assertThat(result.statDelta().fatigueDelta()).isEqualTo(fatigueDelta);
        assertThat(result.statDelta().stressDelta()).isEqualTo(stressDelta);
        assertThat(result.statDelta().happinessDelta()).isEqualTo(happinessDelta);
        assertThat(result.statDelta().knowledgeDelta()).isEqualTo(knowledgeDelta);
        assertThat(result.cashPreview().minimumCashDelta()).isEqualTo(cashDelta);
        assertThat(result.cashPreview().maximumCashDelta()).isEqualTo(cashDelta);
    }

    @DisplayName("최근 12턴 장기 효과 대상 행동 메타데이터를 구분한다.")
    @Test
    void categorizeCounterTargets() {
        // given
        final ActionDefinition study = actionCatalog.getDefinition(ActionType.STUDY);
        final ActionDefinition meetFriend = actionCatalog.getDefinition(ActionType.MEET_FRIEND);
        final ActionDefinition networking = actionCatalog.getDefinition(ActionType.NETWORKING);
        final ActionDefinition exercise = actionCatalog.getDefinition(ActionType.EXERCISE);

        // then
        assertThat(study.studyCounterTarget()).isTrue();
        assertThat(study.networkingCounterTarget()).isFalse();
        assertThat(study.jobOfferBonusTarget()).isFalse();

        assertThat(meetFriend.studyCounterTarget()).isFalse();
        assertThat(meetFriend.networkingCounterTarget()).isFalse();
        assertThat(meetFriend.jobOfferBonusTarget()).isTrue();

        assertThat(networking.studyCounterTarget()).isFalse();
        assertThat(networking.networkingCounterTarget()).isTrue();
        assertThat(networking.jobOfferBonusTarget()).isFalse();

        assertThat(exercise.studyCounterTarget()).isFalse();
        assertThat(exercise.networkingCounterTarget()).isFalse();
        assertThat(exercise.jobOfferBonusTarget()).isFalse();
    }

    @DisplayName("실제 부업 효과는 SideJobIncomePolicy 계산 결과를 사용한다.")
    @Test
    void getActualAction() {
        // when
        final ResolvedAction result = actionCatalog.getActualAction(ActionType.SIDE_JOB, 55);

        // then
        assertThat(result.cashDelta()).isEqualTo(470_000);
    }

    @DisplayName("저지식 부업 actual은 SideJobIncomePolicy의 랜덤 계산 결과를 사용한다.")
    @Test
    void useRandomCashDeltaForLowKnowledgeSideJob() {
        // given
        final ActionCatalog randomCatalog = new ActionCatalog(
            new SideJobIncomePolicy((minimumIncome, maximumIncome) -> 250_000)
        );

        // when
        final ResolvedAction result = randomCatalog.getActualAction(ActionType.SIDE_JOB, 10);

        // then
        assertThat(result.cashDelta()).isEqualTo(250_000);
    }

    @DisplayName("저지식 부업 preview는 cash delta 범위를 사용한다.")
    @Test
    void useRangePreviewForLowKnowledgeSideJob() {
        // when
        final ResolvedAction preview = actionCatalog.getPreviewAction(ActionType.SIDE_JOB, 10);

        // then
        assertThat(preview.cashPreview().minimumCashDelta()).isEqualTo(200_000);
        assertThat(preview.cashPreview().maximumCashDelta()).isEqualTo(350_000);
        assertThat(preview.cashPreview().isRange()).isTrue();
    }

    @DisplayName("중고지식 부업 preview는 actual과 같은 결정형 cash delta를 사용한다.")
    @Test
    void useDeterministicCashDeltaForMidAndHighKnowledgeSideJob() {
        // when
        final ResolvedAction midPreview = actionCatalog.getPreviewAction(ActionType.SIDE_JOB, 50);
        final ResolvedAction highPreview = actionCatalog.getPreviewAction(ActionType.SIDE_JOB, 80);
        final ResolvedAction midActual = actionCatalog.getActualAction(ActionType.SIDE_JOB, 50);
        final ResolvedAction highActual = actionCatalog.getActualAction(ActionType.SIDE_JOB, 80);

        // then
        assertThat(midPreview.cashPreview().minimumCashDelta()).isEqualTo(430_000);
        assertThat(midPreview.cashPreview().maximumCashDelta()).isEqualTo(430_000);
        assertThat(highPreview.cashPreview().minimumCashDelta()).isEqualTo(790_000);
        assertThat(highPreview.cashPreview().maximumCashDelta()).isEqualTo(790_000);
        assertThat(midActual.cashDelta()).isEqualTo(430_000);
        assertThat(highActual.cashDelta()).isEqualTo(790_000);
    }

    @DisplayName("행동 카탈로그 정책 구성이 잘못되면 에러코드 기반 예외가 발생한다.")
    @Test
    void createWithInvalidPolicy() {
        assertThatThrownBy(() -> new ActionCatalog(null))
            .isInstanceOf(HomerunException.class)
            .extracting(exception -> ((HomerunException) exception).getErrorCode())
            .isEqualTo(ErrorCode.CHARACTER_SCHEDULE_POLICY_INVALID);
    }

    private static Stream<Arguments> previewActions() {
        return Stream.of(
            Arguments.of(
                ActionType.STUDY,
                ActionCategory.ACTIVITY,
                "공부",
                "study",
                0,
                6,
                3,
                0,
                8,
                0
            ),
            Arguments.of(
                ActionType.EXERCISE,
                ActionCategory.ACTIVITY,
                "운동",
                "exercise",
                8,
                6,
                -10,
                3,
                0,
                0
            ),
            Arguments.of(
                ActionType.REST,
                ActionCategory.ACTIVITY,
                "휴식",
                "rest",
                3,
                -12,
                -8,
                2,
                0,
                0
            ),
            Arguments.of(
                ActionType.HOBBY,
                ActionCategory.SHOPPING,
                "취미",
                "hobby",
                0,
                -2,
                -6,
                6,
                0,
                -100_000
            ),
            Arguments.of(
                ActionType.MEET_FRIEND,
                ActionCategory.SHOPPING,
                "친구 만나기",
                "meet-friend",
                -3,
                4,
                -5,
                10,
                2,
                -150_000
            ),
            Arguments.of(
                ActionType.NETWORKING,
                ActionCategory.ACTIVITY,
                "네트워킹",
                "networking",
                -2,
                5,
                3,
                0,
                4,
                -50_000
            ),
            Arguments.of(
                ActionType.SIDE_JOB,
                ActionCategory.ACTIVITY,
                "부업",
                "side-job",
                -3,
                10,
                8,
                0,
                0,
                430_000
            )
        );
    }
}
