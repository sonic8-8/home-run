package io.ssafy.p.j14c103.homerun.domain.character.schedule;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;

public class ActionCatalog {

    private final SideJobIncomePolicy sideJobIncomePolicy;
    private final List<ActionDefinition> actionDefinitions;

    public ActionCatalog() {
        this(new SideJobIncomePolicy());
    }

    ActionCatalog(final SideJobIncomePolicy sideJobIncomePolicy) {
        validatePolicyNotNull(sideJobIncomePolicy);

        this.sideJobIncomePolicy = sideJobIncomePolicy;
        this.actionDefinitions = List.of(
            new ActionDefinition(
                ActionType.STUDY,
                ActionCategory.ACTIVITY,
                "공부",
                "study",
                ActionStatDelta.of(0, 6, 3, 0, 8),
                0,
                true,
                false,
                false
            ),
            new ActionDefinition(
                ActionType.EXERCISE,
                ActionCategory.ACTIVITY,
                "운동",
                "exercise",
                ActionStatDelta.of(8, 6, -10, 3, 0),
                0,
                false,
                false,
                false
            ),
            new ActionDefinition(
                ActionType.REST,
                ActionCategory.ACTIVITY,
                "휴식",
                "rest",
                ActionStatDelta.of(3, -12, -8, 2, 0),
                0,
                false,
                false,
                false
            ),
            new ActionDefinition(
                ActionType.HOBBY,
                ActionCategory.SHOPPING,
                "취미",
                "hobby",
                ActionStatDelta.of(0, -2, -6, 6, 0),
                -100_000,
                false,
                false,
                false
            ),
            new ActionDefinition(
                ActionType.MEET_FRIEND,
                ActionCategory.SHOPPING,
                "친구 만나기",
                "meet-friend",
                ActionStatDelta.of(-3, 4, -5, 10, 2),
                -150_000,
                false,
                false,
                true
            ),
            new ActionDefinition(
                ActionType.NETWORKING,
                ActionCategory.ACTIVITY,
                "네트워킹",
                "networking",
                ActionStatDelta.of(-2, 5, 3, 0, 4),
                -50_000,
                false,
                true,
                false
            ),
            new ActionDefinition(
                ActionType.SIDE_JOB,
                ActionCategory.ACTIVITY,
                "부업",
                "side-job",
                ActionStatDelta.of(-3, 10, 8, 0, 0),
                0,
                false,
                false,
                false
            )
        );
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

    public List<ResolvedAction> getPreviewActions(final int knowledge) {
        return actionDefinitions.stream()
            .map(actionDefinition -> resolvePreviewAction(actionDefinition, knowledge))
            .toList();
    }

    public ResolvedAction getPreviewAction(final ActionType actionType, final int knowledge) {
        return resolvePreviewAction(getDefinition(actionType), knowledge);
    }

    public ResolvedAction getActualAction(final ActionType actionType, final int knowledge) {
        return resolveActualAction(getDefinition(actionType), knowledge);
    }

    private ResolvedAction resolvePreviewAction(
        final ActionDefinition actionDefinition,
        final int knowledge
    ) {
        return actionDefinition.resolve(resolvePreviewCashDelta(actionDefinition, knowledge));
    }

    private ResolvedAction resolveActualAction(
        final ActionDefinition actionDefinition,
        final int knowledge
    ) {
        return actionDefinition.resolve(resolveActualCashDelta(actionDefinition, knowledge));
    }

    private CashPreview resolvePreviewCashDelta(
        final ActionDefinition actionDefinition,
        final int knowledge
    ) {
        if (actionDefinition.actionType() != ActionType.SIDE_JOB) {
            return CashPreview.fixed(actionDefinition.fixedCashDelta());
        }

        final SideJobIncomePolicy.IncomePreview incomePreview = sideJobIncomePolicy.resolvePreview(
            knowledge
        );

        return new CashPreview(incomePreview.minimumIncome(), incomePreview.maximumIncome());
    }

    private int resolveActualCashDelta(
        final ActionDefinition actionDefinition,
        final int knowledge
    ) {
        if (actionDefinition.actionType() != ActionType.SIDE_JOB) {
            return actionDefinition.fixedCashDelta();
        }

        return sideJobIncomePolicy.calculateIncome(knowledge);
    }

    private void validateRequestNotNull(final Object value) {
        if (value == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }

    private void validatePolicyNotNull(final Object value) {
        if (value == null) {
            throwSchedulePolicyInvalid();
        }
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

        private ResolvedAction resolve(final int cashDelta) {
            return new ResolvedAction(
                actionType,
                category,
                label,
                iconKey,
                statDelta,
                CashPreview.fixed(cashDelta),
                cashDelta
            );
        }

        private ResolvedAction resolve(final CashPreview cashPreview) {
            return new ResolvedAction(
                actionType,
                category,
                label,
                iconKey,
                statDelta,
                cashPreview,
                cashPreview.minimumCashDelta()
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

    public record ResolvedAction(
        ActionType actionType,
        ActionCategory category,
        String label,
        String iconKey,
        ActionStatDelta statDelta,
        CashPreview cashPreview,
        int cashDelta
    ) {

        public ResolvedAction {
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
            if (cashPreview == null) {
                throwSchedulePolicyInvalid();
            }
        }
    }

    public record CashPreview(
        int minimumCashDelta,
        int maximumCashDelta
    ) {

        public CashPreview {
            if (minimumCashDelta > maximumCashDelta) {
                throwSchedulePolicyInvalid();
            }
        }

        public static CashPreview fixed(final int cashDelta) {
            return new CashPreview(cashDelta, cashDelta);
        }

        public boolean isRange() {
            return minimumCashDelta != maximumCashDelta;
        }
    }

    private static void throwSchedulePolicyInvalid() {
        throw new HomerunException(ErrorCode.CHARACTER_SCHEDULE_POLICY_INVALID);
    }
}
