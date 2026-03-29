package io.ssafy.p.j14c103.homerun.api.service.game.port;

import io.ssafy.p.j14c103.homerun.api.service.game.turn.SettlementStepType;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.Map;
import lombok.Getter;

public interface SettlementStepExecutor {

    StepExecutionResult execute(SettlementStepExecutionContext context);

    @Getter
    class SettlementStepExecutionContext {

        private final Long sessionId;
        private final Integer nextTurnNumber;
        private final SettlementStepType stepType;
        private final Money currentCash;
        private final Money currentStockValue;
        private final Money currentLoanBalance;
        private final Map<String, Integer> currentStatChanges;
        private final boolean targetPropertyOwned;

        private SettlementStepExecutionContext(
            final Long sessionId,
            final Integer nextTurnNumber,
            final SettlementStepType stepType,
            final Money currentCash,
            final Money currentStockValue,
            final Money currentLoanBalance,
            final Map<String, Integer> currentStatChanges,
            final boolean targetPropertyOwned
        ) {
            validate(
                sessionId,
                nextTurnNumber,
                stepType,
                currentCash,
                currentStockValue,
                currentLoanBalance,
                currentStatChanges
            );
            this.sessionId = sessionId;
            this.nextTurnNumber = nextTurnNumber;
            this.stepType = stepType;
            this.currentCash = currentCash;
            this.currentStockValue = currentStockValue;
            this.currentLoanBalance = currentLoanBalance;
            this.currentStatChanges = Map.copyOf(currentStatChanges);
            this.targetPropertyOwned = targetPropertyOwned;
        }

        public static SettlementStepExecutionContext of(
            final Long sessionId,
            final Integer nextTurnNumber,
            final SettlementStepType stepType,
            final Money currentCash,
            final Money currentStockValue,
            final Money currentLoanBalance,
            final Map<String, Integer> currentStatChanges,
            final boolean targetPropertyOwned
        ) {
            return new SettlementStepExecutionContext(
                sessionId,
                nextTurnNumber,
                stepType,
                currentCash,
                currentStockValue,
                currentLoanBalance,
                currentStatChanges,
                targetPropertyOwned
            );
        }

        private static void validate(
            final Long sessionId,
            final Integer nextTurnNumber,
            final SettlementStepType stepType,
            final Money currentCash,
            final Money currentStockValue,
            final Money currentLoanBalance,
            final Map<String, Integer> currentStatChanges
        ) {
            if (sessionId == null || nextTurnNumber == null || nextTurnNumber < 1 || stepType == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            if (currentCash == null || currentStockValue == null || currentLoanBalance == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            if (currentStatChanges == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
        }
    }

    @Getter
    class StepExecutionResult {

        private final String description;
        private final Money cashDelta;
        private final Money stockValueDelta;
        private final Money loanBalanceDelta;
        private final Map<String, Integer> statChanges;
        private final boolean eventTriggered;
        private final boolean targetPropertyOwned;

        private StepExecutionResult(
            final String description,
            final Money cashDelta,
            final Money stockValueDelta,
            final Money loanBalanceDelta,
            final Map<String, Integer> statChanges,
            final boolean eventTriggered,
            final boolean targetPropertyOwned
        ) {
            validate(description, cashDelta, stockValueDelta, loanBalanceDelta, statChanges);
            this.description = description;
            this.cashDelta = cashDelta;
            this.stockValueDelta = stockValueDelta;
            this.loanBalanceDelta = loanBalanceDelta;
            this.statChanges = Map.copyOf(statChanges);
            this.eventTriggered = eventTriggered;
            this.targetPropertyOwned = targetPropertyOwned;
        }

        public static StepExecutionResult of(
            final String description,
            final Money cashDelta,
            final Money stockValueDelta,
            final Money loanBalanceDelta,
            final Map<String, Integer> statChanges,
            final boolean eventTriggered,
            final boolean targetPropertyOwned
        ) {
            return new StepExecutionResult(
                description,
                cashDelta,
                stockValueDelta,
                loanBalanceDelta,
                statChanges,
                eventTriggered,
                targetPropertyOwned
            );
        }

        private static void validate(
            final String description,
            final Money cashDelta,
            final Money stockValueDelta,
            final Money loanBalanceDelta,
            final Map<String, Integer> statChanges
        ) {
            if (description == null || description.isBlank()) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            if (cashDelta == null || stockValueDelta == null || loanBalanceDelta == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            if (statChanges == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
        }
    }
}
