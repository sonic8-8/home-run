package io.ssafy.p.j14c103.homerun.api.controller.home;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.home.LoanRecommendationService;
import io.ssafy.p.j14c103.homerun.api.service.home.response.LoanRecommendationItem;
import io.ssafy.p.j14c103.homerun.api.service.home.response.LoanRecommendationResponse;
import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
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

@WebMvcTest(LoanRecommendationController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.homerun.local", uriPort = 443)
class LoanRecommendationControllerTest extends RestDocsTestSupport {

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
        mockMvc.perform(get("/api/home/loan-recommendations")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .with(currentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.cssScore").value(840))
                .andExpect(jsonPath("$.data.creditLoans[0].productId").value("LOAN-KB-001"))
                .andExpect(jsonPath("$.data.creditLoans[0].estimatedRate").value(3.79))
                .andExpect(jsonPath("$.data.creditLoans[0].creditProductTypeName").value("일반신용대출"))
                .andExpect(jsonPath("$.data.creditLoans[0].joinWay").value("영업점,인터넷"))
                .andExpect(jsonPath("$.data.jeonseLoans[0].averageRate").value(2.94))
                .andExpect(jsonPath("$.data.jeonseLoans[0].repaymentTypeName").value("만기일시상환방식"))
                .andExpect(jsonPath("$.data.mortgageLoans[0].mortgageTypeName").value("아파트"))
                .andDo(document("home/loan-recommendations/success",
                        requestHeaders(authorizationHeader()),
                        apiResponseFields(
                                "대출 추천 결과",
                                fieldWithPath("cssScore").type(JsonFieldType.NUMBER).description("CSS 점수"),
                                fieldWithPath("cssGrade").type(JsonFieldType.NUMBER).description("CSS 등급"),
                                fieldWithPath("cssGradeLabel").type(JsonFieldType.STRING).description("CSS 등급 라벨"),
                                fieldWithPath("estimatedMinRate").type(JsonFieldType.NUMBER).description("예상 최저 금리"),
                                fieldWithPath("creditLoans").type(JsonFieldType.ARRAY).description("개인신용대출 추천 목록"),
                                fieldWithPath("creditLoans[].productId").type(JsonFieldType.STRING).description("대출 상품 ID"),
                                fieldWithPath("creditLoans[].bankName").type(JsonFieldType.STRING).description("은행명"),
                                fieldWithPath("creditLoans[].bankLogoUrl").type(JsonFieldType.STRING).description("은행 로고 URL"),
                                fieldWithPath("creditLoans[].productName").type(JsonFieldType.STRING).description("대출 상품명"),
                                fieldWithPath("creditLoans[].productType").type(JsonFieldType.STRING).description("대출 상품 유형"),
                                fieldWithPath("creditLoans[].minRate").type(JsonFieldType.NUMBER).description("최저 금리"),
                                fieldWithPath("creditLoans[].maxRate").type(JsonFieldType.NUMBER).description("최고 금리"),
                                fieldWithPath("creditLoans[].estimatedRate").type(JsonFieldType.NUMBER).description("예상 금리"),
                                fieldWithPath("creditLoans[].joinWay").type(JsonFieldType.STRING).description("가입 경로"),
                                fieldWithPath("creditLoans[].creditProductTypeName").type(JsonFieldType.STRING).description("신용대출 상품 분류명"),
                                fieldWithPath("creditLoans[].averageRate").type(JsonFieldType.VARIES).description("평균 금리"),
                                fieldWithPath("creditLoans[].rateTypeName").type(JsonFieldType.VARIES).description("금리 유형"),
                                fieldWithPath("creditLoans[].repaymentTypeName").type(JsonFieldType.VARIES).description("상환 방식"),
                                fieldWithPath("creditLoans[].loanLimit").type(JsonFieldType.VARIES).description("대출 한도"),
                                fieldWithPath("creditLoans[].mortgageTypeName").type(JsonFieldType.VARIES).description("담보 유형"),
                                fieldWithPath("jeonseLoans").type(JsonFieldType.ARRAY).description("전세자금대출 추천 목록"),
                                fieldWithPath("jeonseLoans[].productId").type(JsonFieldType.STRING).description("대출 상품 ID"),
                                fieldWithPath("jeonseLoans[].bankName").type(JsonFieldType.STRING).description("은행명"),
                                fieldWithPath("jeonseLoans[].bankLogoUrl").type(JsonFieldType.STRING).description("은행 로고 URL"),
                                fieldWithPath("jeonseLoans[].productName").type(JsonFieldType.STRING).description("대출 상품명"),
                                fieldWithPath("jeonseLoans[].productType").type(JsonFieldType.STRING).description("대출 상품 유형"),
                                fieldWithPath("jeonseLoans[].minRate").type(JsonFieldType.NUMBER).description("최저 금리"),
                                fieldWithPath("jeonseLoans[].maxRate").type(JsonFieldType.NUMBER).description("최고 금리"),
                                fieldWithPath("jeonseLoans[].estimatedRate").type(JsonFieldType.NUMBER).description("예상 금리"),
                                fieldWithPath("jeonseLoans[].joinWay").type(JsonFieldType.STRING).description("가입 경로"),
                                fieldWithPath("jeonseLoans[].creditProductTypeName").type(JsonFieldType.VARIES).description("신용대출 상품 분류명"),
                                fieldWithPath("jeonseLoans[].averageRate").type(JsonFieldType.NUMBER).description("평균 금리"),
                                fieldWithPath("jeonseLoans[].rateTypeName").type(JsonFieldType.STRING).description("금리 유형"),
                                fieldWithPath("jeonseLoans[].repaymentTypeName").type(JsonFieldType.STRING).description("상환 방식"),
                                fieldWithPath("jeonseLoans[].loanLimit").type(JsonFieldType.STRING).description("대출 한도"),
                                fieldWithPath("jeonseLoans[].mortgageTypeName").type(JsonFieldType.VARIES).description("담보 유형"),
                                fieldWithPath("mortgageLoans").type(JsonFieldType.ARRAY).description("주택담보대출 추천 목록"),
                                fieldWithPath("mortgageLoans[].productId").type(JsonFieldType.STRING).description("대출 상품 ID"),
                                fieldWithPath("mortgageLoans[].bankName").type(JsonFieldType.STRING).description("은행명"),
                                fieldWithPath("mortgageLoans[].bankLogoUrl").type(JsonFieldType.STRING).description("은행 로고 URL"),
                                fieldWithPath("mortgageLoans[].productName").type(JsonFieldType.STRING).description("대출 상품명"),
                                fieldWithPath("mortgageLoans[].productType").type(JsonFieldType.STRING).description("대출 상품 유형"),
                                fieldWithPath("mortgageLoans[].minRate").type(JsonFieldType.NUMBER).description("최저 금리"),
                                fieldWithPath("mortgageLoans[].maxRate").type(JsonFieldType.NUMBER).description("최고 금리"),
                                fieldWithPath("mortgageLoans[].estimatedRate").type(JsonFieldType.NUMBER).description("예상 금리"),
                                fieldWithPath("mortgageLoans[].joinWay").type(JsonFieldType.STRING).description("가입 경로"),
                                fieldWithPath("mortgageLoans[].creditProductTypeName").type(JsonFieldType.VARIES).description("신용대출 상품 분류명"),
                                fieldWithPath("mortgageLoans[].averageRate").type(JsonFieldType.NUMBER).description("평균 금리"),
                                fieldWithPath("mortgageLoans[].rateTypeName").type(JsonFieldType.STRING).description("금리 유형"),
                                fieldWithPath("mortgageLoans[].repaymentTypeName").type(JsonFieldType.STRING).description("상환 방식"),
                                fieldWithPath("mortgageLoans[].loanLimit").type(JsonFieldType.STRING).description("대출 한도"),
                                fieldWithPath("mortgageLoans[].mortgageTypeName").type(JsonFieldType.STRING).description("담보 유형")
                        )
                ));
    }
}
