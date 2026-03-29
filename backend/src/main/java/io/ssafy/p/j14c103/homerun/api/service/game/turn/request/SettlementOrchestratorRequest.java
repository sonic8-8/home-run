package io.ssafy.p.j14c103.homerun.api.service.game.turn.request;

import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.Getter;

@Getter
public class SettlementOrchestratorRequest {

    private final Long sessionId;
    private final Integer nextTurnNumber;
    private final Money currentCash;
    private final Money currentStockValue;
    private final Money currentLoanBalance;
    private final boolean targetPropertyOwned;

    private SettlementOrchestratorRequest(
        final Long sessionId,
        final Integer nextTurnNumber,
        final Money currentCash,
        final Money currentStockValue,
        final Money currentLoanBalance,
        final boolean targetPropertyOwned
    ) {
        validate(sessionId, nextTurnNumber, currentCash, currentStockValue, currentLoanBalance);
        this.sessionId = sessionId;
        this.nextTurnNumber = nextTurnNumber;
        this.currentCash = currentCash;
        this.currentStockValue = currentStockValue;
        this.currentLoanBalance = currentLoanBalance;
        this.targetPropertyOwned = targetPropertyOwned;
    }

    public static SettlementOrchestratorRequest of(
        final Long sessionId,
        final Integer nextTurnNumber,
        final Money currentCash,
        final Money currentStockValue,
        final Money currentLoanBalance,
        final boolean targetPropertyOwned
    ) {
        return new SettlementOrchestratorRequest(
            sessionId,
            nextTurnNumber,
            currentCash,
            currentStockValue,
            currentLoanBalance,
            targetPropertyOwned
        );
    }

    private static void validate(
        final Long sessionId,
        final Integer nextTurnNumber,
        final Money currentCash,
        final Money currentStockValue,
        final Money currentLoanBalance
    ) {
        if (sessionId == null || nextTurnNumber == null || nextTurnNumber < 1) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
        if (currentCash == null || currentStockValue == null || currentLoanBalance == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }
}
