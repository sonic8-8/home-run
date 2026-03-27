package io.ssafy.p.j14c103.homerun.api.service.game.stock;

import io.ssafy.p.j14c103.homerun.api.service.game.stock.request.StockOrderServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.stock.response.StockHoldingsServiceResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.stock.response.StockMarketServiceResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.stock.response.StockOrderServiceResponse;
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
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주식 거래 서비스.
 * 주문 등록(현재 턴) → 정산 시 체결(다음 턴) 흐름을 처리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockTradingService {

    private final StockMarketRepository stockMarketRepository;
    private final GameStockMarketStateRepository gameStockMarketStateRepository;
    private final StockHoldingRepository stockHoldingRepository;
    private final StockOrderRepository stockOrderRepository;

    /**
     * 게임 시작 시 stock_markets의 base_price를 복사하여 세션 초기 상태를 생성한다.
     */
    @Transactional
    public void initializeStockStates(final Long gameSessionId) {
        final List<StockMarket> markets = stockMarketRepository.findAll();
        final List<GameStockMarketState> states = markets.stream()
                .map(market -> GameStockMarketState.initializeFrom(
                        gameSessionId, market.getStockCode(), market.getBasePriceAmount(), 1))
                .toList();
        gameStockMarketStateRepository.saveAll(states);
        log.info("게임 세션 {} 주식 시장 초기화 완료: {}종목", gameSessionId, states.size());
    }

    /**
     * 세션의 현재 시장 가격 조회.
     */
    public List<GameStockMarketState> getMarketPrices(final Long gameSessionId) {
        return gameStockMarketStateRepository.findAllByGameSessionId(gameSessionId);
    }

    /**
     * 세션의 보유 주식 조회.
     */
    public List<StockHolding> getHoldings(final Long gameSessionId) {
        return stockHoldingRepository.findAllByGameSessionId(gameSessionId);
    }

    public StockMarketServiceResponse getMarket(final Long gameSessionId) {
        final List<GameStockMarketState> states = getMarketPrices(gameSessionId);
        final Map<String, StockMarket> marketMap = stockMarketRepository.findAll().stream()
                .collect(Collectors.toMap(StockMarket::getStockCode, market -> market));

        final List<StockMarketServiceResponse.StockItemResponse> items = states.stream()
                .map(state -> {
                    final StockMarket market = marketMap.get(state.getStockCode());
                    final String stockName = market != null ? market.getStockName() : state.getStockCode();
                    return StockMarketServiceResponse.StockItemResponse.of(
                            state.getStockCode(),
                            stockName,
                            state.getCurrentPriceAmount()
                    );
                })
                .toList();

        return StockMarketServiceResponse.of(items);
    }

    public StockHoldingsServiceResponse getHoldingsSummary(final Long gameSessionId) {
        final List<StockHolding> holdings = getHoldings(gameSessionId);
        final Map<String, StockMarket> marketMap = stockMarketRepository.findAll().stream()
                .collect(Collectors.toMap(StockMarket::getStockCode, market -> market));

        final List<StockHoldingsServiceResponse.HoldingItemResponse> items = holdings.stream()
                .map(holding -> {
                    final StockMarket market = marketMap.get(holding.getStockCode());
                    final String stockName = market != null ? market.getStockName() : holding.getStockCode();
                    final int currentPrice = resolveCurrentPrice(gameSessionId, holding.getStockCode());

                    return StockHoldingsServiceResponse.HoldingItemResponse.of(
                            holding.getStockCode(),
                            stockName,
                            currentPrice,
                            holding.getQuantity(),
                            holding.getAveragePurchasePriceAmount()
                    );
                })
                .toList();

        return StockHoldingsServiceResponse.of(items);
    }

    @Transactional
    public StockOrderServiceResponse placeOrder(
            final Long gameSessionId,
            final StockOrderServiceRequest request
    ) {
        final int currentTurn = 1;
        final OrderType orderType = OrderType.valueOf(request.getOrderType());
        final StockOrder order = placeOrder(gameSessionId, request, currentTurn, orderType);
        final int currentPrice = resolveCurrentPrice(gameSessionId, request.getStockCode());

        return StockOrderServiceResponse.of(
                order.getStockOrderId(),
                order.getStockCode(),
                order.getOrderType().name(),
                order.getQuantity(),
                currentPrice,
                "턴 " + order.getExecuteTurn(),
                order.getOrderStatus().name()
        );
    }

    /**
     * 매수 주문 등록. 다음 턴에 체결된다.
     */
    @Transactional
    public StockOrder placeBuyOrder(
            final Long gameSessionId,
            final String stockCode,
            final Integer quantity,
            final Integer currentTurn
    ) {
        validateStockExists(gameSessionId, stockCode);
        if (quantity == null || quantity <= 0) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }

        final StockOrder order = StockOrder.createBuyOrder(gameSessionId, stockCode, quantity, currentTurn);
        return stockOrderRepository.save(order);
    }

    /**
     * 매도 주문 등록. 보유 수량 검증 후 다음 턴에 체결된다.
     */
    @Transactional
    public StockOrder placeSellOrder(
            final Long gameSessionId,
            final String stockCode,
            final Integer quantity,
            final Integer currentTurn
    ) {
        validateStockExists(gameSessionId, stockCode);
        if (quantity == null || quantity <= 0) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }

        final StockHolding holding = stockHoldingRepository
                .findById(new StockHoldingId(gameSessionId, stockCode))
                .orElseThrow(() -> new HomerunException(ErrorCode.INVALID_INPUT_VALUE));

        if (holding.getQuantity() < quantity) {
            throw new HomerunException(ErrorCode.INVALID_INPUT_VALUE);
        }

        final StockOrder order = StockOrder.createSellOrder(gameSessionId, stockCode, quantity, currentTurn);
        return stockOrderRepository.save(order);
    }

    /**
     * 턴 정산 시 해당 턴에 체결 예정인 PENDING 주문들을 실행한다.
     *
     * @return 정산으로 인한 현금 변동액 (매도수입 - 매수비용)
     */
    @Transactional
    public int settleOrders(final Long gameSessionId, final Integer currentTurn) {
        final List<StockOrder> pendingOrders = stockOrderRepository
                .findAllByGameSessionIdAndExecuteTurnAndOrderStatus(
                        gameSessionId, currentTurn, OrderStatus.PENDING);

        int cashChange = 0;

        for (final StockOrder order : pendingOrders) {
            final GameStockMarketState state = gameStockMarketStateRepository
                    .findById(new GameStockMarketStateId(gameSessionId, order.getStockCode()))
                    .orElse(null);

            if (state == null) {
                order.cancel();
                continue;
            }

            final int executionPrice = state.getCurrentPriceAmount();

            if (order.getOrderType() == io.ssafy.p.j14c103.homerun.domain.gamesession.stock.OrderType.BUY) {
                cashChange -= executeBuyOrder(order, gameSessionId, executionPrice);
            } else {
                cashChange += executeSellOrder(order, gameSessionId, executionPrice);
            }

            order.execute();
        }

        log.info("게임 세션 {} 턴 {} 주식 정산 완료: {}건, 현금 변동: {}",
                gameSessionId, currentTurn, pendingOrders.size(), cashChange);
        return cashChange;
    }

    private int executeBuyOrder(final StockOrder order, final Long gameSessionId, final int price) {
        final int totalCost = price * order.getQuantity();
        final StockHoldingId holdingId = new StockHoldingId(gameSessionId, order.getStockCode());

        stockHoldingRepository.findById(holdingId)
                .ifPresentOrElse(
                        holding -> holding.addShares(price, order.getQuantity()),
                        () -> stockHoldingRepository.save(
                                StockHolding.create(gameSessionId, order.getStockCode(),
                                        price, order.getQuantity()))
                );

        return totalCost;
    }

    private int executeSellOrder(final StockOrder order, final Long gameSessionId, final int price) {
        final int totalProceeds = price * order.getQuantity();
        final StockHoldingId holdingId = new StockHoldingId(gameSessionId, order.getStockCode());

        stockHoldingRepository.findById(holdingId)
                .ifPresent(holding -> {
                    holding.removeShares(order.getQuantity());
                    if (holding.isEmpty()) {
                        stockHoldingRepository.delete(holding);
                    }
                });

        return totalProceeds;
    }

    private StockOrder placeOrder(
            final Long gameSessionId,
            final StockOrderServiceRequest request,
            final int currentTurn,
            final OrderType orderType
    ) {
        if (orderType == OrderType.BUY) {
            return placeBuyOrder(gameSessionId, request.getStockCode(), request.getQuantity(), currentTurn);
        }

        return placeSellOrder(gameSessionId, request.getStockCode(), request.getQuantity(), currentTurn);
    }

    private int resolveCurrentPrice(final Long gameSessionId, final String stockCode) {
        return gameStockMarketStateRepository.findById(new GameStockMarketStateId(gameSessionId, stockCode))
                .map(GameStockMarketState::getCurrentPriceAmount)
                .orElse(0);
    }

    private void validateStockExists(final Long gameSessionId, final String stockCode) {
        gameStockMarketStateRepository
                .findById(new GameStockMarketStateId(gameSessionId, stockCode))
                .orElseThrow(() -> new HomerunException(ErrorCode.INVALID_INPUT_VALUE));
    }
}
