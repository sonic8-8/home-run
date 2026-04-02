package io.ssafy.p.j14c103.homerun.api.service.game.turn.request;

import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.Map;
import lombok.Getter;

@Getter
public class SettlementOrchestratorRequest {

    private final Long sessionId;
    private final Long userId;
    private final Integer nextTurnNumber;
    private final Money currentCash;
    private final Money currentStockValue;
    private final Money currentRealEstateValue;
    private final Money currentLoanBalance;
    private final Money previewCashChange;
    private final Map<String, Integer> previewStatChanges;
    private final String cycleDescription;
    private final boolean hasEventCandidate;
    private final boolean targetPropertyOwned;

    private SettlementOrchestratorRequest(
        final Long sessionId,
        final Long userId,
        final Integer nextTurnNumber,
        final Money currentCash,
        final Money currentStockValue,
        final Money currentRealEstateValue,
        final Money currentLoanBalance,
        final Money previewCashChange,
        final Map<String, Integer> previewStatChanges,
        final String cycleDescription,
        final boolean hasEventCandidate,
        final boolean targetPropertyOwned
    ) {
        validate(
            sessionId,
            userId,
            nextTurnNumber,
            currentCash,
            currentStockValue,
            currentRealEstateValue,
            currentLoanBalance,
            previewCashChange,
            previewStatChanges,
            cycleDescription
        );
        this.sessionId = sessionId;
        this.userId = userId;
        this.nextTurnNumber = nextTurnNumber;
        this.currentCash = currentCash;
        this.currentStockValue = currentStockValue;
        this.currentRealEstateValue = currentRealEstateValue;
        this.currentLoanBalance = currentLoanBalance;
        this.previewCashChange = previewCashChange;
        this.previewStatChanges = Map.copyOf(previewStatChanges);
        this.cycleDescription = cycleDescription;
        this.hasEventCandidate = hasEventCandidate;
        this.targetPropertyOwned = targetPropertyOwned;
    }

    public static SettlementOrchestratorRequest of(
        final Long sessionId,
        final Long userId,
        final Integer nextTurnNumber,
        final Money currentCash,
        final Money currentStockValue,
        final Money currentRealEstateValue,
        final Money currentLoanBalance,
        final Money previewCashChange,
        final Map<String, Integer> previewStatChanges,
        final String cycleDescription,
        final boolean hasEventCandidate,
        final boolean targetPropertyOwned
    ) {
        return new SettlementOrchestratorRequest(
            sessionId,
            userId,
            nextTurnNumber,
            currentCash,
            currentStockValue,
            currentRealEstateValue,
            currentLoanBalance,
            previewCashChange,
            previewStatChanges,
            cycleDescription,
            hasEventCandidate,
            targetPropertyOwned
        );
    }

    private static void validate(
        final Long sessionId,
        final Long userId,
        final Integer nextTurnNumber,
        final Money currentCash,
        final Money currentStockValue,
        final Money currentRealEstateValue,
        final Money currentLoanBalance,
        final Money previewCashChange,
        final Map<String, Integer> previewStatChanges,
        final String cycleDescription
    ) {
        if (sessionId == null || userId == null || nextTurnNumber == null || nextTurnNumber < 1) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
        if (currentCash == null
            || currentStockValue == null
            || currentRealEstateValue == null
            || currentLoanBalance == null
            || previewCashChange == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
        if (previewStatChanges == null || cycleDescription == null || cycleDescription.isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }
}
