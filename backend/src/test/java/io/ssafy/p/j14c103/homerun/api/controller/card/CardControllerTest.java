package io.ssafy.p.j14c103.homerun.api.controller.card;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.card.CardService;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardBenefitResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardListResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardRecommendationResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardResponse;
import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CardController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.homerun.local", uriPort = 443)
class CardControllerTest extends RestDocsTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @DisplayName("전체 카드 조회는 ApiResponse로 감싼 카드 목록을 반환한다")
    @Test
    void getCards() throws Exception {
        // given
        given(cardService.getCards()).willReturn(CardListResponse.of(List.of(sampleCard(1L, "Alpha Card"))));

        // when & then
        mockMvc.perform(get("/api/cards")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.cards[0].cardProductId").value(1L))
                .andExpect(jsonPath("$.data.cards[0].cardName").value("Alpha Card"))
                .andExpect(jsonPath("$.data.cards[0].activeBenefits[0].categoryName").value("생활"))
                .andExpect(jsonPath("$.data.cards[0].activeBenefits[1].categoryName").value("생활"))
                .andExpect(jsonPath("$.data.cards[0].activeBenefits[1].discountRate").value(5.0))
                .andExpect(jsonPath("$.data.cards[0].activeBenefits[2].categoryName").value("교통"))
                .andDo(document("card/list/success",
                        requestHeaders(authorizationHeader()),
                        apiResponseFields(
                                "카드 목록",
                                fieldWithPath("cards").type(JsonFieldType.ARRAY).description("카드 목록"),
                                fieldWithPath("cards[].cardProductId").type(JsonFieldType.NUMBER).description("카드 상품 ID"),
                                fieldWithPath("cards[].cardName").type(JsonFieldType.STRING).description("카드 이름"),
                                fieldWithPath("cards[].cardIssuerName").type(JsonFieldType.STRING).description("카드사 이름"),
                                fieldWithPath("cards[].cardDescription").type(JsonFieldType.STRING).description("카드 설명"),
                                fieldWithPath("cards[].baselinePerformanceAmount").type(JsonFieldType.NUMBER).description("전월 실적 기준 금액"),
                                fieldWithPath("cards[].maxBenefitLimitAmount").type(JsonFieldType.NUMBER).description("최대 혜택 한도"),
                                fieldWithPath("cards[].cardImageUrl").type(JsonFieldType.STRING).description("카드 이미지 URL"),
                                fieldWithPath("cards[].activeBenefits").type(JsonFieldType.ARRAY).description("활성 혜택 목록"),
                                fieldWithPath("cards[].activeBenefits[].categoryId").type(JsonFieldType.STRING).description("혜택 카테고리 ID"),
                                fieldWithPath("cards[].activeBenefits[].categoryName").type(JsonFieldType.STRING).description("혜택 카테고리명"),
                                fieldWithPath("cards[].activeBenefits[].categoryDescription").type(JsonFieldType.STRING).description("혜택 카테고리 설명"),
                                fieldWithPath("cards[].activeBenefits[].discountRate").type(JsonFieldType.NUMBER).description("할인율"),
                                fieldWithPath("cards[].activeBenefits[].exampleMerchants").type(JsonFieldType.ARRAY).description("예시 가맹점 목록")
                        )
                ));
    }

    @DisplayName("카드 추천 조회는 ApiResponse로 감싼 추천 목록을 반환한다")
    @Test
    void getRecommendations() throws Exception {
        // given
        given(cardService.getRecommendations(1L))
                .willReturn(CardRecommendationResponse.of(List.of(sampleCard(3L, "Bravo Card"))));

        // when & then
        mockMvc.perform(get("/api/cards/recommendations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .with(currentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.recommendations[0].cardProductId").value(3L))
                .andExpect(jsonPath("$.data.recommendations[0].cardName").value("Bravo Card"))
                .andDo(document("card/recommendations/success",
                        requestHeaders(authorizationHeader()),
                        apiResponseFields(
                                "추천 카드 목록",
                                fieldWithPath("recommendations").type(JsonFieldType.ARRAY).description("추천 카드 목록"),
                                fieldWithPath("recommendations[].cardProductId").type(JsonFieldType.NUMBER).description("카드 상품 ID"),
                                fieldWithPath("recommendations[].cardName").type(JsonFieldType.STRING).description("카드 이름"),
                                fieldWithPath("recommendations[].cardIssuerName").type(JsonFieldType.STRING).description("카드사 이름"),
                                fieldWithPath("recommendations[].cardDescription").type(JsonFieldType.STRING).description("카드 설명"),
                                fieldWithPath("recommendations[].baselinePerformanceAmount").type(JsonFieldType.NUMBER).description("전월 실적 기준 금액"),
                                fieldWithPath("recommendations[].maxBenefitLimitAmount").type(JsonFieldType.NUMBER).description("최대 혜택 한도"),
                                fieldWithPath("recommendations[].cardImageUrl").type(JsonFieldType.STRING).description("카드 이미지 URL"),
                                fieldWithPath("recommendations[].activeBenefits").type(JsonFieldType.ARRAY).description("활성 혜택 목록"),
                                fieldWithPath("recommendations[].activeBenefits[].categoryId").type(JsonFieldType.STRING).description("혜택 카테고리 ID"),
                                fieldWithPath("recommendations[].activeBenefits[].categoryName").type(JsonFieldType.STRING).description("혜택 카테고리명"),
                                fieldWithPath("recommendations[].activeBenefits[].categoryDescription").type(JsonFieldType.STRING).description("혜택 카테고리 설명"),
                                fieldWithPath("recommendations[].activeBenefits[].discountRate").type(JsonFieldType.NUMBER).description("할인율"),
                                fieldWithPath("recommendations[].activeBenefits[].exampleMerchants").type(JsonFieldType.ARRAY).description("예시 가맹점 목록")
                        )
                ));
    }

    @DisplayName("카드 추천 v2 조회는 ApiResponse로 감싼 추천 목록을 반환한다")
    @Test
    void getPreferenceRecommendations() throws Exception {
        // given
        given(cardService.getPreferenceRecommendations(1L))
                .willReturn(CardRecommendationResponse.of(List.of(sampleCard(4L, "Delta Card"))));

        // when & then
        mockMvc.perform(get("/api/cards/recommendations/v2")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .with(currentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.recommendations[0].cardProductId").value(4L))
                .andExpect(jsonPath("$.data.recommendations[0].cardName").value("Delta Card"))
                .andDo(document("card/recommendations-v2/success",
                        requestHeaders(authorizationHeader()),
                        apiResponseFields(
                                "추천 카드 목록",
                                fieldWithPath("recommendations").type(JsonFieldType.ARRAY).description("추천 카드 목록"),
                                fieldWithPath("recommendations[].cardProductId").type(JsonFieldType.NUMBER).description("카드 상품 ID"),
                                fieldWithPath("recommendations[].cardName").type(JsonFieldType.STRING).description("카드 이름"),
                                fieldWithPath("recommendations[].cardIssuerName").type(JsonFieldType.STRING).description("카드사 이름"),
                                fieldWithPath("recommendations[].cardDescription").type(JsonFieldType.STRING).description("카드 설명"),
                                fieldWithPath("recommendations[].baselinePerformanceAmount").type(JsonFieldType.NUMBER).description("전월 실적 기준 금액"),
                                fieldWithPath("recommendations[].maxBenefitLimitAmount").type(JsonFieldType.NUMBER).description("최대 혜택 한도"),
                                fieldWithPath("recommendations[].cardImageUrl").type(JsonFieldType.STRING).description("카드 이미지 URL"),
                                fieldWithPath("recommendations[].activeBenefits").type(JsonFieldType.ARRAY).description("활성 혜택 목록"),
                                fieldWithPath("recommendations[].activeBenefits[].categoryId").type(JsonFieldType.STRING).description("혜택 카테고리 ID"),
                                fieldWithPath("recommendations[].activeBenefits[].categoryName").type(JsonFieldType.STRING).description("혜택 카테고리명"),
                                fieldWithPath("recommendations[].activeBenefits[].categoryDescription").type(JsonFieldType.STRING).description("혜택 카테고리 설명"),
                                fieldWithPath("recommendations[].activeBenefits[].discountRate").type(JsonFieldType.NUMBER).description("할인율"),
                                fieldWithPath("recommendations[].activeBenefits[].exampleMerchants").type(JsonFieldType.ARRAY).description("예시 가맹점 목록")
                        )
                ));
    }

    private CardResponse sampleCard(final Long cardProductId, final String cardName) {
        return CardResponse.of(
                cardProductId,
                cardName,
                "KB국민카드",
                "생활형 카드",
                300000,
                40000,
                "card.png",
                List.of(
                        CardBenefitResponse.of(
                                "CG-9ca85f66311a23d",
                                "생활",
                                "(음식점, 커피전문점, 편의점, 약국 ..)",
                                BigDecimal.valueOf(10.0),
                                List.of("스타벅스")
                        ),
                        CardBenefitResponse.of(
                                "CG-9ca85f66311a23d",
                                "생활",
                                "(음식점, 커피전문점, 편의점, 약국 ..)",
                                BigDecimal.valueOf(5.0),
                                List.of("배달의민족")
                        ),
                        CardBenefitResponse.of(
                                "CG-4fa85f6455cad4a",
                                "교통",
                                "(버스, 지하철, 택시)",
                                BigDecimal.valueOf(3.0),
                                List.of("택시")
                        )
                )
        );
    }
}
