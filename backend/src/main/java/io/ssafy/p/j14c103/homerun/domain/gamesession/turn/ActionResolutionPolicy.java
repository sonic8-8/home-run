package io.ssafy.p.j14c103.homerun.domain.gamesession.turn;

import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ActionResolutionPolicy {

    private final ActionCatalog actionCatalog;
    private final SideJobIncomePolicy sideJobIncomePolicy;

    public ActionResolutionPolicy(
        final ActionCatalog actionCatalog,
        final SideJobIncomePolicy sideJobIncomePolicy
    ) {
        validatePolicy(actionCatalog);
        validatePolicy(sideJobIncomePolicy);

        this.actionCatalog = actionCatalog;
        this.sideJobIncomePolicy = sideJobIncomePolicy;
    }

    public List<ResolvedAction> resolvePreviewActions(
        final GameStat gameStat,
        final int currentTurn
    ) {
        validateGameStat(gameStat);
        validateTurn(currentTurn);

        return actionCatalog.getDefinitions().stream()
            .map(definition -> resolvePreviewAction(definition, gameStat, currentTurn))
            .toList();
    }

    public ResolvedAction resolvePreviewAction(
        final ActionType actionType,
        final GameStat gameStat,
        final int currentTurn
    ) {
        return resolvePreviewAction(actionCatalog.getDefinition(actionType), gameStat, currentTurn);
    }

    public ResolvedAction resolvePreviewAction(
        final ActionCatalog.ActionDefinition definition,
        final GameStat gameStat,
        final int currentTurn
    ) {
        validateDefinition(definition);
        validateGameStat(gameStat);
        validateTurn(currentTurn);

        final ActionAvailability availability = resolveAvailability(
            definition.actionType(),
            gameStat,
            currentTurn
        );

        return ResolvedAction.of(
            definition.actionType(),
            definition.category(),
            definition.label(),
            definition.iconKey(),
            definition.statDelta(),
            resolveExpectedCashDelta(definition, gameStat.getKnowledge()),
            availability.disabled(),
            availability.forced(),
            definition.longTermEffects()
        );
    }

    private CashPreview resolveExpectedCashDelta(
        final ActionCatalog.ActionDefinition definition,
        final Integer knowledge
    ) {
        if (!definition.knowledgeBasedCash()) {
            return CashPreview.fixed(definition.fixedCashDelta());
        }

        final SideJobIncomePolicy.SideJobIncomePreview preview = sideJobIncomePolicy.previewIncome(
            knowledge
        );
        return CashPreview.of(preview.minAmount(), preview.maxAmount(), preview.rangePreview());
    }

    private ActionAvailability resolveAvailability(
        final ActionType actionType,
        final GameStat gameStat,
        final int currentTurn
    ) {
        if (gameStat.isHospitalizedAt(currentTurn)) {
            if (actionType == ActionType.REST) {
                return ActionAvailability.asForced();
            }
            return ActionAvailability.asDisabled();
        }

        if (Boolean.TRUE.equals(gameStat.getBurnout()) && actionType == ActionType.REST) {
            return ActionAvailability.asForced();
        }

        return ActionAvailability.asAvailable();
    }

    private void validateDefinition(final ActionCatalog.ActionDefinition definition) {
        if (definition == null) {
            throw new HomerunException(ErrorCode.SCHEDULE_ACTION_CATALOG_INVALID);
        }
    }

    private void validateGameStat(final GameStat gameStat) {
        if (gameStat == null) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private void validateTurn(final int currentTurn) {
        if (currentTurn < 0) {
            throw new HomerunException(ErrorCode.CHARACTER_TURN_INVALID);
        }
    }

    private void validatePolicy(final Object policy) {
        if (policy == null) {
            throw new HomerunException(ErrorCode.SCHEDULE_ACTION_CATALOG_INVALID);
        }
    }

    public record ResolvedAction(
        ActionType actionType,
        ActionCategory category,
        String label,
        String iconKey,
        ActionCatalog.ActionStatDelta statDelta,
        CashPreview cashPreview,
        boolean disabled,
        boolean forced,
        Set<ActionLongTermEffect> longTermEffects
    ) {

        public ResolvedAction {
            validateActionType(actionType);
            validateCategory(category);
            validateText(label);
            validateText(iconKey);
            validateStatDelta(statDelta);
            validateCashPreview(cashPreview);
            validateAvailability(disabled, forced);
            validateLongTermEffects(longTermEffects);
            longTermEffects = Set.copyOf(longTermEffects);
        }

        public static ResolvedAction of(
            final ActionType actionType,
            final ActionCategory category,
            final String label,
            final String iconKey,
            final ActionCatalog.ActionStatDelta statDelta,
            final CashPreview cashPreview,
            final boolean disabled,
            final boolean forced,
            final Set<ActionLongTermEffect> longTermEffects
        ) {
            return new ResolvedAction(
                actionType,
                category,
                label,
                iconKey,
                statDelta,
                cashPreview,
                disabled,
                forced,
                longTermEffects
            );
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

        private static void validateStatDelta(final ActionCatalog.ActionStatDelta statDelta) {
            if (statDelta == null) {
                throw new HomerunException(ErrorCode.SCHEDULE_ACTION_CATALOG_INVALID);
            }
        }

        private static void validateCashPreview(final CashPreview cashPreview) {
            if (cashPreview == null) {
                throw new HomerunException(ErrorCode.SCHEDULE_ACTION_CATALOG_INVALID);
            }
        }

        private static void validateAvailability(final boolean disabled, final boolean forced) {
            if (disabled && forced) {
                throw new HomerunException(ErrorCode.SCHEDULE_ACTION_CATALOG_INVALID);
            }
        }

        private static void validateLongTermEffects(
            final Set<ActionLongTermEffect> longTermEffects
        ) {
            if (longTermEffects == null) {
                throw new HomerunException(ErrorCode.SCHEDULE_ACTION_CATALOG_INVALID);
            }
        }
    }

    public record CashPreview(
        int minAmount,
        int maxAmount,
        boolean rangePreview
    ) {

        public CashPreview {
            if (minAmount > maxAmount) {
                throw new HomerunException(ErrorCode.SCHEDULE_ACTION_CATALOG_INVALID);
            }

            if (!rangePreview && minAmount != maxAmount) {
                throw new HomerunException(ErrorCode.SCHEDULE_ACTION_CATALOG_INVALID);
            }
        }

        public static CashPreview fixed(final int amount) {
            return new CashPreview(amount, amount, false);
        }

        public static CashPreview of(
            final int minAmount,
            final int maxAmount,
            final boolean rangePreview
        ) {
            return new CashPreview(minAmount, maxAmount, rangePreview);
        }
    }

    private record ActionAvailability(
        boolean disabled,
        boolean forced
    ) {

        private static ActionAvailability asAvailable() {
            return new ActionAvailability(false, false);
        }

        private static ActionAvailability asDisabled() {
            return new ActionAvailability(true, false);
        }

        private static ActionAvailability asForced() {
            return new ActionAvailability(false, true);
        }
    }
}
