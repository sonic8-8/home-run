package io.ssafy.p.j14c103.homerun.api.controller.game.loan;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.game.loan.LoanProductService;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.LoanService;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.request.LoanCalculateServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.response.LoanCalculateResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.response.LoanProductResponse;
import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
import java.util.List;
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

@WebMvcTest(LoanController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.homerun.local", uriPort = 443)
class LoanControllerTest extends RestDocsTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoanService loanService;

    @MockitoBean
    private LoanProductService loanProductService;

    @DisplayName("대출 상품 목록 조회는 ApiResponse로 감싼 상품 목록을 반환한다.")
    @Test
    void getProducts() throws Exception {
        LoanProductResponse response = LoanProductResponse.builder()
                .productId("LN-001")
                .bankName("싸피은행")
                .bankLogoUrl("https://cdn.example.com/logo.png")
                .productName("청년 전세 대출")
                .productType("JEONSE")
                .minRate(2.3)
                .maxRate(3.1)
                .build();
        given(loanProductService.getProducts("ALL", 0, 20)).willReturn(List.of(response));

        mockMvc.perform(get("/api/games/sessions/{sessionId}/loans/products", 10L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].productId").value("LN-001"))
                .andExpect(jsonPath("$.data[0].bankName").value("싸피은행"))
                .andDo(document("loan/products/success",
                        requestHeaders(authorizationHeader()),
                        pathParameters(
                                parameterWithName("sessionId").description("게임 세션 ID")
                        ),
                        queryParameters(
                                parameterWithName("category").description("대출 상품 카테고리").optional(),
                                parameterWithName("page").description("페이지 번호").optional(),
                                parameterWithName("size").description("페이지 크기").optional()
                        ),
                        org.springframework.restdocs.payload.PayloadDocumentation.responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                                fieldWithPath("data").type(JsonFieldType.ARRAY).description("대출 상품 목록"),
                                fieldWithPath("data[].productId").type(JsonFieldType.STRING).description("대출 상품 ID"),
                                fieldWithPath("data[].bankName").type(JsonFieldType.STRING).description("은행명"),
                                fieldWithPath("data[].bankLogoUrl").type(JsonFieldType.STRING).description("은행 로고 URL"),
                                fieldWithPath("data[].productName").type(JsonFieldType.STRING).description("대출 상품명"),
                                fieldWithPath("data[].productType").type(JsonFieldType.STRING).description("대출 상품 유형"),
                                fieldWithPath("data[].minRate").type(JsonFieldType.NUMBER).description("최저 금리"),
                                fieldWithPath("data[].maxRate").type(JsonFieldType.NUMBER).description("최고 금리")
                        )
                ));
    }

    @DisplayName("이자 계산은 ApiResponse로 감싼 계산 결과를 반환한다.")
    @Test
    void calculate() throws Exception {
        LoanCalculateResponse response = mock(LoanCalculateResponse.class);
        given(response.getMonthlyPayment()).willReturn(356000);
        given(response.getTotalInterest()).willReturn(560000);
        given(response.getTotalPayment()).willReturn(12560000);
        given(loanService.calculate(any(LoanCalculateServiceRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/games/sessions/{sessionId}/loans/calculate", 10L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "repaymentMethod": "EQUAL_PRINCIPAL_AND_INTEREST",
                                  "termMonths": 36,
                                  "principal": 12000000,
                                  "annualRate": 3.5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.monthlyPayment").value(356000))
                .andDo(document("loan/calculate/success",
                        requestHeaders(authorizationHeader()),
                        pathParameters(
                                parameterWithName("sessionId").description("게임 세션 ID")
                        ),
                        requestFields(
                                fieldWithPath("repaymentMethod").type(JsonFieldType.STRING).description("상환 방식"),
                                fieldWithPath("termMonths").type(JsonFieldType.NUMBER).description("대출 기간(개월)"),
                                fieldWithPath("principal").type(JsonFieldType.NUMBER).description("대출 원금"),
                                fieldWithPath("annualRate").type(JsonFieldType.NUMBER).description("연 이율")
                        ),
                        apiResponseFields(
                                "이자 계산 결과",
                                fieldWithPath("monthlyPayment").type(JsonFieldType.NUMBER).description("월 납입액"),
                                fieldWithPath("totalInterest").type(JsonFieldType.NUMBER).description("총 이자"),
                                fieldWithPath("totalPayment").type(JsonFieldType.NUMBER).description("총 상환액")
                        )
                ));
    }

    @DisplayName("이자 계산 요청 검증 실패는 400과 필드 에러를 반환한다.")
    @Test
    void calculateValidationError() throws Exception {
        mockMvc.perform(post("/api/games/sessions/{sessionId}/loans/calculate", 10L)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "repaymentMethod": "",
                                  "termMonths": 0,
                                  "principal": 0,
                                  "annualRate": -0.1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItem("repaymentMethod")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("termMonths")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("principal")))
                .andDo(document("loan/calculate/validation-error",
                        requestHeaders(authorizationHeader()),
                        pathParameters(
                                parameterWithName("sessionId").description("게임 세션 ID")
                        ),
                        validationErrorResponseFields()
                ));
    }
}
