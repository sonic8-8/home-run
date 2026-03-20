package io.ssafy.p.j14c103.homerun.domain.gamesession.turn;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.ActionCatalog.ActionDefinition;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ActionCatalogTest {

    private final ActionCatalog actionCatalog = new ActionCatalog();

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

    @DisplayName("행동 카탈로그는 기획표 기준 메타데이터와 고정 cash delta를 제공한다.")
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("definitions")
    void getDefinition(
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
        final ActionDefinition result = actionCatalog.getDefinition(actionType);

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
        assertThat(result.fixedCashDelta()).isEqualTo(cashDelta);
    }

    @DisplayName("최근 12턴 장기 효과 대상 행동 메타데이터를 구분한다.")
    @Test
    void definesLongTermEffects() {
        // given
        final ActionCatalog.ActionDefinition study = actionCatalog.getDefinition(ActionType.STUDY);
        final ActionCatalog.ActionDefinition networking = actionCatalog.getDefinition(
            ActionType.NETWORKING
        );
        final ActionCatalog.ActionDefinition meetFriend = actionCatalog.getDefinition(
            ActionType.MEET_FRIEND
        );
        final ActionCatalog.ActionDefinition rest = actionCatalog.getDefinition(ActionType.REST);

        // when & then
        assertThat(study.updatesRecentStudyCount()).isTrue();
        assertThat(study.updatesRecentNetworkingCount()).isFalse();
        assertThat(networking.updatesRecentNetworkingCount()).isTrue();
        assertThat(networking.updatesRecentStudyCount()).isFalse();
        assertThat(meetFriend.isJobOfferBonusTarget()).isTrue();
        assertThat(rest.longTermEffects()).isEmpty();
    }

    private static Stream<Arguments> definitions() {
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
                0
            )
        );
    }
}
