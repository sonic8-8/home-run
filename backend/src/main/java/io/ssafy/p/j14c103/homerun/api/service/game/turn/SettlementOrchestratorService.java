package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.ssafy.p.j14c103.homerun.api.service.game.port.EndingRuleEvaluator;
import io.ssafy.p.j14c103.homerun.api.service.game.port.SettlementStepExecutor;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.request.SettlementOrchestratorRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.response.SettlementOrchestratorResult;
import io.ssafy.p.j14c103.homerun.domain.gamesession.settlement.SettlementPhaseType;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementOrchestratorService {

    private static final String SETTLEMENT_PHASE_DURATION = "homerun.settlement.phase.duration";
    private static final String PHASE_TAG = "phase";

    private final SettlementStepExecutor settlementStepExecutor;
    private final EndingRuleEvaluator endingRuleEvaluator;
    private final MeterRegistry meterRegistry;

    public SettlementOrchestratorResult orchestrate(final SettlementOrchestratorRequest request) {
        Money currentCash = request.getCurrentCash();
        Money currentStockValue = request.getCurrentStockValue();
        Money currentLoanBalance = request.getCurrentLoanBalance();
        boolean targetPropertyOwned = request.isTargetPropertyOwned();
        boolean hasEvent = false;
        final Map<String, Integer> aggregatedStatChanges = new LinkedHashMap<>();
        final List<SettlementOrchestratorResult.StepResult> stepResults = new ArrayList<>();
        SettlementPhaseType currentPhase = null;
        Timer.Sample phaseSample = null;

        try {
            for (SettlementStepType stepType : SettlementStepType.orderedValues()) {
                phaseSample = advancePhaseSample(stepType.getPhaseType(), currentPhase, phaseSample);
                currentPhase = stepType.getPhaseType();
                final SettlementStepExecutor.StepExecutionResult stepExecutionResult =
                    settlementStepExecutor.execute(
                        SettlementStepExecutor.SettlementStepExecutionContext.of(
                            request.getSessionId(),
                            request.getUserId(),
                            request.getNextTurnNumber(),
                            stepType,
                            currentCash,
                            currentStockValue,
                            currentLoanBalance,
                            request.getPreviewCashChange(),
                            request.getPreviewStatChanges(),
                            request.getCycleDescription(),
                            request.isHasEventCandidate(),
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
        } finally {
            stopPhaseSample(currentPhase, phaseSample);
        }

        final Money totalAssets = currentCash.add(currentStockValue);
        final EndingRuleEvaluator.EndingEvaluation endingEvaluation = endingRuleEvaluator.evaluate(
            EndingRuleEvaluator.EndingRuleContext.of(
                request.getNextTurnNumber(),
                currentCash,
                currentStockValue,
                request.getCurrentRealEstateValue(),
                currentLoanBalance,
                targetPropertyOwned,
                request.isForeclosureTriggered()
            )
        );

        final Money totalAssetsWithRealEstate = totalAssets.add(request.getCurrentRealEstateValue());

        return SettlementOrchestratorResult.of(
            stepResults,
            currentCash,
            currentStockValue,
            currentLoanBalance,
            totalAssetsWithRealEstate,
            endingEvaluation.getNetWorth(),
            aggregatedStatChanges,
            endingEvaluation.getSessionStatus(),
            hasEvent,
            targetPropertyOwned
        );
    }

    private Timer.Sample advancePhaseSample(
        final SettlementPhaseType nextPhase,
        final SettlementPhaseType currentPhase,
        final Timer.Sample currentSample
    ) {
        if (nextPhase == currentPhase) {
            return currentSample;
        }

        stopPhaseSample(currentPhase, currentSample);
        return Timer.start(meterRegistry);
    }

    private void stopPhaseSample(
        final SettlementPhaseType phaseType,
        final Timer.Sample sample
    ) {
        if (phaseType == null || sample == null) {
            return;
        }
        sample.stop(settlementPhaseTimer(phaseType));
    }

    private Timer settlementPhaseTimer(final SettlementPhaseType phaseType) {
        return Timer.builder(SETTLEMENT_PHASE_DURATION)
            .tag(PHASE_TAG, phaseType.name())
            .register(meterRegistry);
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
