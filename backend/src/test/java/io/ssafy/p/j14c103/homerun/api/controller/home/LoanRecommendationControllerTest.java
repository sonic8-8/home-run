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
                        .joinWay("영업점,인터넷")
                        .creditProductTypeName("일반신용대출")
                        .build()))
                .jeonseLoans(List.of(LoanRecommendationItem.builder()
                        .productId("LOAN-WOORI-001")
                        .bankName("우리은행")
                        .bankLogoUrl("/images/banks/woori.png")
                        .productName("우리전세론")
                        .productType("전세자금대출")
                        .minRate(2.97)
                        .maxRate(4.82)
                        .estimatedRate(3.16)
                        .joinWay("영업점,모집인")
                        .averageRate(2.94)
                        .rateTypeName("변동금리")
                        .repaymentTypeName("만기일시상환방식")
                        .loanLimit("최대3억원")
                        .build()))
                .mortgageLoans(List.of(LoanRecommendationItem.builder()
                        .productId("LOAN-SC-001")
                        .bankName("한국스탠다드차타드은행")
                        .bankLogoUrl("/images/banks/sc.png")
                        .productName("주택담보대출")
                        .productType("주택담보대출")
                        .minRate(2.46)
                        .maxRate(4.50)
                        .estimatedRate(2.57)
                        .joinWay("영업점")
                        .averageRate(2.88)
                        .rateTypeName("변동금리")
                        .repaymentTypeName("분할상환방식")
                        .loanLimit("LTV 최대 70%")
                        .mortgageTypeName("아파트")
                        .build()))
                .build();
        given(loanRecommendationService.getRecommendations(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/home/loan-recommendations").with(currentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.cssScore").value(840))
                .andExpect(jsonPath("$.data.creditLoans[0].productId").value("LOAN-KB-001"))
                .andExpect(jsonPath("$.data.creditLoans[0].estimatedRate").value(3.79))
                .andExpect(jsonPath("$.data.creditLoans[0].creditProductTypeName").value("일반신용대출"))
                .andExpect(jsonPath("$.data.creditLoans[0].joinWay").value("영업점,인터넷"))
                .andExpect(jsonPath("$.data.jeonseLoans[0].averageRate").value(2.94))
                .andExpect(jsonPath("$.data.jeonseLoans[0].repaymentTypeName").value("만기일시상환방식"))
                .andExpect(jsonPath("$.data.mortgageLoans[0].mortgageTypeName").value("아파트"));
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
