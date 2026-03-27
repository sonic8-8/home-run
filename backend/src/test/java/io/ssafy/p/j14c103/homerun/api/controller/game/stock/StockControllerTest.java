package io.ssafy.p.j14c103.homerun.api.controller.game.stock;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.game.stock.StockTradingService;
import io.ssafy.p.j14c103.homerun.api.service.game.stock.request.StockOrderServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.stock.response.StockHoldingsServiceResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.stock.response.StockMarketServiceResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.stock.response.StockOrderServiceResponse;
import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StockController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.homerun.local", uriPort = 443)
class StockControllerTest extends RestDocsTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StockTradingService stockTradingService;

    @DisplayName("주식 시장 조회는 ApiResponse로 감싼 시세 목록을 반환한다.")
    @Test
    void getMarket() throws Exception {
        StockMarketServiceResponse response = StockMarketServiceResponse.of(
                java.util.List.of(
                        StockMarketServiceResponse.StockItemResponse.of("005930", "삼성전자", 75000)
                )
        );
        given(stockTradingService.getMarket(10L)).willReturn(response);

        mockMvc.perform(get("/api/games/sessions/{sessionId}/stocks/market", 10L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.stocks[0].stockCode").value("005930"))
                .andDo(document("stock/market/success",
                        requestHeaders(authorizationHeader()),
                        pathParameters(
                                parameterWithName("sessionId").description("게임 세션 ID")
                        ),
                        apiResponseFields(
                                "주식 시장 시세 목록",
                                fieldWithPath("stocks").type(JsonFieldType.ARRAY).description("주식 시세 목록"),
                                fieldWithPath("stocks[].stockCode").type(JsonFieldType.STRING).description("종목 코드"),
                                fieldWithPath("stocks[].stockName").type(JsonFieldType.STRING).description("종목명"),
                                fieldWithPath("stocks[].currentPrice").type(JsonFieldType.NUMBER).description("현재가"),
                                fieldWithPath("stocks[].pricePerShare").type(JsonFieldType.STRING).description("주당 가격 표시")
                        )
                ));
    }

    @DisplayName("보유 주식 조회는 ApiResponse로 감싼 보유 현황을 반환한다.")
    @Test
    void getHoldings() throws Exception {
        StockHoldingsServiceResponse response = StockHoldingsServiceResponse.of(
                java.util.List.of(
                        StockHoldingsServiceResponse.HoldingItemResponse.of("005930", "삼성전자", 75000, 3, 70000)
                )
        );
        given(stockTradingService.getHoldingsSummary(10L)).willReturn(response);

        mockMvc.perform(get("/api/games/sessions/{sessionId}/stocks/holdings", 10L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.totalValue").value(225000))
                .andExpect(jsonPath("$.data.holdings[0].stockCode").value("005930"))
                .andDo(document("stock/holdings/success",
                        requestHeaders(authorizationHeader()),
                        pathParameters(
                                parameterWithName("sessionId").description("게임 세션 ID")
                        ),
                        apiResponseFields(
                                "보유 주식 현황",
                                fieldWithPath("totalValue").type(JsonFieldType.NUMBER).description("총 평가 금액"),
                                fieldWithPath("totalReturnRate").type(JsonFieldType.NUMBER).description("총 수익률"),
                                fieldWithPath("totalPurchaseAmount").type(JsonFieldType.NUMBER).description("총 매입 금액"),
                                fieldWithPath("holdings").type(JsonFieldType.ARRAY).description("보유 주식 목록"),
                                fieldWithPath("holdings[].stockCode").type(JsonFieldType.STRING).description("종목 코드"),
                                fieldWithPath("holdings[].stockName").type(JsonFieldType.STRING).description("종목명"),
                                fieldWithPath("holdings[].currentValue").type(JsonFieldType.NUMBER).description("현재 평가 금액"),
                                fieldWithPath("holdings[].quantity").type(JsonFieldType.NUMBER).description("보유 수량"),
                                fieldWithPath("holdings[].avgPurchasePrice").type(JsonFieldType.NUMBER).description("평균 매수가"),
                                fieldWithPath("holdings[].returnRate").type(JsonFieldType.NUMBER).description("수익률")
                        )
                ));
    }

    @DisplayName("주식 주문은 ApiResponse로 감싼 주문 결과를 반환한다.")
    @Test
    void placeOrder() throws Exception {
        StockOrderServiceResponse response = StockOrderServiceResponse.of(
                1,
                "005930",
                "BUY",
                2,
                75000,
                "턴 2",
                "PENDING"
        );
        given(stockTradingService.placeOrder(any(Long.class), any(StockOrderServiceRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/games/sessions/{sessionId}/stocks/orders", 10L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "stockCode": "005930",
                                  "orderType": "BUY",
                                  "quantity": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.orderId").value(1))
                .andDo(document("stock/orders/success",
                        requestHeaders(authorizationHeader()),
                        pathParameters(
                                parameterWithName("sessionId").description("게임 세션 ID")
                        ),
                        requestFields(
                                fieldWithPath("stockCode").type(JsonFieldType.STRING).description("종목 코드"),
                                fieldWithPath("orderType").type(JsonFieldType.STRING).description("주문 유형(BUY 또는 SELL)"),
                                fieldWithPath("quantity").type(JsonFieldType.NUMBER).description("주문 수량")
                        ),
                        apiResponseFields(
                                "주문 결과",
                                fieldWithPath("orderId").type(JsonFieldType.NUMBER).description("주문 ID"),
                                fieldWithPath("stockCode").type(JsonFieldType.STRING).description("종목 코드"),
                                fieldWithPath("orderType").type(JsonFieldType.STRING).description("주문 유형"),
                                fieldWithPath("quantity").type(JsonFieldType.NUMBER).description("주문 수량"),
                                fieldWithPath("pricePerShare").type(JsonFieldType.NUMBER).description("주당 가격"),
                                fieldWithPath("totalAmount").type(JsonFieldType.NUMBER).description("총 주문 금액"),
                                fieldWithPath("executeTurn").type(JsonFieldType.STRING).description("체결 예정 턴"),
                                fieldWithPath("orderStatus").type(JsonFieldType.STRING).description("주문 상태")
                        )
                ));
    }

    @DisplayName("주식 주문 요청 검증 실패는 400과 필드 에러를 반환한다.")
    @Test
    void placeOrderValidationError() throws Exception {
        mockMvc.perform(post("/api/games/sessions/{sessionId}/stocks/orders", 10L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "stockCode": "",
                                  "orderType": "",
                                  "quantity": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItem("stockCode")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("orderType")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("quantity")))
                .andDo(document("stock/orders/validation-error",
                        requestHeaders(authorizationHeader()),
                        pathParameters(
                                parameterWithName("sessionId").description("게임 세션 ID")
                        ),
                        validationErrorResponseFields()
                ));
    }
}
