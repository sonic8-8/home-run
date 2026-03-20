package io.ssafy.p.j14c103.homerun.domain.gamesession.turn;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ActionCatalog {

    private final List<ActionDefinition> definitions = List.of(
        ActionDefinition.activity(
            ActionType.STUDY,
            "공부",
            "study",
            ActionStatDelta.of(0, 6, 3, 0, 8),
            0,
            ActionLongTermEffect.RECENT_STUDY_COUNT
        ),
        ActionDefinition.activity(
            ActionType.EXERCISE,
            "운동",
            "exercise",
            ActionStatDelta.of(8, 6, -10, 3, 0),
            0
        ),
        ActionDefinition.activity(
            ActionType.REST,
            "휴식",
            "rest",
            ActionStatDelta.of(3, -12, -8, 2, 0),
            0
        ),
        ActionDefinition.shopping(
            ActionType.HOBBY,
            "취미",
            "hobby",
            ActionStatDelta.of(0, -2, -6, 6, 0),
            -100_000
        ),
        ActionDefinition.shopping(
            ActionType.MEET_FRIEND,
            "친구 만나기",
            "meet-friend",
            ActionStatDelta.of(-3, 4, -5, 10, 2),
            -150_000,
            ActionLongTermEffect.JOB_OFFER_BONUS_TARGET
        ),
        ActionDefinition.activity(
            ActionType.NETWORKING,
            "네트워킹",
            "networking",
            ActionStatDelta.of(-2, 5, 3, 0, 4),
            -50_000,
            ActionLongTermEffect.RECENT_NETWORKING_COUNT
        ),
        ActionDefinition.sideJob(
            "부업",
            "side-job",
            ActionStatDelta.of(-3, 10, 8, 0, 0)
        )
    );

    public List<ActionDefinition> getDefinitions() {
        return definitions;
    }

    public ActionDefinition getDefinition(final ActionType actionType) {
        validateActionType(actionType);

        return definitions.stream()
            .filter(definition -> definition.actionType() == actionType)
            .findFirst()
            .orElseThrow(() -> new HomerunException(ErrorCode.SCHEDULE_ACTION_CATALOG_INVALID));
    }

    private void validateActionType(final ActionType actionType) {
        if (actionType == null) {
            throw new HomerunException(ErrorCode.SCHEDULE_ACTION_TYPE_INVALID);
        }
    }

    public record ActionDefinition(
        ActionType actionType,
        ActionCategory category,
        String label,
        String iconKey,
        ActionStatDelta statDelta,
        int fixedCashDelta,
        boolean knowledgeBasedCash,
        Set<ActionLongTermEffect> longTermEffects
    ) {

        public ActionDefinition {
            validateActionType(actionType);
            validateCategory(category);
            validateText(label);
            validateText(iconKey);
            validateStatDelta(statDelta);
            validateLongTermEffects(longTermEffects);
            longTermEffects = Set.copyOf(longTermEffects);
        }

        public static ActionDefinition activity(
            final ActionType actionType,
            final String label,
            final String iconKey,
            final ActionStatDelta statDelta,
            final int fixedCashDelta,
            final ActionLongTermEffect... longTermEffects
        ) {
            return new ActionDefinition(
                actionType,
                ActionCategory.ACTIVITY,
                label,
                iconKey,
                statDelta,
                fixedCashDelta,
                false,
                Set.of(longTermEffects)
            );
        }

        public static ActionDefinition shopping(
            final ActionType actionType,
            final String label,
            final String iconKey,
            final ActionStatDelta statDelta,
            final int fixedCashDelta,
            final ActionLongTermEffect... longTermEffects
        ) {
            return new ActionDefinition(
                actionType,
                ActionCategory.SHOPPING,
                label,
                iconKey,
                statDelta,
                fixedCashDelta,
                false,
                Set.of(longTermEffects)
            );
        }

        public static ActionDefinition sideJob(
            final String label,
            final String iconKey,
            final ActionStatDelta statDelta
        ) {
            return new ActionDefinition(
                ActionType.SIDE_JOB,
                ActionCategory.ACTIVITY,
                label,
                iconKey,
                statDelta,
                0,
                true,
                Set.of()
            );
        }

        public boolean updatesRecentStudyCount() {
            return longTermEffects.contains(ActionLongTermEffect.RECENT_STUDY_COUNT);
        }

        public boolean updatesRecentNetworkingCount() {
            return longTermEffects.contains(ActionLongTermEffect.RECENT_NETWORKING_COUNT);
        }

        public boolean isJobOfferBonusTarget() {
            return longTermEffects.contains(ActionLongTermEffect.JOB_OFFER_BONUS_TARGET);
        }

        private static void validateActionType(final ActionType actionType) {
            if (actionType == null) {
                throw new HomerunException(ErrorCode.SCHEDULE_ACTION_CATALOG_INVALID);
            }
        }

        private static void validateCategory(final ActionCategory category) {
            if (category == null) {
                throw new HomerunException(ErrorCode.SCHEDULE_ACTION_CATALOG_INVALID);
            }
        }

        private static void validateText(final String value) {
            if (value == null || value.isBlank()) {
                throw new HomerunException(ErrorCode.SCHEDULE_ACTION_CATALOG_INVALID);
            }
        }

        private static void validateStatDelta(final ActionStatDelta statDelta) {
            if (statDelta == null) {
                throw new HomerunException(ErrorCode.SCHEDULE_ACTION_CATALOG_INVALID);
            }
        }

        private static void validateLongTermEffects(final Set<ActionLongTermEffect> longTermEffects) {
            if (longTermEffects == null) {
                throw new HomerunException(ErrorCode.SCHEDULE_ACTION_CATALOG_INVALID);
            }
        }
    }

    public record ActionStatDelta(
        int healthDelta,
        int fatigueDelta,
        int stressDelta,
        int happinessDelta,
        int knowledgeDelta
    ) {

        public static ActionStatDelta of(
            final int healthDelta,
            final int fatigueDelta,
            final int stressDelta,
            final int happinessDelta,
            final int knowledgeDelta
        ) {
            return new ActionStatDelta(
                healthDelta,
                fatigueDelta,
                stressDelta,
                happinessDelta,
                knowledgeDelta
            );
        }
    }
}
