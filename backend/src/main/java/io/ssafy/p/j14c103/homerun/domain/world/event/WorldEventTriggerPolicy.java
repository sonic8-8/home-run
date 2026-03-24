package io.ssafy.p.j14c103.homerun.domain.world.event;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class WorldEventTriggerPolicy {

    private static final String COLUMN_COMPARISON = "COLUMN_COMPARISON";
    private static final String GAME_SESSIONS = "game_sessions";
    private static final String GAME_STATS = "game_stats";
    private static final String GAME_CAREERS = "game_careers";
    private static final String ECONOMIC_CYCLE_TYPE = "economic_cycle_type";
    private static final String KNOWLEDGE = "knowledge";
    private static final String TENURE_TURNS = "tenure_turns";
    private static final String EQ = "EQ";
    private static final String GTE = "GTE";
    private static final String AND = "AND";
    private static final String OR = "OR";
    private static final String OVERTIME_EVENT_CODE = "EVT-OVERTIME-001";
    private static final String BOOM = "BOOM";
    private static final String RECOVERY = "RECOVERY";
    private static final BigDecimal BOOM_OVERTIME_THRESHOLD = new BigDecimal("0.2000");
    private static final BigDecimal DEFAULT_MISS_ROLL = BigDecimal.ONE;

    public boolean isTriggered(
        final GameEvent gameEvent,
        final List<EventCondition> conditions,
        final TriggerContext triggerContext,
        final BigDecimal roll
    ) {
        validateGameEvent(gameEvent);
        validateTriggerContext(triggerContext);

        if (!gameEvent.isActiveYn()) {
            return false;
        }

        if (gameEvent.getEventTriggerType() == EventTriggerType.PROBABILITY) {
            return isProbabilityTriggered(gameEvent, roll);
        }
        if (gameEvent.getEventTriggerType() == EventTriggerType.CONDITION) {
            return areConditionsSatisfied(conditions, triggerContext);
        }
        if (gameEvent.getEventTriggerType() == EventTriggerType.CYCLE) {
            if (!areConditionsSatisfied(conditions, triggerContext)) {
                return false;
            }
            return isCycleTriggered(gameEvent, triggerContext, roll);
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private boolean isProbabilityTriggered(final GameEvent gameEvent, final BigDecimal roll) {
        final BigDecimal threshold = requireThreshold(gameEvent.getEventTriggerValue());
        return resolveRoll(roll).compareTo(threshold) < 0;
    }

    private boolean isCycleTriggered(
        final GameEvent gameEvent,
        final TriggerContext triggerContext,
        final BigDecimal roll
    ) {
        final BigDecimal threshold = resolveCycleThreshold(gameEvent, triggerContext);
        return resolveRoll(roll).compareTo(threshold) < 0;
    }

    private BigDecimal resolveCycleThreshold(
        final GameEvent gameEvent,
        final TriggerContext triggerContext
    ) {
        if (OVERTIME_EVENT_CODE.equals(gameEvent.getEventCode())
            && BOOM.equals(triggerContext.economicCycleType())) {
            return BOOM_OVERTIME_THRESHOLD;
        }

        return requireThreshold(gameEvent.getEventTriggerValue());
    }

    private boolean areConditionsSatisfied(
        final List<EventCondition> conditions,
        final TriggerContext triggerContext
    ) {
        if (conditions == null || conditions.isEmpty()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }

        final Map<Integer, List<EventCondition>> groupedConditions = conditions.stream()
            .collect(Collectors.groupingBy(
                condition -> condition.getConditionGroupNumber() == null ? 0 : condition.getConditionGroupNumber()
            ));

        return groupedConditions.values().stream()
            .anyMatch(group -> evaluateConditionGroup(group, triggerContext));
    }

    private boolean evaluateConditionGroup(
        final List<EventCondition> group,
        final TriggerContext triggerContext
    ) {
        if (group.isEmpty()) {
            return false;
        }

        final String operator = resolveLogicalOperator(group);
        if (OR.equals(operator)) {
            return group.stream().anyMatch(condition -> evaluateCondition(condition, triggerContext));
        }

        return group.stream().allMatch(condition -> evaluateCondition(condition, triggerContext));
    }

    private String resolveLogicalOperator(final List<EventCondition> group) {
        return group.stream()
            .map(EventCondition::getLogicalOperatorType)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(AND);
    }

    private boolean evaluateCondition(
        final EventCondition condition,
        final TriggerContext triggerContext
    ) {
        if (!COLUMN_COMPARISON.equals(condition.getConditionType())) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }

        if (EQ.equals(condition.getComparisonOperator())) {
            return evaluateEquals(condition, triggerContext);
        }
        if (GTE.equals(condition.getComparisonOperator())) {
            return evaluateGreaterThanOrEqual(condition, triggerContext);
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private boolean evaluateEquals(
        final EventCondition condition,
        final TriggerContext triggerContext
    ) {
        final String actualValue = resolveTextValue(condition, triggerContext);
        final String criteriaValue = condition.getCriteriaTextValue();
        if (criteriaValue == null || criteriaValue.isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }

        return actualValue.equals(criteriaValue);
    }

    private boolean evaluateGreaterThanOrEqual(
        final EventCondition condition,
        final TriggerContext triggerContext
    ) {
        final BigDecimal actualValue = resolveNumericValue(condition, triggerContext);
        final BigDecimal criteriaValue = condition.getCriteriaNumberValue1();
        if (criteriaValue == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }

        return actualValue.compareTo(criteriaValue) >= 0;
    }

    private String resolveTextValue(
        final EventCondition condition,
        final TriggerContext triggerContext
    ) {
        if (GAME_SESSIONS.equals(condition.getTargetTableName())
            && ECONOMIC_CYCLE_TYPE.equals(condition.getTargetColumnName())) {
            return triggerContext.economicCycleType();
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private BigDecimal resolveNumericValue(
        final EventCondition condition,
        final TriggerContext triggerContext
    ) {
        if (GAME_STATS.equals(condition.getTargetTableName())
            && KNOWLEDGE.equals(condition.getTargetColumnName())) {
            return BigDecimal.valueOf(triggerContext.knowledge().longValue());
        }
        if (GAME_CAREERS.equals(condition.getTargetTableName())
            && TENURE_TURNS.equals(condition.getTargetColumnName())) {
            return BigDecimal.valueOf(triggerContext.tenureTurns().longValue());
        }

        throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
    }

    private BigDecimal requireThreshold(final BigDecimal threshold) {
        if (threshold == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }

        return threshold;
    }

    private BigDecimal resolveRoll(final BigDecimal roll) {
        if (roll == null) {
            return DEFAULT_MISS_ROLL;
        }

        return roll;
    }

    private void validateGameEvent(final GameEvent gameEvent) {
        if (gameEvent == null || gameEvent.getEventTriggerType() == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private void validateTriggerContext(final TriggerContext triggerContext) {
        if (triggerContext == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    public record TriggerContext(
        String economicCycleType,
        Integer knowledge,
        Integer tenureTurns
    ) {

        public TriggerContext {
            if (economicCycleType == null || economicCycleType.isBlank()) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            if (knowledge == null || tenureTurns == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
        }

        public static TriggerContext of(
            final String economicCycleType,
            final Integer knowledge,
            final Integer tenureTurns
        ) {
            return new TriggerContext(economicCycleType, knowledge, tenureTurns);
        }
    }
}
