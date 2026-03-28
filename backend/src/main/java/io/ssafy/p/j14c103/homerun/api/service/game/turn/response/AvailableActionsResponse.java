package io.ssafy.p.j14c103.homerun.api.service.game.turn.response;

import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.ActionCatalog.ActionDefinition;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.ActionCatalog.ActionStatDelta;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.ActionCategory;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AvailableActionsResponse {

    private final List<ActionResponse> shopping;
    private final List<ActionResponse> activities;

    @Builder(access = AccessLevel.PRIVATE)
    private AvailableActionsResponse(
        final List<ActionResponse> shopping,
        final List<ActionResponse> activities
    ) {
        validateActions(shopping);
        validateActions(activities);
        this.shopping = List.copyOf(shopping);
        this.activities = List.copyOf(activities);
    }

    public static AvailableActionsResponse from(final List<ActionDefinition> definitions) {
        validateDefinitions(definitions);
        return AvailableActionsResponse.builder()
            .shopping(mapByCategory(definitions, ActionCategory.SHOPPING))
            .activities(mapByCategory(definitions, ActionCategory.ACTIVITY))
            .build();
    }

    public static AvailableActionsResponse of(
        final List<ActionResponse> shopping,
        final List<ActionResponse> activities
    ) {
        return AvailableActionsResponse.builder()
            .shopping(shopping)
            .activities(activities)
            .build();
    }

    private static List<ActionResponse> mapByCategory(
        final List<ActionDefinition> definitions,
        final ActionCategory category
    ) {
        return definitions.stream()
            .filter(definition -> definition.category() == category)
            .map(ActionResponse::from)
            .toList();
    }

    private static void validateDefinitions(final List<ActionDefinition> definitions) {
        if (definitions == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    private static void validateActions(final List<ActionResponse> actions) {
        if (actions == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    @Getter
    public static class ActionResponse {

        private final String actionType;
        private final String label;
        private final String iconUrl;
        private final ActionEffectResponse effects;

        @Builder(access = AccessLevel.PRIVATE)
        private ActionResponse(
            final String actionType,
            final String label,
            final String iconUrl,
            final ActionEffectResponse effects
        ) {
            validateText(actionType);
            validateText(label);
            validateText(iconUrl);
            validateEffect(effects);
            this.actionType = actionType;
            this.label = label;
            this.iconUrl = iconUrl;
            this.effects = effects;
        }

        public static ActionResponse from(final ActionDefinition definition) {
            return ActionResponse.builder()
                .actionType(definition.actionType().name())
                .label(definition.label())
                .iconUrl("/images/actions/" + definition.iconKey() + ".png")
                .effects(ActionEffectResponse.from(definition.statDelta(), definition.fixedCashDelta()))
                .build();
        }

        public static ActionResponse of(
            final String actionType,
            final String label,
            final String iconUrl,
            final ActionEffectResponse effects
        ) {
            return ActionResponse.builder()
                .actionType(actionType)
                .label(label)
                .iconUrl(iconUrl)
                .effects(effects)
                .build();
        }

        private static void validateText(final String value) {
            if (value == null || value.isBlank()) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
        }

        private static void validateEffect(final ActionEffectResponse effects) {
            if (effects == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
        }
    }

    @Getter
    public static class ActionEffectResponse {

        private final Integer cash;
        private final Integer health;
        private final Integer fatigue;
        private final Integer stress;
        private final Integer happiness;
        private final Integer knowledge;

        @Builder(access = AccessLevel.PRIVATE)
        private ActionEffectResponse(
            final Integer cash,
            final Integer health,
            final Integer fatigue,
            final Integer stress,
            final Integer happiness,
            final Integer knowledge
        ) {
            this.cash = validateNumber(cash);
            this.health = validateNumber(health);
            this.fatigue = validateNumber(fatigue);
            this.stress = validateNumber(stress);
            this.happiness = validateNumber(happiness);
            this.knowledge = validateNumber(knowledge);
        }

        public static ActionEffectResponse from(
            final ActionStatDelta statDelta,
            final int fixedCashDelta
        ) {
            validateStatDelta(statDelta);
            return ActionEffectResponse.builder()
                .cash(fixedCashDelta)
                .health(statDelta.healthDelta())
                .fatigue(statDelta.fatigueDelta())
                .stress(statDelta.stressDelta())
                .happiness(statDelta.happinessDelta())
                .knowledge(statDelta.knowledgeDelta())
                .build();
        }

        public static ActionEffectResponse of(
            final Integer cash,
            final Integer health,
            final Integer fatigue,
            final Integer stress,
            final Integer happiness,
            final Integer knowledge
        ) {
            return ActionEffectResponse.builder()
                .cash(cash)
                .health(health)
                .fatigue(fatigue)
                .stress(stress)
                .happiness(happiness)
                .knowledge(knowledge)
                .build();
        }

        private static void validateStatDelta(final ActionStatDelta statDelta) {
            if (statDelta == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
        }

        private static Integer validateNumber(final Integer value) {
            if (value == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            return value;
        }
    }
}
