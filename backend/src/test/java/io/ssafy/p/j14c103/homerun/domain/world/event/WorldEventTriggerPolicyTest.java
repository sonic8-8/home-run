package io.ssafy.p.j14c103.homerun.domain.world.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WorldEventTriggerPolicyTest {

    private final WorldEventTriggerPolicy worldEventTriggerPolicy = new WorldEventTriggerPolicy();

    @DisplayName("사이클형 이벤트는 호황에서 20퍼센트 기준으로 발생을 판정한다")
    @Test
    void isTriggeredOvertimeEventInBoom() {
        // given
        final GameEvent gameEvent = GameEvent.create(
            "OVERTIME_REQUEST",
            "EVT-OVERTIME-001",
            "야근 요청",
            EventPresentationType.CHOICE,
            EventTriggerType.CYCLE,
            new BigDecimal("0.1000"),
            true,
            null,
            null,
            null,
            "마감 일정 때문에 야근 요청이 들어왔습니다.",
            true
        );
        final List<EventCondition> conditions = List.of(
            EventCondition.create(
                1,
                1,
                1,
                "COLUMN_COMPARISON",
                "game_sessions",
                "economic_cycle_type",
                "EQ",
                "BOOM",
                null,
                null,
                "OR"
            ),
            EventCondition.create(
                1,
                2,
                1,
                "COLUMN_COMPARISON",
                "game_sessions",
                "economic_cycle_type",
                "EQ",
                "RECOVERY",
                null,
                null,
                "OR"
            )
        );
        final WorldEventTriggerPolicy.TriggerContext triggerContext =
            WorldEventTriggerPolicy.TriggerContext.of("BOOM", 50, 6);

        // when
        final boolean triggered = worldEventTriggerPolicy.isTriggered(
            gameEvent,
            conditions,
            triggerContext,
            new BigDecimal("0.1500")
        );

        // then
        assertThat(triggered).isTrue();
    }

    @DisplayName("조건형 이벤트는 그룹 내부 AND와 그룹 간 OR 규칙으로 판정한다")
    @Test
    void isTriggeredConditionEventWithGroupedConditions() {
        // given
        final GameEvent gameEvent = GameEvent.create(
            "JOB_TRANSFER",
            "EVT-JOB-001",
            "열심히 일한 당신! 이직하시겠습니까?",
            EventPresentationType.JOB_TRANSFER,
            EventTriggerType.CONDITION,
            null,
            true,
            null,
            null,
            null,
            "열심히 일한 당신에게 새로운 이직 오퍼가 도착했습니다.",
            true
        );
        final List<EventCondition> conditions = List.of(
            EventCondition.create(
                1,
                1,
                1,
                "COLUMN_COMPARISON",
                "game_stats",
                "knowledge",
                "GTE",
                null,
                new BigDecimal("60"),
                null,
                "AND"
            ),
            EventCondition.create(
                1,
                1,
                2,
                "COLUMN_COMPARISON",
                "game_careers",
                "tenure_turns",
                "GTE",
                null,
                new BigDecimal("12"),
                null,
                "AND"
            ),
            EventCondition.create(
                1,
                2,
                1,
                "COLUMN_COMPARISON",
                "game_stats",
                "knowledge",
                "GTE",
                null,
                new BigDecimal("90"),
                null,
                "AND"
            )
        );
        final WorldEventTriggerPolicy.TriggerContext triggerContext =
            WorldEventTriggerPolicy.TriggerContext.of("RECOVERY", 60, 12);

        // when
        final boolean triggered = worldEventTriggerPolicy.isTriggered(
            gameEvent,
            conditions,
            triggerContext,
            null
        );

        // then
        assertThat(triggered).isTrue();
    }

    @DisplayName("지원하지 않는 비교 연산자는 서버 설정 오류로 실패한다")
    @Test
    void isTriggeredWithUnsupportedOperator() {
        // given
        final GameEvent gameEvent = GameEvent.create(
            "INVALID_TRIGGER",
            "EVT-INVALID-001",
            "잘못된 이벤트",
            EventPresentationType.CHOICE,
            EventTriggerType.CONDITION,
            null,
            true,
            null,
            null,
            null,
            "지원하지 않는 연산자 테스트",
            true
        );
        final List<EventCondition> conditions = List.of(
            EventCondition.create(
                1,
                1,
                1,
                "COLUMN_COMPARISON",
                "game_stats",
                "knowledge",
                "LT",
                null,
                new BigDecimal("80"),
                null,
                "AND"
            )
        );
        final WorldEventTriggerPolicy.TriggerContext triggerContext =
            WorldEventTriggerPolicy.TriggerContext.of("BOOM", 80, 12);

        // when & then
        assertThatThrownBy(() -> worldEventTriggerPolicy.isTriggered(gameEvent, conditions, triggerContext, null))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    @DisplayName("health 상한 조건은 LTE 연산자로 판정할 수 있다")
    @Test
    void isTriggeredWithLteHealthCondition() {
        // given
        final GameEvent gameEvent = GameEvent.create(
            "HOSPITALIZATION",
            "EVT-STATUS-002",
            "입원",
            EventPresentationType.CHOICE,
            EventTriggerType.CONDITION,
            null,
            true,
            null,
            null,
            null,
            "체력 저하로 병원 치료가 필요합니다.",
            true
        );
        final List<EventCondition> conditions = List.of(
            EventCondition.create(
                1,
                1,
                1,
                "COLUMN_COMPARISON",
                "game_stats",
                "health",
                "GTE",
                null,
                new BigDecimal("10"),
                null,
                "AND"
            ),
            EventCondition.create(
                1,
                1,
                2,
                "COLUMN_COMPARISON",
                "game_stats",
                "health",
                "LTE",
                null,
                new BigDecimal("29"),
                null,
                "AND"
            )
        );
        final WorldEventTriggerPolicy.TriggerContext triggerContext =
            WorldEventTriggerPolicy.TriggerContext.of("RECOVERY", null, "STUDIO", 60, 25, 20, 20, 12, "EMPLOYED");

        // when
        final boolean triggered = worldEventTriggerPolicy.isTriggered(
            gameEvent,
            conditions,
            triggerContext,
            null
        );

        // then
        assertThat(triggered).isTrue();
    }
}
