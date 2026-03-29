package io.ssafy.p.j14c103.homerun.api.controller.card;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ssafy.p.j14c103.homerun.api.controller.card.request.CardApplyRequest;
import io.ssafy.p.j14c103.homerun.api.service.card.CardService;
import io.ssafy.p.j14c103.homerun.api.service.card.OwnedCardService;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardBenefitResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardListResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardRecommendationResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.OwnedCardListResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.OwnedCardResponse;
import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private OwnedCardService ownedCardService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @DisplayName("전체 카드 조회는 ApiResponse로 감싼 카드 목록을 반환한다")
    @Test
    void getCards() throws Exception {
        given(cardService.getCards()).willReturn(CardListResponse.of(List.of(sampleCard(1L, "Alpha Card"))));

        mockMvc.perform(get("/api/cards")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.cards[0].cardProductId").value(1L))
                .andExpect(jsonPath("$.data.cards[0].cardName").value("Alpha Card"))
                .andExpect(jsonPath("$.data.cards[0].activeBenefits[0].categoryName").value("생활"))
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
        given(cardService.getRecommendations(1L))
                .willReturn(CardRecommendationResponse.of(List.of(sampleCard(3L, "Bravo Card"))));

        mockMvc.perform(get("/api/cards/recommendations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .with(currentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.recommendations[0].cardProductId").value(3L))
                .andExpect(jsonPath("$.data.recommendations[0].cardName").value("Bravo Card"));
    }

    @DisplayName("카드 추천 v2 조회는 ApiResponse로 감싼 추천 목록을 반환한다")
    @Test
    void getPreferenceRecommendations() throws Exception {
        given(cardService.getPreferenceRecommendations(1L))
                .willReturn(CardRecommendationResponse.of(List.of(sampleCard(4L, "Delta Card"))));

        mockMvc.perform(get("/api/cards/recommendations/v2")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .with(currentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.recommendations[0].cardProductId").value(4L))
                .andExpect(jsonPath("$.data.recommendations[0].cardName").value("Delta Card"));
    }

    @DisplayName("보유 카드 조회는 ApiResponse로 감싼 보유 카드 목록을 반환한다")
    @Test
    void getOwnedCards() throws Exception {
        given(ownedCardService.getOwnedCards(1L))
                .willReturn(OwnedCardListResponse.of(List.of(sampleOwnedCard(11L, 1L, "Alpha Card"))));

        mockMvc.perform(get("/api/cards/owned")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .with(currentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.cards[0].ownedCardId").value(11L))
                .andExpect(jsonPath("$.data.cards[0].cardProductId").value(1L))
                .andExpect(jsonPath("$.data.cards[0].cardAlias").value("Alpha Card 메인"))
                .andExpect(jsonPath("$.data.cards[0].activeBenefits[0].categoryName").value("생활"))
                .andDo(document("card/owned/success",
                        requestHeaders(authorizationHeader()),
                        apiResponseFields(
                                "보유 카드 목록",
                                fieldWithPath("cards").type(JsonFieldType.ARRAY).description("보유 카드 목록"),
                                fieldWithPath("cards[].ownedCardId").type(JsonFieldType.NUMBER).description("보유 카드 ID"),
                                fieldWithPath("cards[].cardProductId").type(JsonFieldType.NUMBER).description("카드 상품 ID"),
                                fieldWithPath("cards[].cardName").type(JsonFieldType.STRING).description("카드 이름"),
                                fieldWithPath("cards[].cardIssuerName").type(JsonFieldType.STRING).description("카드사 이름"),
                                fieldWithPath("cards[].cardDescription").type(JsonFieldType.STRING).description("카드 설명"),
                                fieldWithPath("cards[].baselinePerformanceAmount").type(JsonFieldType.NUMBER).description("전월 실적 기준 금액"),
                                fieldWithPath("cards[].maxBenefitLimitAmount").type(JsonFieldType.NUMBER).description("최대 혜택 한도"),
                                fieldWithPath("cards[].activeBenefits").type(JsonFieldType.ARRAY).description("활성 혜택 목록"),
                                fieldWithPath("cards[].activeBenefits[].categoryId").type(JsonFieldType.STRING).description("혜택 카테고리 ID"),
                                fieldWithPath("cards[].activeBenefits[].categoryName").type(JsonFieldType.STRING).description("혜택 카테고리명"),
                                fieldWithPath("cards[].activeBenefits[].categoryDescription").type(JsonFieldType.STRING).description("혜택 카테고리 설명"),
                                fieldWithPath("cards[].activeBenefits[].discountRate").type(JsonFieldType.NUMBER).description("할인율"),
                                fieldWithPath("cards[].activeBenefits[].exampleMerchants").type(JsonFieldType.ARRAY).description("예시 가맹점 목록"),
                                fieldWithPath("cards[].cardAlias").type(JsonFieldType.STRING).description("카드 별칭"),
                                fieldWithPath("cards[].maskedCardNo").type(JsonFieldType.STRING).description("마스킹 카드 번호"),
                                fieldWithPath("cards[].cardImageUrl").type(JsonFieldType.STRING).description("카드 이미지 URL"),
                                fieldWithPath("cards[].openedAt").type(JsonFieldType.STRING).description("카드 개설 시각")
                        )
                ));
    }

    @DisplayName("카드 신청은 생성 응답을 반환한다")
    @Test
    void applyCard() throws Exception {
        final CardApplyRequest request = CardApplyRequest.builder()
                .cardProductId(1L)
                .build();
        given(ownedCardService.applyCard(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any()))
                .willReturn(sampleOwnedCard(11L, 1L, "Alpha Card"));

        mockMvc.perform(post("/api/cards/apply")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .with(currentUser())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.ownedCardId").value(11L))
                .andExpect(jsonPath("$.data.cardProductId").value(1L))
                .andDo(document("card/apply/success",
                        requestHeaders(authorizationHeader()),
                        requestFields(
                                fieldWithPath("cardProductId").type(JsonFieldType.NUMBER).description("신청할 카드 상품 ID")
                        ),
                        apiResponseFields(
                                "신청된 보유 카드",
                                fieldWithPath("ownedCardId").type(JsonFieldType.NUMBER).description("보유 카드 ID"),
                                fieldWithPath("cardProductId").type(JsonFieldType.NUMBER).description("카드 상품 ID"),
                                fieldWithPath("cardName").type(JsonFieldType.STRING).description("카드 이름"),
                                fieldWithPath("cardIssuerName").type(JsonFieldType.STRING).description("카드사 이름"),
                                fieldWithPath("cardDescription").type(JsonFieldType.STRING).description("카드 설명"),
                                fieldWithPath("baselinePerformanceAmount").type(JsonFieldType.NUMBER).description("전월 실적 기준 금액"),
                                fieldWithPath("maxBenefitLimitAmount").type(JsonFieldType.NUMBER).description("최대 혜택 한도"),
                                fieldWithPath("activeBenefits").type(JsonFieldType.ARRAY).description("활성 혜택 목록"),
                                fieldWithPath("activeBenefits[].categoryId").type(JsonFieldType.STRING).description("혜택 카테고리 ID"),
                                fieldWithPath("activeBenefits[].categoryName").type(JsonFieldType.STRING).description("혜택 카테고리명"),
                                fieldWithPath("activeBenefits[].categoryDescription").type(JsonFieldType.STRING).description("혜택 카테고리 설명"),
                                fieldWithPath("activeBenefits[].discountRate").type(JsonFieldType.NUMBER).description("할인율"),
                                fieldWithPath("activeBenefits[].exampleMerchants").type(JsonFieldType.ARRAY).description("예시 가맹점 목록"),
                                fieldWithPath("cardAlias").type(JsonFieldType.STRING).description("카드 별칭"),
                                fieldWithPath("maskedCardNo").type(JsonFieldType.STRING).description("마스킹 카드 번호"),
                                fieldWithPath("cardImageUrl").type(JsonFieldType.STRING).description("카드 이미지 URL"),
                                fieldWithPath("openedAt").type(JsonFieldType.STRING).description("카드 개설 시각")
                        )
                ));
    }

    @DisplayName("카드 해지는 204를 반환한다")
    @Test
    void cancelOwnedCard() throws Exception {
        mockMvc.perform(delete("/api/cards/owned/11")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .with(currentUser()))
                .andExpect(status().isNoContent())
                .andDo(document("card/cancel/success",
                        requestHeaders(authorizationHeader())));

        then(ownedCardService).should().cancelOwnedCard(1L, 11L);
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
                benefits()
        );
    }

    private OwnedCardResponse sampleOwnedCard(
            final Long ownedCardId,
            final Long cardProductId,
            final String cardName
    ) {
        return OwnedCardResponse.of(
                ownedCardId,
                cardProductId,
                cardName,
                "KB국민카드",
                "생활형 카드",
                300000L,
                40000L,
                benefits(),
                cardName + " 메인",
                "1234-****-****-0001",
                "https://cdn.example.com/card.png",
                LocalDateTime.of(2026, 3, 30, 12, 0)
        );
    }

    private List<CardBenefitResponse> benefits() {
        return List.of(
                CardBenefitResponse.of(
                        "CG-9ca85f66311a23d",
                        "생활",
                        "(음식점, 커피전문점, 편의점, 약국 ..)",
                        BigDecimal.valueOf(10.0),
                        List.of("스타벅스")
                ),
                CardBenefitResponse.of(
                        "CG-4fa85f6455cad4a",
                        "교통",
                        "(버스, 지하철, 택시)",
                        BigDecimal.valueOf(3.0),
                        List.of("택시")
                )
        );
    }
}
