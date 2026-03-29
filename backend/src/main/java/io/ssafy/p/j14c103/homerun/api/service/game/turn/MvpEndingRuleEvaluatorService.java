package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import io.ssafy.p.j14c103.homerun.api.service.game.port.EndingRuleEvaluator;
import io.ssafy.p.j14c103.homerun.domain.gamesession.SessionStatus;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import org.springframework.stereotype.Component;

@Component
public class MvpEndingRuleEvaluatorService implements EndingRuleEvaluator {

    private static final int TIMEOUT_TURN = 360;

    @Override
    public EndingEvaluation evaluate(final EndingRuleContext context) {
        final Money netWorth = context.getCashBalance()
            .add(context.getStockValue())
            .subtract(context.getLoanBalance());

        if (context.isTargetPropertyOwned()) {
            return EndingEvaluation.of(netWorth, SessionStatus.CLEAR);
        }
        if (netWorth.getAmount().signum() <= 0) {
            return EndingEvaluation.of(netWorth, SessionStatus.BANKRUPT);
        }
        if (context.getNextTurnNumber() >= TIMEOUT_TURN) {
            return EndingEvaluation.of(netWorth, SessionStatus.TIMEOUT);
        }
        return EndingEvaluation.of(netWorth, SessionStatus.IN_PROGRESS);
    }
}
