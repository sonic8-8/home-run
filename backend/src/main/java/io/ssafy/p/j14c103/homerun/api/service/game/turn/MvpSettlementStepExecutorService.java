package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import io.ssafy.p.j14c103.homerun.api.service.game.port.SettlementStepExecutor;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MvpSettlementStepExecutorService implements SettlementStepExecutor {

    @Override
    public StepExecutionResult execute(final SettlementStepExecutionContext context) {
        return StepExecutionResult.of(
            describe(context),
            resolveCashDelta(context),
            Money.zero(),
            Money.zero(),
            resolveStatChanges(context),
            resolveEventTriggered(context),
            context.isTargetPropertyOwned()
        );
    }

    private String describe(final SettlementStepExecutionContext context) {
        return switch (context.getStepType()) {
            case MARKET_CYCLE_UPDATE -> context.getCycleDescription();
            case MARKET_STOCK_PRICE_REFRESH -> "주가 반영 정산을 대기한다";
            case MARKET_WORLD_SIGNAL_REFRESH -> "월드 신호 정산을 대기한다";
            case INCOME_SALARY_SETTLEMENT -> "턴 행동 결과를 반영한다";
            case INCOME_SIDE_JOB_SETTLEMENT -> "부업 수익 정산을 대기한다";
            case INCOME_FIXED_EXPENSE_SETTLEMENT -> "고정 지출 정산을 대기한다";
            case INCOME_HOUSING_COST_SETTLEMENT -> "주거 비용 정산을 대기한다";
            case INCOME_CARD_BILL_SETTLEMENT -> "카드 대금 정산을 대기한다";
            case INCOME_STOCK_ORDER_SETTLEMENT -> "주문 체결 정산을 대기한다";
            case INCOME_LOAN_INTEREST_SETTLEMENT -> "대출 이자 정산을 대기한다";
            case STATUS_CHARACTER_UPDATE -> "캐릭터 상태 갱신을 대기한다";
            case STATUS_PENDING_EVENT_PREPARE -> "이벤트 준비 결과를 반영한다";
            case STATUS_ENDING_CHECKPOINT -> "엔딩 체크포인트 정산을 대기한다";
        };
    }

    private Money resolveCashDelta(final SettlementStepExecutionContext context) {
        if (context.getStepType() == SettlementStepType.INCOME_SALARY_SETTLEMENT) {
            return context.getPreviewCashChange();
        }
        return Money.zero();
    }

    private Map<String, Integer> resolveStatChanges(final SettlementStepExecutionContext context) {
        if (context.getStepType() == SettlementStepType.STATUS_CHARACTER_UPDATE) {
            return context.getPreviewStatChanges();
        }
        return Map.of();
    }

    private boolean resolveEventTriggered(final SettlementStepExecutionContext context) {
        if (context.getStepType() == SettlementStepType.STATUS_PENDING_EVENT_PREPARE) {
            return context.isHasEventCandidate();
        }
        return false;
    }
}
