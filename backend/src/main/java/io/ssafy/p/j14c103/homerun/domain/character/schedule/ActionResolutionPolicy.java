package io.ssafy.p.j14c103.homerun.domain.character.schedule;

import io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionCatalog.ActionDefinition;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionCatalog.ActionStatDelta;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;

public class ActionResolutionPolicy {

    private final ActionCatalog actionCatalog;
    private final SideJobIncomePolicy sideJobIncomePolicy;

    public ActionResolutionPolicy() {
        this(new ActionCatalog(), new SideJobIncomePolicy());
    }

    ActionResolutionPolicy(
        final ActionCatalog actionCatalog,
        final SideJobIncomePolicy sideJobIncomePolicy
    ) {
        validatePolicyNotNull(actionCatalog);
        validatePolicyNotNull(sideJobIncomePolicy);

        this.actionCatalog = actionCatalog;
        this.sideJobIncomePolicy = sideJobIncomePolicy;
    }

    public List<ResolvedAction> getPreviewActions(final int knowledge) {
        return actionCatalog.getDefinitions().stream()
            .map(actionDefinition -> resolvePreviewAction(actionDefinition, knowledge))
            .toList();
    }

    public ResolvedAction getPreviewAction(final ActionType actionType, final int knowledge) {
        return resolvePreviewAction(actionCatalog.getDefinition(actionType), knowledge);
    }

    public ResolvedAction getActualAction(final ActionType actionType, final int knowledge) {
        return resolveActualAction(actionCatalog.getDefinition(actionType), knowledge);
    }

    private ResolvedAction resolvePreviewAction(
        final ActionDefinition actionDefinition,
        final int knowledge
    ) {
        final CashPreview cashPreview = resolvePreviewCashPreview(actionDefinition, knowledge);

        return new ResolvedAction(
            actionDefinition.actionType(),
            actionDefinition.category(),
            actionDefinition.label(),
            actionDefinition.iconKey(),
            actionDefinition.statDelta(),
            cashPreview,
            cashPreview.minimumCashDelta()
        );
    }

    private ResolvedAction resolveActualAction(
        final ActionDefinition actionDefinition,
        final int knowledge
    ) {
        final int cashDelta = resolveActualCashDelta(actionDefinition, knowledge);

        return new ResolvedAction(
            actionDefinition.actionType(),
            actionDefinition.category(),
            actionDefinition.label(),
            actionDefinition.iconKey(),
            actionDefinition.statDelta(),
            CashPreview.fixed(cashDelta),
            cashDelta
        );
    }

    private CashPreview resolvePreviewCashPreview(
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

    private void validatePolicyNotNull(final Object value) {
        if (value == null) {
            throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
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
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
            if (category == null) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
            if (label == null || label.isBlank()) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
            if (iconKey == null || iconKey.isBlank()) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
            if (statDelta == null) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
            if (cashPreview == null) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
        }
    }

    public record CashPreview(
        int minimumCashDelta,
        int maximumCashDelta
    ) {

        public CashPreview {
            if (minimumCashDelta > maximumCashDelta) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
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
