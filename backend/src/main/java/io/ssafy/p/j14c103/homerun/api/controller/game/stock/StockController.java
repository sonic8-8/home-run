package io.ssafy.p.j14c103.homerun.api.controller.game.stock;

import io.ssafy.p.j14c103.homerun.api.controller.game.stock.request.StockOrderRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.stock.StockTradingService;
import io.ssafy.p.j14c103.homerun.api.service.game.stock.response.StockHoldingsServiceResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.stock.response.StockMarketServiceResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.stock.response.StockOrderServiceResponse;
import io.ssafy.p.j14c103.homerun.global.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/market")
    public ApiResponse<StockMarketServiceResponse> getMarket(
            @PathVariable final Long sessionId
    ) {
        final StockMarketServiceResponse response = stockTradingService.getMarket(sessionId);
        return ApiResponse.ok(response);
    }

    @GetMapping("/holdings")
    public ApiResponse<StockHoldingsServiceResponse> getHoldings(
            @PathVariable final Long sessionId
    ) {
        final StockHoldingsServiceResponse response = stockTradingService.getHoldingsSummary(sessionId);
        return ApiResponse.ok(response);
    }

    @PostMapping("/orders")
    public ApiResponse<StockOrderServiceResponse> placeOrder(
            @PathVariable final Long sessionId,
            @Valid @RequestBody final StockOrderRequest request
    ) {
        final StockOrderServiceResponse response = stockTradingService.placeOrder(sessionId, request.toServiceRequest());
        return ApiResponse.ok(response);
    }
}
