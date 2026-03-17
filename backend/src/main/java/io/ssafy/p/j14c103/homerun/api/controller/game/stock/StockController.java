package io.ssafy.p.j14c103.homerun.api.controller.game.stock;

import io.ssafy.p.j14c103.homerun.api.controller.game.stock.request.StockOrderRequest;
import io.ssafy.p.j14c103.homerun.api.controller.game.stock.response.StockHoldingsResponse;
import io.ssafy.p.j14c103.homerun.api.controller.game.stock.response.StockMarketResponse;
import io.ssafy.p.j14c103.homerun.api.controller.game.stock.response.StockOrderResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.stock.StockTradingService;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.GameStockMarketState;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.GameStockMarketStateId;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.GameStockMarketStateRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.OrderType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockHolding;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockMarket;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockMarketRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.stock.StockOrder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games/sessions/{sessionId}/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockTradingService stockTradingService;
    private final StockMarketRepository stockMarketRepository;
    private final GameStockMarketStateRepository gameStockMarketStateRepository;

    /**
     * 주식 시장 현재가 목록.
     */
    @GetMapping("/market")
    public ResponseEntity<StockMarketResponse> getMarket(
            @PathVariable final Integer sessionId
    ) {
        final List<GameStockMarketState> states = stockTradingService.getMarketPrices(sessionId);
        final Map<String, StockMarket> marketMap = stockMarketRepository.findAll().stream()
                .collect(Collectors.toMap(StockMarket::getStockCode, m -> m));

        final List<StockMarketResponse.StockItem> items = states.stream()
                .map(state -> {
                    final StockMarket market = marketMap.get(state.getStockCode());
                    final String name = market != null ? market.getStockName() : state.getStockCode();
                    return new StockMarketResponse.StockItem(
                            state.getStockCode(), name, state.getCurrentPriceAmount());
                })
                .toList();

        return ResponseEntity.ok(StockMarketResponse.from(items));
    }

    /**
     * 내 보유 주식 현황.
     */
    @GetMapping("/holdings")
    public ResponseEntity<StockHoldingsResponse> getHoldings(
            @PathVariable final Integer sessionId
    ) {
        final List<StockHolding> holdings = stockTradingService.getHoldings(sessionId);
        final Map<String, StockMarket> marketMap = stockMarketRepository.findAll().stream()
                .collect(Collectors.toMap(StockMarket::getStockCode, m -> m));

        final List<StockHoldingsResponse.HoldingItem> items = holdings.stream()
                .map(holding -> {
                    final StockMarket market = marketMap.get(holding.getStockCode());
                    final String name = market != null ? market.getStockName() : holding.getStockCode();

                    final GameStockMarketState state = gameStockMarketStateRepository
                            .findById(new GameStockMarketStateId(sessionId, holding.getStockCode()))
                            .orElse(null);
                    final int currentPrice = state != null ? state.getCurrentPriceAmount() : 0;

                    return new StockHoldingsResponse.HoldingItem(
                            holding.getStockCode(), name, currentPrice,
                            holding.getQuantity(), holding.getAveragePurchasePriceAmount());
                })
                .toList();

        return ResponseEntity.ok(StockHoldingsResponse.from(items));
    }

    /**
     * 매수/매도 주문.
     */
    @PostMapping("/orders")
    public ResponseEntity<StockOrderResponse> placeOrder(
            @PathVariable final Integer sessionId,
            @RequestBody final StockOrderRequest request
    ) {
        // TODO: currentTurn은 GameSession에서 가져와야 함 — 임시로 1
        final int currentTurn = 1;

        final OrderType orderType = OrderType.valueOf(request.getOrderType());
        final StockOrder order;
        if (orderType == OrderType.BUY) {
            order = stockTradingService.placeBuyOrder(
                    sessionId, request.getStockCode(), request.getQuantity(), currentTurn);
        } else {
            order = stockTradingService.placeSellOrder(
                    sessionId, request.getStockCode(), request.getQuantity(), currentTurn);
        }

        final GameStockMarketState state = gameStockMarketStateRepository
                .findById(new GameStockMarketStateId(sessionId, request.getStockCode()))
                .orElse(null);
        final int price = state != null ? state.getCurrentPriceAmount() : 0;

        return ResponseEntity.ok(new StockOrderResponse(
                order.getStockOrderId(),
                order.getStockCode(),
                order.getOrderType().name(),
                order.getQuantity(),
                price,
                "턴 " + order.getExecuteTurn(),
                order.getOrderStatus().name()
        ));
    }
}
