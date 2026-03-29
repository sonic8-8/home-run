package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import io.ssafy.p.j14c103.homerun.api.service.game.port.EndingRuleEvaluator;
import io.ssafy.p.j14c103.homerun.api.service.game.port.SettlementStepExecutor;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.request.SettlementOrchestratorRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.response.SettlementOrchestratorResult;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementOrchestrator {

    private final SettlementStepExecutor settlementStepExecutor;
    private final EndingRuleEvaluator endingRuleEvaluator;

    public SettlementOrchestratorResult orchestrate(final SettlementOrchestratorRequest request) {
        Money currentCash = request.getCurrentCash();
        Money currentStockValue = request.getCurrentStockValue();
        Money currentLoanBalance = request.getCurrentLoanBalance();
        boolean targetPropertyOwned = request.isTargetPropertyOwned();
        boolean hasEvent = false;
        final Map<String, Integer> aggregatedStatChanges = new LinkedHashMap<>();
        final List<SettlementOrchestratorResult.StepResult> stepResults = new ArrayList<>();

        for (SettlementStepType stepType : SettlementStepType.orderedValues()) {
            final SettlementStepExecutor.StepExecutionResult stepExecutionResult =
                settlementStepExecutor.execute(
                    SettlementStepExecutor.SettlementStepExecutionContext.of(
                        request.getSessionId(),
                        request.getNextTurnNumber(),
                        stepType,
                        currentCash,
                        currentStockValue,
                        currentLoanBalance,
                        aggregatedStatChanges,
                        targetPropertyOwned
                    )
                );

            currentCash = currentCash.add(stepExecutionResult.getCashDelta());
            currentStockValue = currentStockValue.add(stepExecutionResult.getStockValueDelta());
            currentLoanBalance = currentLoanBalance.add(stepExecutionResult.getLoanBalanceDelta());
            mergeStatChanges(aggregatedStatChanges, stepExecutionResult.getStatChanges());
            hasEvent = hasEvent || stepExecutionResult.isEventTriggered();
            targetPropertyOwned = targetPropertyOwned || stepExecutionResult.isTargetPropertyOwned();
            stepResults.add(SettlementOrchestratorResult.StepResult.from(stepType, stepExecutionResult));
        }

        final Money totalAssets = currentCash.add(currentStockValue);
        final EndingRuleEvaluator.EndingEvaluation endingEvaluation = endingRuleEvaluator.evaluate(
            EndingRuleEvaluator.EndingRuleContext.of(
                request.getNextTurnNumber(),
                currentCash,
                currentStockValue,
                currentLoanBalance,
                targetPropertyOwned
            )
        );

        return SettlementOrchestratorResult.of(
            stepResults,
            currentCash,
            currentStockValue,
            currentLoanBalance,
            totalAssets,
            endingEvaluation.getNetWorth(),
            aggregatedStatChanges,
            endingEvaluation.getSessionStatus(),
            hasEvent,
            targetPropertyOwned
        );
    }

    private void mergeStatChanges(
        final Map<String, Integer> aggregatedStatChanges,
        final Map<String, Integer> stepStatChanges
    ) {
        stepStatChanges.forEach((statKey, delta) ->
            aggregatedStatChanges.merge(statKey, delta, Integer::sum)
        );
    }
}
