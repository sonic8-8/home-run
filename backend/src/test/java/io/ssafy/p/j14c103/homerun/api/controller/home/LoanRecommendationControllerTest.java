package io.ssafy.p.j14c103.homerun.api.controller.home;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.home.LoanRecommendationService;
import io.ssafy.p.j14c103.homerun.api.service.home.response.LoanRecommendationItem;
import io.ssafy.p.j14c103.homerun.api.service.home.response.LoanRecommendationResponse;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
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

@WebMvcTest(LoanRecommendationController.class)
@AutoConfigureMockMvc(addFilters = false)
class LoanRecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoanRecommendationService loanRecommendationService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

  @DisplayName("대출 추천 조회는 ApiResponse로 감싼 추천 결과를 반환한다")
  @Test
  void getLoanRecommendations() throws Exception {
    // given
    final LoanRecommendationResponse response = LoanRecommendationResponse.builder()
            .cssScore(840)
            .cssGrade(2)
                .cssGradeLabel("Very Good")
                .estimatedMinRate(3.49)
                .creditLoans(List.of(LoanRecommendationItem.builder()
                        .productId("LOAN-KB-001")
                        .bankName("KB")
                        .bankLogoUrl("/images/banks/kb.png")
                        .productName("KB 직장인 신용대출")
                        .productType("개인신용대출")
                        .minRate(3.49)
                        .maxRate(5.89)
                        .estimatedRate(3.79)
                        .build()))
                .jeonseLoans(List.of())
                .mortgageLoans(List.of())
            .build();
    given(loanRecommendationService.getRecommendations(1L)).willReturn(response);

    // when & then
    mockMvc.perform(get("/api/home/loan-recommendations").with(currentUser()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.cssScore").value(840))
                .andExpect(jsonPath("$.data.creditLoans[0].productId").value("LOAN-KB-001"))
                .andExpect(jsonPath("$.data.creditLoans[0].estimatedRate").value(3.79));
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
