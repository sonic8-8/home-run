package io.ssafy.p.j14c103.homerun.domain.character.schedule;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;

public class ActionCatalog {

    private final List<ActionDefinition> actionDefinitions;

    public ActionCatalog() {
        this.actionDefinitions = createDefinitions();
    }

    public List<ActionDefinition> getDefinitions() {
        return actionDefinitions;
    }

    public ActionDefinition getDefinition(final ActionType actionType) {
        validateRequestNotNull(actionType);

        return actionDefinitions.stream()
            .filter(actionDefinition -> actionDefinition.actionType() == actionType)
            .findFirst()
            .orElseThrow(() -> new HomerunException(ErrorCode.CHARACTER_ACTION_TYPE_UNSUPPORTED));
    }

    private void validateRequestNotNull(final Object value) {
        if (value == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }

    private List<ActionDefinition> createDefinitions() {
        return List.of(
            ActionDefinition.activity(
                ActionType.STUDY,
                "공부",
                "study",
                ActionStatDelta.of(0, 6, 3, 0, 8),
                0
            ).markStudyCounterTarget(),
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
                -150_000
            ).markJobOfferBonusTarget(),
            ActionDefinition.activity(
                ActionType.NETWORKING,
                "네트워킹",
                "networking",
                ActionStatDelta.of(-2, 5, 3, 0, 4),
                -50_000
            ).markNetworkingCounterTarget(),
            ActionDefinition.activity(
                ActionType.SIDE_JOB,
                "부업",
                "side-job",
                ActionStatDelta.of(-3, 10, 8, 0, 0),
                0
            )
        );
    }

    public record ActionDefinition(
        ActionType actionType,
        ActionCategory category,
        String label,
        String iconKey,
        ActionStatDelta statDelta,
        int fixedCashDelta,
        boolean studyCounterTarget,
        boolean networkingCounterTarget,
        boolean jobOfferBonusTarget
    ) {

        public static ActionDefinition activity(
            final ActionType actionType,
            final String label,
            final String iconKey,
            final ActionStatDelta statDelta,
            final int fixedCashDelta
        ) {
            return new ActionDefinition(
                actionType,
                ActionCategory.ACTIVITY,
                label,
                iconKey,
                statDelta,
                fixedCashDelta,
                false,
                false,
                false
            );
        }

        public static ActionDefinition shopping(
            final ActionType actionType,
            final String label,
            final String iconKey,
            final ActionStatDelta statDelta,
            final int fixedCashDelta
        ) {
            return new ActionDefinition(
                actionType,
                ActionCategory.SHOPPING,
                label,
                iconKey,
                statDelta,
                fixedCashDelta,
                false,
                false,
                false
            );
        }

        public ActionDefinition {
            if (actionType == null) {
                throwSchedulePolicyInvalid();
            }
            if (category == null) {
                throwSchedulePolicyInvalid();
            }
            if (label == null || label.isBlank()) {
                throwSchedulePolicyInvalid();
            }
            if (iconKey == null || iconKey.isBlank()) {
                throwSchedulePolicyInvalid();
            }
            if (statDelta == null) {
                throwSchedulePolicyInvalid();
            }
        }

        public ActionDefinition markStudyCounterTarget() {
            return new ActionDefinition(
                actionType,
                category,
                label,
                iconKey,
                statDelta,
                fixedCashDelta,
                true,
                networkingCounterTarget,
                jobOfferBonusTarget
            );
        }

        public ActionDefinition markNetworkingCounterTarget() {
            return new ActionDefinition(
                actionType,
                category,
                label,
                iconKey,
                statDelta,
                fixedCashDelta,
                studyCounterTarget,
                true,
                jobOfferBonusTarget
            );
        }

        public ActionDefinition markJobOfferBonusTarget() {
            return new ActionDefinition(
                actionType,
                category,
                label,
                iconKey,
                statDelta,
                fixedCashDelta,
                studyCounterTarget,
                networkingCounterTarget,
                true
            );
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

    private static void throwSchedulePolicyInvalid() {
        throw new HomerunException(ErrorCode.CHARACTER_SCHEDULE_POLICY_INVALID);
    }
}
