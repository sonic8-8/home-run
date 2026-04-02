package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import io.ssafy.p.j14c103.homerun.api.service.game.loan.LoanService;
import io.ssafy.p.j14c103.homerun.api.service.game.port.SettlementStepExecutor;
import io.ssafy.p.j14c103.homerun.api.service.game.stock.StockTradingService;
import io.ssafy.p.j14c103.homerun.domain.character.EmploymentStatus;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareerRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.UnemploymentBenefitPolicy;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousing;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousingRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.GameLoan;
import io.ssafy.p.j14c103.homerun.domain.gamesession.loan.GameLoanRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.GameStockMarketState;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.OrderStatus;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.OrderType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockOrder;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockOrderRepository;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetCardSpendRepository;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetProfile;
import io.ssafy.p.j14c103.homerun.domain.user.UserAssetProfileRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MvpSettlementStepExecutorService implements SettlementStepExecutor {

    private static final int MONTHS_PER_YEAR = 12;

    private final GameCareerRepository gameCareerRepository;
    private final GameSessionRepository gameSessionRepository;
    private final UserAssetProfileRepository userAssetProfileRepository;
    private final UserAssetCardSpendRepository userAssetCardSpendRepository;
    private final GameHousingRepository gameHousingRepository;
    private final GameLoanRepository gameLoanRepository;
    private final StockOrderRepository stockOrderRepository;
    private final StockTradingService stockTradingService;
    private final LoanService loanService;
    private final UnemploymentBenefitPolicy unemploymentBenefitPolicy = new UnemploymentBenefitPolicy();

    @Override
    public StepExecutionResult execute(final SettlementStepExecutionContext context) {
        return switch (context.getStepType()) {
            case MARKET_CYCLE_UPDATE -> fixedResult(
                context.getCycleDescription(),
                Money.zero(),
                Money.zero(),
                Money.zero(),
                Map.of(),
                false,
                context.isTargetPropertyOwned()
            );
            case MARKET_STOCK_PRICE_REFRESH -> fixedResult(
                "현재 주식 평가액 " + formatAmount(context.getCurrentStockValue()) + "원을 기준으로 정산한다.",
                Money.zero(),
                Money.zero(),
                Money.zero(),
                Map.of(),
                false,
                context.isTargetPropertyOwned()
            );
            case MARKET_WORLD_SIGNAL_REFRESH -> fixedResult(
                "월드 신호와 경기 정보를 이번 턴 정산에 반영한다.",
                Money.zero(),
                Money.zero(),
                Money.zero(),
                Map.of(),
                false,
                context.isTargetPropertyOwned()
            );
            case INCOME_SALARY_SETTLEMENT -> settleSalary(context);
            case INCOME_SIDE_JOB_SETTLEMENT -> settlePreviewCash(context);
            case INCOME_FIXED_EXPENSE_SETTLEMENT -> settleFixedExpense(context);
            case INCOME_HOUSING_COST_SETTLEMENT -> settleHousingCost(context);
            case INCOME_CARD_BILL_SETTLEMENT -> settleCardBill(context);
            case INCOME_STOCK_ORDER_SETTLEMENT -> settleStockOrders(context);
            case INCOME_LOAN_INTEREST_SETTLEMENT -> settleLoanInterest(context);
            case STATUS_CHARACTER_UPDATE -> fixedResult(
                "턴 행동에 따른 캐릭터 상태 변화를 반영한다.",
                Money.zero(),
                Money.zero(),
                Money.zero(),
                context.getPreviewStatChanges(),
                false,
                context.isTargetPropertyOwned()
            );
            case STATUS_PENDING_EVENT_PREPARE -> fixedResult(
                context.isHasEventCandidate()
                    ? "이번 턴에 처리할 이벤트 후보를 준비한다."
                    : "이번 턴에 준비할 이벤트 후보가 없다.",
                Money.zero(),
                Money.zero(),
                Money.zero(),
                Map.of(),
                context.isHasEventCandidate(),
                context.isTargetPropertyOwned()
            );
            case STATUS_ENDING_CHECKPOINT -> fixedResult(
                "이번 턴 종료 조건을 점검한다.",
                Money.zero(),
                Money.zero(),
                Money.zero(),
                Map.of(),
                false,
                context.isTargetPropertyOwned()
            );
        };
    }

    private StepExecutionResult settleSalary(final SettlementStepExecutionContext context) {
        final GameCareer gameCareer = gameCareerRepository.findById(toGameId(context.getSessionId()))
            .orElse(null);
        final UserAssetProfile userAssetProfile = isMyDataSession(context.getSessionId())
            ? userAssetProfileRepository.findById(context.getUserId()).orElse(null)
            : null;

        if (gameCareer != null && gameCareer.getEmploymentStatus() == EmploymentStatus.UNEMPLOYED) {
            final UnemploymentBenefitPolicy.UnemploymentBenefitResult benefitResult =
                unemploymentBenefitPolicy.calculate(gameCareer);
            if (benefitResult.benefitGranted()) {
                gameCareer.consumeUnemploymentBenefit();
                return fixedResult(
                    "실업 급여 " + benefitResult.benefitAmount() + "원을 반영한다.",
                    Money.of(benefitResult.benefitAmount()),
                    Money.zero(),
                    Money.zero(),
                    Map.of(),
                    false,
                    context.isTargetPropertyOwned()
                );
            }
            return fixedResult(
                "이번 턴에 반영할 급여가 없다.",
                Money.zero(),
                Money.zero(),
                Money.zero(),
                Map.of(),
                false,
                context.isTargetPropertyOwned()
            );
        }

        final long salaryAmount = resolveSalaryAmount(gameCareer, userAssetProfile);
        if (salaryAmount == 0L) {
            return fixedResult(
                "이번 턴에 반영할 급여가 없다.",
                Money.zero(),
                Money.zero(),
                Money.zero(),
                Map.of(),
                false,
                context.isTargetPropertyOwned()
            );
        }

        return fixedResult(
            "월급 " + salaryAmount + "원을 반영한다.",
            Money.of(salaryAmount),
            Money.zero(),
            Money.zero(),
            Map.of(),
            false,
            context.isTargetPropertyOwned()
        );
    }

    private StepExecutionResult settlePreviewCash(final SettlementStepExecutionContext context) {
        final Money previewCashChange = context.getPreviewCashChange();
        if (previewCashChange.isZero()) {
            return fixedResult(
                "재량 행동으로 인한 현금 변동이 없다.",
                Money.zero(),
                Money.zero(),
                Money.zero(),
                Map.of(),
                false,
                context.isTargetPropertyOwned()
            );
        }

        return fixedResult(
            "재량 행동 현금 변동 " + formatSignedAmount(previewCashChange) + "원을 반영한다.",
            previewCashChange,
            Money.zero(),
            Money.zero(),
            Map.of(),
            false,
            context.isTargetPropertyOwned()
        );
    }

    private StepExecutionResult settleFixedExpense(final SettlementStepExecutionContext context) {
        if (!isMyDataSession(context.getSessionId())) {
            return fixedResult(
                "이번 턴에 반영할 고정 지출이 없다.",
                Money.zero(),
                Money.zero(),
                Money.zero(),
                Map.of(),
                false,
                context.isTargetPropertyOwned()
            );
        }

        final long fixedExpenseAmount = userAssetProfileRepository.findById(context.getUserId())
            .map(UserAssetProfile::getMonthlyFixedExpenseAmount)
            .orElse(0L);
        if (fixedExpenseAmount == 0L) {
            return fixedResult(
                "이번 턴에 반영할 고정 지출이 없다.",
                Money.zero(),
                Money.zero(),
                Money.zero(),
                Map.of(),
                false,
                context.isTargetPropertyOwned()
            );
        }

        return fixedResult(
            "고정 지출 " + fixedExpenseAmount + "원을 차감한다.",
            Money.of(-fixedExpenseAmount),
            Money.zero(),
            Money.zero(),
            Map.of(),
            false,
            context.isTargetPropertyOwned()
        );
    }

    private StepExecutionResult settleHousingCost(final SettlementStepExecutionContext context) {
        final GameHousing gameHousing = gameHousingRepository.findByGameSessionId(context.getSessionId())
            .orElse(null);
        if (gameHousing == null) {
            return fixedResult(
                "이번 턴에 반영할 주거 비용이 없다.",
                Money.zero(),
                Money.zero(),
                Money.zero(),
                Map.of(),
                false,
                context.isTargetPropertyOwned()
            );
        }

        final long monthlyRentAmount = moneyAmount(gameHousing.getMonthlyRent());
        final long maintenanceFeeAmount = moneyAmount(gameHousing.getMaintenanceFee());
        final long totalHousingCost = monthlyRentAmount + maintenanceFeeAmount;
        if (totalHousingCost == 0L) {
            return fixedResult(
                "이번 턴에 반영할 주거 비용이 없다.",
                Money.zero(),
                Money.zero(),
                Money.zero(),
                Map.of(),
                false,
                context.isTargetPropertyOwned()
            );
        }

        return fixedResult(
            "주거 비용 " + totalHousingCost + "원을 차감한다.",
            Money.of(-totalHousingCost),
            Money.zero(),
            Money.zero(),
            Map.of(),
            false,
            context.isTargetPropertyOwned()
        );
    }

    private StepExecutionResult settleCardBill(final SettlementStepExecutionContext context) {
        if (!isMyDataSession(context.getSessionId())) {
            return fixedResult(
                "이번 턴에 정산할 카드 대금이 없다.",
                Money.zero(),
                Money.zero(),
                Money.zero(),
                Map.of(),
                false,
                context.isTargetPropertyOwned()
            );
        }

        final long cardBillAmount = userAssetCardSpendRepository.findAllByUserIdOrderByIdAsc(context.getUserId())
            .stream()
            .mapToLong(cardSpend -> cardSpend.getAmount() == null ? 0L : cardSpend.getAmount())
            .sum();
        if (cardBillAmount == 0L) {
            return fixedResult(
                "이번 턴에 정산할 카드 대금이 없다.",
                Money.zero(),
                Money.zero(),
                Money.zero(),
                Map.of(),
                false,
                context.isTargetPropertyOwned()
            );
        }

        return fixedResult(
            "카드 대금 " + cardBillAmount + "원을 차감한다.",
            Money.of(-cardBillAmount),
            Money.zero(),
            Money.zero(),
            Map.of(),
            false,
            context.isTargetPropertyOwned()
        );
    }

    private StepExecutionResult settleStockOrders(final SettlementStepExecutionContext context) {
        final List<StockOrder> pendingOrders = stockOrderRepository
            .findAllByGameSessionIdAndExecuteTurnAndOrderStatus(
                context.getSessionId(),
                context.getNextTurnNumber(),
                OrderStatus.PENDING
            );
        if (pendingOrders.isEmpty()) {
            return fixedResult(
                "이번 턴에 체결할 주식 주문이 없다.",
                Money.zero(),
                Money.zero(),
                Money.zero(),
                Map.of(),
                false,
                context.isTargetPropertyOwned()
            );
        }

        long buyAmount = 0L;
        long sellAmount = 0L;
        for (StockOrder pendingOrder : pendingOrders) {
            final Integer executionPrice = resolveExecutionPrice(context.getSessionId(), pendingOrder.getStockCode());
            if (executionPrice == null) {
                continue;
            }
            final long orderAmount = (long) executionPrice * pendingOrder.getQuantity();
            if (pendingOrder.getOrderType() == OrderType.BUY) {
                buyAmount += orderAmount;
                continue;
            }
            sellAmount += orderAmount;
        }

        final long cashDeltaAmount = stockTradingService.settleOrders(
            context.getSessionId(),
            context.getNextTurnNumber()
        );
        final long stockValueDeltaAmount = buyAmount - sellAmount;

        return fixedResult(
            "주식 주문 " + pendingOrders.size() + "건을 체결한다.",
            Money.of(cashDeltaAmount),
            Money.of(stockValueDeltaAmount),
            Money.zero(),
            Map.of(),
            false,
            context.isTargetPropertyOwned()
        );
    }

    private StepExecutionResult settleLoanInterest(final SettlementStepExecutionContext context) {
        final Money currentLoanBalance = Money.of(activeLoanPrincipalAmount(context.getSessionId()));
        final int loanInterestAmount = loanService.settleMonthlyInterest(context.getSessionId());
        final Money nextLoanBalance = Money.of(activeLoanPrincipalAmount(context.getSessionId()));
        if (loanInterestAmount == 0) {
            return fixedResult(
                "이번 턴에 정산할 대출 이자가 없다.",
                Money.zero(),
                Money.zero(),
                nextLoanBalance.subtract(currentLoanBalance),
                Map.of(),
                false,
                context.isTargetPropertyOwned()
            );
        }

        return fixedResult(
            "대출 상환액 " + loanInterestAmount + "원을 차감한다.",
            Money.of(-loanInterestAmount),
            Money.zero(),
            nextLoanBalance.subtract(currentLoanBalance),
            Map.of(),
            false,
            context.isTargetPropertyOwned()
        );
    }

    private StepExecutionResult fixedResult(
        final String description,
        final Money cashDelta,
        final Money stockValueDelta,
        final Money loanBalanceDelta,
        final Map<String, Integer> statChanges,
        final boolean eventTriggered,
        final boolean targetPropertyOwned
    ) {
        return StepExecutionResult.of(
            description,
            cashDelta,
            stockValueDelta,
            loanBalanceDelta,
            statChanges,
            eventTriggered,
            targetPropertyOwned
        );
    }

    private long resolveSalaryAmount(
        final GameCareer gameCareer,
        final UserAssetProfile userAssetProfile
    ) {
        if (userAssetProfile != null && userAssetProfile.getMonthlySalaryAmount() != null) {
            return userAssetProfile.getMonthlySalaryAmount();
        }
        if (gameCareer == null || gameCareer.getSalary() == null) {
            return 0L;
        }
        return BigDecimal.valueOf(gameCareer.getSalary())
            .divide(BigDecimal.valueOf(MONTHS_PER_YEAR), 0, RoundingMode.DOWN)
            .longValueExact();
    }

    private Integer resolveExecutionPrice(final Long sessionId, final String stockCode) {
        return stockTradingService.getMarketPrices(sessionId).stream()
            .filter(state -> state.getStockCode().equals(stockCode))
            .map(GameStockMarketState::getCurrentPriceAmount)
            .findFirst()
            .orElse(null);
    }

    private long moneyAmount(final Money money) {
        if (money == null) {
            return 0L;
        }
        return money.getAmount().longValueExact();
    }

    private long activeLoanPrincipalAmount(final Long sessionId) {
        return gameLoanRepository.findAllByGameSessionId(sessionId).stream()
            .filter(GameLoan::isActive)
            .mapToLong(loan -> loan.getPrincipalAmount() == null ? 0L : loan.getPrincipalAmount())
            .sum();
    }

    private String formatAmount(final Money money) {
        return money.getAmount().toPlainString();
    }

    private String formatSignedAmount(final Money money) {
        return money.getAmount().toPlainString();
    }

    private Integer toGameId(final Long sessionId) {
        return Math.toIntExact(sessionId);
    }

    private boolean isMyDataSession(final Long sessionId) {
        return gameSessionRepository.findById(sessionId)
            .map(GameSession::getDataSourceType)
            .filter(dataSourceType -> dataSourceType == DataSourceType.MY_DATA)
            .isPresent();
    }
}
