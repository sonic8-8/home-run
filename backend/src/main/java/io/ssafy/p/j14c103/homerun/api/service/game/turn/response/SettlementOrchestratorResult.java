package io.ssafy.p.j14c103.homerun.api.service.game.turn.response;

import io.ssafy.p.j14c103.homerun.api.service.game.port.SettlementStepExecutor;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.SettlementStepType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.SessionStatus;
import io.ssafy.p.j14c103.homerun.domain.gamesession.settlement.SettlementPhaseType;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import java.util.Map;
import lombok.Getter;

@Getter
public class SettlementOrchestratorResult {

    private final List<StepResult> stepResults;
    private final Money finalCash;
    private final Money finalStockValue;
    private final Money finalLoanBalance;
    private final Money totalAssets;
    private final Money netWorth;
    private final Map<String, Integer> aggregatedStatChanges;
    private final SessionStatus endingStatus;
    private final boolean hasEvent;
    private final boolean targetPropertyOwned;

    private SettlementOrchestratorResult(
        final List<StepResult> stepResults,
        final Money finalCash,
        final Money finalStockValue,
        final Money finalLoanBalance,
        final Money totalAssets,
        final Money netWorth,
        final Map<String, Integer> aggregatedStatChanges,
        final SessionStatus endingStatus,
        final boolean hasEvent,
        final boolean targetPropertyOwned
    ) {
        validate(
            stepResults,
            finalCash,
            finalStockValue,
            finalLoanBalance,
            totalAssets,
            netWorth,
            aggregatedStatChanges,
            endingStatus
        );
        this.stepResults = List.copyOf(stepResults);
        this.finalCash = finalCash;
        this.finalStockValue = finalStockValue;
        this.finalLoanBalance = finalLoanBalance;
        this.totalAssets = totalAssets;
        this.netWorth = netWorth;
        this.aggregatedStatChanges = Map.copyOf(aggregatedStatChanges);
        this.endingStatus = endingStatus;
        this.hasEvent = hasEvent;
        this.targetPropertyOwned = targetPropertyOwned;
    }

    public static SettlementOrchestratorResult of(
        final List<StepResult> stepResults,
        final Money finalCash,
        final Money finalStockValue,
        final Money finalLoanBalance,
        final Money totalAssets,
        final Money netWorth,
        final Map<String, Integer> aggregatedStatChanges,
        final SessionStatus endingStatus,
        final boolean hasEvent,
        final boolean targetPropertyOwned
    ) {
        return new SettlementOrchestratorResult(
            stepResults,
            finalCash,
            finalStockValue,
            finalLoanBalance,
            totalAssets,
            netWorth,
            aggregatedStatChanges,
            endingStatus,
            hasEvent,
            targetPropertyOwned
        );
    }

    private static void validate(
        final List<StepResult> stepResults,
        final Money finalCash,
        final Money finalStockValue,
        final Money finalLoanBalance,
        final Money totalAssets,
        final Money netWorth,
        final Map<String, Integer> aggregatedStatChanges,
        final SessionStatus endingStatus
    ) {
        if (stepResults == null || finalCash == null || finalStockValue == null || finalLoanBalance == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
        if (totalAssets == null || netWorth == null || aggregatedStatChanges == null || endingStatus == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    @Getter
    public static class StepResult {

        private final SettlementStepType stepType;
        private final SettlementPhaseType phaseType;
        private final String description;
        private final Money cashDelta;
        private final Money stockValueDelta;
        private final Money loanBalanceDelta;
        private final Map<String, Integer> statChanges;
        private final boolean eventTriggered;
        private final boolean targetPropertyOwned;

        private StepResult(
            final SettlementStepType stepType,
            final SettlementPhaseType phaseType,
            final String description,
            final Money cashDelta,
            final Money stockValueDelta,
            final Money loanBalanceDelta,
            final Map<String, Integer> statChanges,
            final boolean eventTriggered,
            final boolean targetPropertyOwned
        ) {
            if (stepType == null || phaseType == null || description == null || description.isBlank()) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            if (cashDelta == null || stockValueDelta == null || loanBalanceDelta == null || statChanges == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            this.stepType = stepType;
            this.phaseType = phaseType;
            this.description = description;
            this.cashDelta = cashDelta;
            this.stockValueDelta = stockValueDelta;
            this.loanBalanceDelta = loanBalanceDelta;
            this.statChanges = Map.copyOf(statChanges);
            this.eventTriggered = eventTriggered;
            this.targetPropertyOwned = targetPropertyOwned;
        }

        public static StepResult from(
            final SettlementStepType stepType,
            final SettlementStepExecutor.StepExecutionResult stepExecutionResult
        ) {
            return new StepResult(
                stepType,
                stepType.getPhaseType(),
                stepExecutionResult.getDescription(),
                stepExecutionResult.getCashDelta(),
                stepExecutionResult.getStockValueDelta(),
                stepExecutionResult.getLoanBalanceDelta(),
                stepExecutionResult.getStatChanges(),
                stepExecutionResult.isEventTriggered(),
                stepExecutionResult.isTargetPropertyOwned()
            );
        }
    }
}
