package io.ssafy.p.j14c103.homerun.domain.character.career;

import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionCatalog;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;

public class NegotiationPreparationPolicy {

    private static final int MIN_STAT = 0;
    private static final int MAX_STAT = 100;
    private static final int NO_PREPARATION_BONUS_RATE = 0;
    private static final int MID_PREPARATION_BONUS_RATE = 2;
    private static final int HIGH_PREPARATION_BONUS_RATE = 5;

    private final ActionCatalog actionCatalog = new ActionCatalog();

    public PreparationResult calculate(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final List<ActionType> currentTurnActions,
        final List<ActionType> expiredTurnActions
    ) {
        validateRequest(gameCareer, gameStat, currentTurnActions, expiredTurnActions);

        final int recentStudyCount = updateCounter(
            requireNonNegativeState(gameCareer.getRecentStudyCount()),
            countStudyActions(currentTurnActions),
            countStudyActions(expiredTurnActions)
        );
        final int recentNetworkingCount = updateCounter(
            requireNonNegativeState(gameCareer.getRecentNetworkingCount()),
            countNetworkingActions(currentTurnActions),
            countNetworkingActions(expiredTurnActions)
        );
        final int preparationScore = recentStudyCount * 2
            + recentNetworkingCount * 3
            + resolveKnowledgeBonus(requireStat(gameStat.getKnowledge()));

        return PreparationResult.of(
            recentStudyCount,
            recentNetworkingCount,
            preparationScore
        );
    }

    public int resolveRaiseBonusRate(final Integer negotiationPreparationScore) {
        final int preparationScore = requireNonNegativeState(negotiationPreparationScore);
        if (preparationScore <= 5) {
            return NO_PREPARATION_BONUS_RATE;
        }
        if (preparationScore <= 10) {
            return MID_PREPARATION_BONUS_RATE;
        }

        return HIGH_PREPARATION_BONUS_RATE;
    }

    private void validateRequest(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final List<ActionType> currentTurnActions,
        final List<ActionType> expiredTurnActions
    ) {
        if (gameCareer == null || gameStat == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
        validateActionTypes(currentTurnActions);
        validateActionTypes(expiredTurnActions);
    }

    private void validateActionTypes(final List<ActionType> actionTypes) {
        if (actionTypes == null || actionTypes.stream().anyMatch(actionType -> actionType == null)) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }

    private int countStudyActions(final List<ActionType> actionTypes) {
        return (int) actionTypes.stream()
            .filter(this::isStudyCounterTarget)
            .count();
    }

    private int countNetworkingActions(final List<ActionType> actionTypes) {
        return (int) actionTypes.stream()
            .filter(this::isNetworkingCounterTarget)
            .count();
    }

    private boolean isStudyCounterTarget(final ActionType actionType) {
        return actionCatalog.getDefinition(actionType).studyCounterTarget();
    }

    private boolean isNetworkingCounterTarget(final ActionType actionType) {
        return actionCatalog.getDefinition(actionType).networkingCounterTarget();
    }

    private int updateCounter(
        final int previousCount,
        final int currentTurnCount,
        final int expiredTurnCount
    ) {
        final int updatedCount = previousCount + currentTurnCount - expiredTurnCount;
        if (updatedCount < 0) {
            throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
        }

        return updatedCount;
    }

    private int resolveKnowledgeBonus(final int knowledge) {
        if (knowledge <= 30) {
            return NO_PREPARATION_BONUS_RATE;
        }
        if (knowledge <= 60) {
            return MID_PREPARATION_BONUS_RATE;
        }

        return HIGH_PREPARATION_BONUS_RATE;
    }

    private int requireNonNegativeState(final Integer value) {
        if (value == null) {
            throw new HomerunException(ErrorCode.CHARACTER_STATE_UNINITIALIZED);
        }
        if (value < 0) {
            throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
        }

        return value;
    }

    private int requireStat(final Integer stat) {
        if (stat == null) {
            throw new HomerunException(ErrorCode.CHARACTER_STATE_UNINITIALIZED);
        }
        if (stat < MIN_STAT || stat > MAX_STAT) {
            throw new HomerunException(ErrorCode.CHARACTER_STAT_INVALID);
        }

        return stat;
    }

    public record PreparationResult(
        int recentStudyCount,
        int recentNetworkingCount,
        int negotiationPreparationScore
    ) {

        public PreparationResult {
            validateNonNegative(recentStudyCount);
            validateNonNegative(recentNetworkingCount);
            validateNonNegative(negotiationPreparationScore);
        }

        public static PreparationResult of(
            final int recentStudyCount,
            final int recentNetworkingCount,
            final int negotiationPreparationScore
        ) {
            return new PreparationResult(
                recentStudyCount,
                recentNetworkingCount,
                negotiationPreparationScore
            );
        }

        private static void validateNonNegative(final int value) {
            if (value < 0) {
                throw new HomerunException(ErrorCode.CHARACTER_POLICY_INVALID);
            }
        }
    }
}
