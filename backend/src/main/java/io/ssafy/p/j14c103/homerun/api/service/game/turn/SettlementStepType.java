package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import io.ssafy.p.j14c103.homerun.domain.gamesession.settlement.SettlementPhaseType;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SettlementStepType {

    MARKET_CYCLE_UPDATE(1, SettlementPhaseType.MARKET_UPDATE),
    MARKET_STOCK_PRICE_REFRESH(2, SettlementPhaseType.MARKET_UPDATE),
    MARKET_WORLD_SIGNAL_REFRESH(3, SettlementPhaseType.MARKET_UPDATE),
    INCOME_SALARY_SETTLEMENT(4, SettlementPhaseType.INCOME_EXPENSE),
    INCOME_SIDE_JOB_SETTLEMENT(5, SettlementPhaseType.INCOME_EXPENSE),
    INCOME_FIXED_EXPENSE_SETTLEMENT(6, SettlementPhaseType.INCOME_EXPENSE),
    INCOME_HOUSING_COST_SETTLEMENT(7, SettlementPhaseType.INCOME_EXPENSE),
    INCOME_CARD_BILL_SETTLEMENT(8, SettlementPhaseType.INCOME_EXPENSE),
    INCOME_STOCK_ORDER_SETTLEMENT(9, SettlementPhaseType.INCOME_EXPENSE),
    INCOME_LOAN_INTEREST_SETTLEMENT(10, SettlementPhaseType.INCOME_EXPENSE),
    STATUS_CHARACTER_UPDATE(11, SettlementPhaseType.STATUS_UPDATE),
    STATUS_PENDING_EVENT_PREPARE(12, SettlementPhaseType.STATUS_UPDATE),
    STATUS_ENDING_CHECKPOINT(13, SettlementPhaseType.STATUS_UPDATE);

    private final int order;
    private final SettlementPhaseType phaseType;

    public static List<SettlementStepType> orderedValues() {
        return Arrays.stream(values())
            .sorted(Comparator.comparingInt(SettlementStepType::getOrder))
            .toList();
    }
}
