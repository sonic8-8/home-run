package io.ssafy.p.j14c103.homerun.api.controller.card;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.card.CardService;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardBenefitResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardListResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardRecommendationResponse;
import io.ssafy.p.j14c103.homerun.api.service.card.response.CardResponse;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(CardController.class)
@AutoConfigureMockMvc(addFilters = false)
class CardControllerTest {

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
        mockMvc.perform(get("/api/cards").with(currentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.cards[0].cardProductId").value(1L))
                .andExpect(jsonPath("$.data.cards[0].cardName").value("Alpha Card"))
                .andExpect(jsonPath("$.data.cards[0].activeBenefits[0].categoryName").value("생활"))
                .andExpect(jsonPath("$.data.cards[0].activeBenefits[1].categoryName").value("생활"))
                .andExpect(jsonPath("$.data.cards[0].activeBenefits[1].discountRate").value(5.0))
                .andExpect(jsonPath("$.data.cards[0].activeBenefits[2].categoryName").value("교통"));
    }

    @DisplayName("카드 추천 조회는 ApiResponse로 감싼 추천 목록을 반환한다")
    @Test
    void getRecommendations() throws Exception {
        // given
        given(cardService.getRecommendations(1L))
                .willReturn(CardRecommendationResponse.of(List.of(sampleCard(3L, "Bravo Card"))));

        // when & then
        mockMvc.perform(get("/api/cards/recommendations").with(currentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.recommendations[0].cardProductId").value(3L))
                .andExpect(jsonPath("$.data.recommendations[0].cardName").value("Bravo Card"));
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

    private RequestPostProcessor currentUser() {
        final Authentication authentication = new UsernamePasswordAuthenticationToken(
                new AuthenticatedUser(1L, "user@example.com"),
                null,
                List.of()
        );
        return request -> {
            final SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            request.setUserPrincipal(authentication);
            return request;
        };
    }
}
