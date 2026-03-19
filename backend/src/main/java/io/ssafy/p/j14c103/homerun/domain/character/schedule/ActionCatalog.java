package io.ssafy.p.j14c103.homerun.domain.character.schedule;

import java.util.List;

public class ActionCatalog {

    private final SideJobIncomePolicy sideJobIncomePolicy;
    private final List<ActionDefinition> actionDefinitions;

    public ActionCatalog() {
        this(new SideJobIncomePolicy());
    }

    ActionCatalog(final SideJobIncomePolicy sideJobIncomePolicy) {
        validateNotNull(sideJobIncomePolicy, "sideJobIncomePolicy");

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
        validateNotNull(actionType, "actionType");

        return actionDefinitions.stream()
            .filter(actionDefinition -> actionDefinition.actionType() == actionType)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 actionType입니다: " + actionType));
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

    private void validateNotNull(final Object value, final String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + "은 null일 수 없습니다.");
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
                throw new IllegalArgumentException("actionType은 null일 수 없습니다.");
            }
            if (category == null) {
                throw new IllegalArgumentException("category는 null일 수 없습니다.");
            }
            if (label == null || label.isBlank()) {
                throw new IllegalArgumentException("label은 비어 있을 수 없습니다.");
            }
            if (iconKey == null || iconKey.isBlank()) {
                throw new IllegalArgumentException("iconKey는 비어 있을 수 없습니다.");
            }
            if (statDelta == null) {
                throw new IllegalArgumentException("statDelta는 null일 수 없습니다.");
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
                throw new IllegalArgumentException("actionType은 null일 수 없습니다.");
            }
            if (category == null) {
                throw new IllegalArgumentException("category는 null일 수 없습니다.");
            }
            if (label == null || label.isBlank()) {
                throw new IllegalArgumentException("label은 비어 있을 수 없습니다.");
            }
            if (iconKey == null || iconKey.isBlank()) {
                throw new IllegalArgumentException("iconKey는 비어 있을 수 없습니다.");
            }
            if (statDelta == null) {
                throw new IllegalArgumentException("statDelta는 null일 수 없습니다.");
            }
            if (cashPreview == null) {
                throw new IllegalArgumentException("cashPreview는 null일 수 없습니다.");
            }
        }
    }

    public record CashPreview(
        int minimumCashDelta,
        int maximumCashDelta
    ) {

        public CashPreview {
            if (minimumCashDelta > maximumCashDelta) {
                throw new IllegalArgumentException(
                    "minimumCashDelta는 maximumCashDelta보다 클 수 없습니다."
                );
            }
        }

        public static CashPreview fixed(final int cashDelta) {
            return new CashPreview(cashDelta, cashDelta);
        }

        public boolean isRange() {
            return minimumCashDelta != maximumCashDelta;
        }
    }
}
