package io.ssafy.p.j14c103.homerun.api.service.game.port;

import io.ssafy.p.j14c103.homerun.domain.gamesession.SessionStatus;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.Getter;

public interface EndingRuleEvaluator {

    EndingEvaluation evaluate(EndingRuleContext context);

    @Getter
    class EndingRuleContext {

        private final Integer nextTurnNumber;
        private final Money cashBalance;
        private final Money stockValue;
        private final Money realEstateValue;
        private final Money loanBalance;
        private final boolean targetPropertyOwned;

        private EndingRuleContext(
            final Integer nextTurnNumber,
            final Money cashBalance,
            final Money stockValue,
            final Money realEstateValue,
            final Money loanBalance,
            final boolean targetPropertyOwned
        ) {
            validate(nextTurnNumber, cashBalance, stockValue, realEstateValue, loanBalance);
            this.nextTurnNumber = nextTurnNumber;
            this.cashBalance = cashBalance;
            this.stockValue = stockValue;
            this.realEstateValue = realEstateValue;
            this.loanBalance = loanBalance;
            this.targetPropertyOwned = targetPropertyOwned;
        }

        public static EndingRuleContext of(
            final Integer nextTurnNumber,
            final Money cashBalance,
            final Money stockValue,
            final Money realEstateValue,
            final Money loanBalance,
            final boolean targetPropertyOwned
        ) {
            return new EndingRuleContext(
                nextTurnNumber,
                cashBalance,
                stockValue,
                realEstateValue,
                loanBalance,
                targetPropertyOwned
            );
        }

        private static void validate(
            final Integer nextTurnNumber,
            final Money cashBalance,
            final Money stockValue,
            final Money realEstateValue,
            final Money loanBalance
        ) {
            if (nextTurnNumber == null || nextTurnNumber < 1) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            if (cashBalance == null
                || stockValue == null
                || realEstateValue == null
                || loanBalance == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
        }
    }

    @Getter
    class EndingEvaluation {

        private final Money netWorth;
        private final SessionStatus sessionStatus;

        private EndingEvaluation(
            final Money netWorth,
            final SessionStatus sessionStatus
        ) {
            if (netWorth == null || sessionStatus == null) {
                throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
            }
            this.netWorth = netWorth;
            this.sessionStatus = sessionStatus;
        }

        public static EndingEvaluation of(
            final Money netWorth,
            final SessionStatus sessionStatus
        ) {
            return new EndingEvaluation(netWorth, sessionStatus);
        }
    }
}
