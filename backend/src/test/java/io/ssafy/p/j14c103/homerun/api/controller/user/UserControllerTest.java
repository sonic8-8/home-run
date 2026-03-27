package io.ssafy.p.j14c103.homerun.api.controller.user;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedRequestFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.user.request.UserAssetLinkServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAssetLinkService;
import io.ssafy.p.j14c103.homerun.api.service.user.UserMeService;
import io.ssafy.p.j14c103.homerun.api.service.user.response.UserAssetLinkResponse;
import io.ssafy.p.j14c103.homerun.api.service.user.response.UserMeResponse;
import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.homerun.local", uriPort = 443)
class UserControllerTest extends RestDocsTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserMeService userMeService;

    @MockitoBean
    private UserAssetLinkService userAssetLinkService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @DisplayName("내 정보 조회는 ApiResponse로 감싼 사용자 응답을 반환한다.")
    @Test
    void getMe() throws Exception {
        // given
        given(userMeService.getMe(1L)).willReturn(UserMeResponse.of(
                1L,
                "user@example.com",
                "홍길동",
                false,
                null,
                null
        ));

        // when & then
        mockMvc.perform(get("/api/users/me")
                        .with(currentUser())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.isAssetLinked").value(false))
                .andExpect(jsonPath("$.data.totalAssetAmount").isEmpty())
                .andExpect(jsonPath("$.data.netAssetAmount").isEmpty())
                .andDo(document("user/me/success",
                        requestHeaders(authorizationHeader()),
                        apiResponseFields(
                                "내 정보",
                                fieldWithPath("userId").type(JsonFieldType.NUMBER).description("회원 ID"),
                                fieldWithPath("email").type(JsonFieldType.STRING).description("회원 이메일"),
                                fieldWithPath("name").type(JsonFieldType.STRING).description("회원 이름"),
                                fieldWithPath("isAssetLinked").type(JsonFieldType.BOOLEAN).description("자산 연동 여부"),
                                fieldWithPath("totalAssetAmount").type(JsonFieldType.VARIES).optional().description("총자산"),
                                fieldWithPath("netAssetAmount").type(JsonFieldType.VARIES).optional().description("순자산")
                        )
                ));
    }

    @DisplayName("자산 연동 실행은 ApiResponse로 감싼 연동 결과를 반환한다.")
    @Test
    void linkAssets() throws Exception {
        // given
        given(userAssetLinkService.linkAssets(eq(1L), any(UserAssetLinkServiceRequest.class))).willReturn(UserAssetLinkResponse.of(
                true,
                true,
                true,
                true
        ));

        // when & then
        mockMvc.perform(post("/api/users/me/asset-link")
                        .with(currentUser())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mainAccountBalanceAmount": 3000000,
                                  "salaryDayOfMonth": 25,
                                  "monthlySalaryAmount": 4200000,
                                  "monthlyFixedExpenseAmount": 1800000,
                                  "depositItems": [
                                    {"name": "정기예금", "amount": 7000000}
                                  ],
                                  "loanItems": [
                                    {"name": "신용대출", "amount": 12000000}
                                  ],
                                  "otherIncomeItems": [
                                    {"name": "부업", "amount": 300000}
                                  ],
                                  "cardSpendItems": [
                                    {"category": "LIVING", "amount": 200000},
                                    {"category": "TRANSPORT", "amount": 100000}
                                  ],
                                  "paymentTypes": ["LIVING", "TRANSPORT"],
                                  "jobType": "LARGE_BIZ"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.isAssetLinked").value(true))
                .andExpect(jsonPath("$.data.mainAccountCreated").value(true))
                .andExpect(jsonPath("$.data.seedmoneyAccountCreated").value(true))
                .andExpect(jsonPath("$.data.summaryInitialized").value(true))
                .andDo(document("user/asset-link/success",
                        requestHeaders(authorizationHeader()),
                        relaxedRequestFields(
                                fieldWithPath("mainAccountBalanceAmount").type(JsonFieldType.NUMBER).description("수시입출금 계좌 잔액"),
                                fieldWithPath("salaryDayOfMonth").type(JsonFieldType.NUMBER).description("급여일"),
                                fieldWithPath("monthlySalaryAmount").type(JsonFieldType.NUMBER).description("월 급여액"),
                                fieldWithPath("monthlyFixedExpenseAmount").type(JsonFieldType.NUMBER).description("월 고정 지출"),
                                fieldWithPath("depositItems").type(JsonFieldType.ARRAY).optional().description("예금 항목 목록"),
                                fieldWithPath("loanItems").type(JsonFieldType.ARRAY).optional().description("대출 항목 목록"),
                                fieldWithPath("otherIncomeItems").type(JsonFieldType.ARRAY).optional().description("기타 수입 항목 목록"),
                                fieldWithPath("cardSpendItems").type(JsonFieldType.ARRAY).optional().description("카드 지출 항목 목록"),
                                fieldWithPath("paymentTypes").type(JsonFieldType.ARRAY).description("많이 쓰는 소비 분야"),
                                fieldWithPath("jobType").type(JsonFieldType.STRING).description("직장 유형")
                        ),
                        apiResponseFields(
                                "자산 연동 결과",
                                fieldWithPath("isAssetLinked").type(JsonFieldType.BOOLEAN).description("자산 연동 여부"),
                                fieldWithPath("mainAccountCreated").type(JsonFieldType.BOOLEAN).description("메인 계좌 생성 여부"),
                                fieldWithPath("seedmoneyAccountCreated").type(JsonFieldType.BOOLEAN).description("시드머니 계좌 생성 여부"),
                                fieldWithPath("summaryInitialized").type(JsonFieldType.BOOLEAN).description("요약 정보 초기화 여부")
                        )
                ));
    }

    @DisplayName("자산 연동 요청에서 소비 선호 카테고리가 없으면 400을 반환한다.")
    @Test
    void linkAssetsWithoutPaymentTypes() throws Exception {
        mockMvc.perform(post("/api/users/me/asset-link")
                        .with(currentUser())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mainAccountBalanceAmount": 3000000,
                                  "salaryDayOfMonth": 25,
                                  "monthlySalaryAmount": 4200000,
                                  "monthlyFixedExpenseAmount": 1800000,
                                  "jobType": "LARGE_BIZ"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field", hasItem("paymentTypes")))
                .andExpect(jsonPath("$.errors[*].message", hasItem("많이 쓰는 소비 분야는 최소 1개 이상 선택해야 합니다.")))
                .andDo(document("user/asset-link/validation-error",
                        requestHeaders(authorizationHeader()),
                        validationErrorResponseFields()
                ));
    }
}
