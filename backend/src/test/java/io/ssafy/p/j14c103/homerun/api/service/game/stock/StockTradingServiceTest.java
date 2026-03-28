package io.ssafy.p.j14c103.homerun.api.service.game.stock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.GameStockMarketState;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.GameStockMarketStateId;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.GameStockMarketStateRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.OrderStatus;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.OrderType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockHolding;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockHoldingId;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockHoldingRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockMarket;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockMarketRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockOrder;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockOrderRepository;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockTradingServiceTest {

    private static final String SETTLEMENT_PHASE_DURATION = "homerun.settlement.phase.duration";
    private static final String STOCK_ORDER_SETTLEMENT_PHASE = "stock_order_settlement";

    private StockTradingService stockTradingService;

    @Mock
    private StockMarketRepository stockMarketRepository;

    @Mock
    private GameStockMarketStateRepository gameStockMarketStateRepository;

    @Mock
    private StockHoldingRepository stockHoldingRepository;

    @Mock
    private StockOrderRepository stockOrderRepository;

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    @BeforeEach
    void setUp() {
        stockTradingService = new StockTradingService(
            stockMarketRepository,
            gameStockMarketStateRepository,
            stockHoldingRepository,
            stockOrderRepository,
            meterRegistry
        );
    }

    @DisplayName("게임 시작 시 stock_markets를 세션 상태로 복사한다")
    @Test
    void initializeStockStates() {
        // given
        final StockMarket bio = StockMarket.create("BIO", "바이오주", "068270", "바이오", 100000, new BigDecimal("0.15"));
        final StockMarket semi = StockMarket.create("SEMI", "반도체주", "005930", "반도체", 72000, new BigDecimal("0.10"));
        given(stockMarketRepository.findAll()).willReturn(List.of(bio, semi));

        // when
        stockTradingService.initializeStockStates(1L);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<GameStockMarketState>> captor = ArgumentCaptor.forClass(List.class);
        verify(gameStockMarketStateRepository).saveAll(captor.capture());
        final List<GameStockMarketState> saved = captor.getValue();
        assertThat(saved).hasSize(2);
        assertThat(saved.get(0).getStockCode()).isEqualTo("BIO");
        assertThat(saved.get(0).getCurrentPriceAmount()).isEqualTo(100000);
        assertThat(saved.get(1).getStockCode()).isEqualTo("SEMI");
        assertThat(saved.get(1).getCurrentPriceAmount()).isEqualTo(72000);
    }

    @DisplayName("매수 주문을 등록한다")
    @Test
    void placeBuyOrder() {
        // given
        given(gameStockMarketStateRepository.findById(any(GameStockMarketStateId.class)))
                .willReturn(Optional.of(GameStockMarketState.initializeFrom(1L, "BIO", 100000, 1)));
        given(stockOrderRepository.save(any(StockOrder.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        final StockOrder order = stockTradingService.placeBuyOrder(1L, "BIO", 5, 1);

        // then
        assertThat(order.getOrderType()).isEqualTo(OrderType.BUY);
        assertThat(order.getQuantity()).isEqualTo(5);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getExecuteTurn()).isEqualTo(2);
    }

    @DisplayName("수량이 0 이하이면 매수 주문이 실패한다")
    @Test
    void placeBuyOrder_invalid_quantity() {
        given(gameStockMarketStateRepository.findById(any(GameStockMarketStateId.class)))
                .willReturn(Optional.of(GameStockMarketState.initializeFrom(1L, "BIO", 100000, 1)));

        assertThatThrownBy(() -> stockTradingService.placeBuyOrder(1L, "BIO", 0, 1))
                .isInstanceOf(HomerunException.class);
    }

    @DisplayName("매도 주문 시 보유 수량보다 많으면 실패한다")
    @Test
    void placeSellOrder_insufficient_quantity() {
        given(gameStockMarketStateRepository.findById(any(GameStockMarketStateId.class)))
                .willReturn(Optional.of(GameStockMarketState.initializeFrom(1L, "BIO", 100000, 1)));
        given(stockHoldingRepository.findById(any(StockHoldingId.class)))
                .willReturn(Optional.of(StockHolding.create(1L, "BIO", 100000, 3)));

        assertThatThrownBy(() -> stockTradingService.placeSellOrder(1L, "BIO", 5, 1))
                .isInstanceOf(HomerunException.class);
    }

    @DisplayName("매도 주문 시 보유 수량 이하이면 성공한다")
    @Test
    void placeSellOrder_success() {
        given(gameStockMarketStateRepository.findById(any(GameStockMarketStateId.class)))
                .willReturn(Optional.of(GameStockMarketState.initializeFrom(1L, "BIO", 100000, 1)));
        given(stockHoldingRepository.findById(any(StockHoldingId.class)))
                .willReturn(Optional.of(StockHolding.create(1L, "BIO", 100000, 10)));
        given(stockOrderRepository.save(any(StockOrder.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        final StockOrder order = stockTradingService.placeSellOrder(1L, "BIO", 5, 1);

        assertThat(order.getOrderType()).isEqualTo(OrderType.SELL);
        assertThat(order.getQuantity()).isEqualTo(5);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @DisplayName("존재하지 않는 종목에 주문하면 실패한다")
    @Test
    void placeOrder_stock_not_found() {
        given(gameStockMarketStateRepository.findById(any(GameStockMarketStateId.class)))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> stockTradingService.placeBuyOrder(1L, "UNKNOWN", 5, 1))
                .isInstanceOf(HomerunException.class);
    }

    @DisplayName("정산 시 PENDING 매수 주문이 체결되고 현금이 감소한다")
    @Test
    void settleOrders_buy() {
        // given
        final long timerCountBefore = settlementPhaseTimerCount();
        final StockOrder buyOrder = StockOrder.createBuyOrder(1L, "BIO", 5, 1);
        given(stockOrderRepository.findAllByGameSessionIdAndExecuteTurnAndOrderStatus(1L, 2, OrderStatus.PENDING))
                .willReturn(List.of(buyOrder));
        given(gameStockMarketStateRepository.findById(new GameStockMarketStateId(1L, "BIO")))
                .willReturn(Optional.of(GameStockMarketState.initializeFrom(1L, "BIO", 110000, 2)));
        given(stockHoldingRepository.findById(any(StockHoldingId.class)))
                .willReturn(Optional.empty());

        // when
        final int cashChange = stockTradingService.settleOrders(1L, 2);

        // then
        assertThat(cashChange).isEqualTo(-550000); // 110000 * 5
        assertThat(buyOrder.getOrderStatus()).isEqualTo(OrderStatus.EXECUTED);
        assertThat(settlementPhaseTimerCount()).isEqualTo(timerCountBefore + 1);
    }

    @DisplayName("정산 시 PENDING 매도 주문이 체결되고 현금이 증가한다")
    @Test
    void settleOrders_sell() {
        // given
        final long timerCountBefore = settlementPhaseTimerCount();
        final StockOrder sellOrder = StockOrder.createSellOrder(1L, "BIO", 3, 1);
        given(stockOrderRepository.findAllByGameSessionIdAndExecuteTurnAndOrderStatus(1L, 2, OrderStatus.PENDING))
                .willReturn(List.of(sellOrder));
        given(gameStockMarketStateRepository.findById(new GameStockMarketStateId(1L, "BIO")))
                .willReturn(Optional.of(GameStockMarketState.initializeFrom(1L, "BIO", 120000, 2)));
        given(stockHoldingRepository.findById(any(StockHoldingId.class)))
                .willReturn(Optional.of(StockHolding.create(1L, "BIO", 100000, 10)));

        // when
        final int cashChange = stockTradingService.settleOrders(1L, 2);

        // then
        assertThat(cashChange).isEqualTo(360000); // 120000 * 3
        assertThat(sellOrder.getOrderStatus()).isEqualTo(OrderStatus.EXECUTED);
        assertThat(settlementPhaseTimerCount()).isEqualTo(timerCountBefore + 1);
    }

    private long settlementPhaseTimerCount() {
        final Timer timer = meterRegistry.find(SETTLEMENT_PHASE_DURATION)
            .tag("phase", STOCK_ORDER_SETTLEMENT_PHASE)
            .timer();
        if (timer == null) {
            return 0;
        }
        return timer.count();
    }
}
