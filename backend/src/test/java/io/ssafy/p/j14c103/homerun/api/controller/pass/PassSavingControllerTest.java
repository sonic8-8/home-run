package io.ssafy.p.j14c103.homerun.api.controller.pass;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.pass.PassSavingService;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassHistoryResponse;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassSaveResponse;
import io.ssafy.p.j14c103.homerun.api.service.pass.response.PassWidgetResponse;
import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
import io.ssafy.p.j14c103.homerun.domain.seedmoney.SeedmoneyTransaction;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PassSavingController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.homerun.local", uriPort = 443)
class PassSavingControllerTest extends RestDocsTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PassSavingService passSavingService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @DisplayName("즉시 저축은 ApiResponse로 감싼 결과를 반환한다")
    @Test
    void save() throws Exception {
        // given
        given(passSavingService.save(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any()))
                .willReturn(PassSaveResponse.of(5000, 70000, 295000));

        // when & then
        mockMvc.perform(post("/api/pass/save")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .with(currentUser())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "subscriptionId": 1,
                                  "sourceAccountId": "110-123-000000"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.savedAmount").value(5000))
                .andExpect(jsonPath("$.data.totalSaved").value(70000))
                .andDo(document("pass/save/success",
                        requestHeaders(authorizationHeader()),
                        requestFields(
                                fieldWithPath("subscriptionId").type(JsonFieldType.NUMBER).description("PASS 구독 ID"),
                                fieldWithPath("sourceAccountId").type(JsonFieldType.STRING).description("자동 이체 출금 계좌 ID")
                        ),
                        apiResponseFields(
                                "즉시 저축 결과",
                                fieldWithPath("savedAmount").type(JsonFieldType.NUMBER).description("이번에 저축한 금액"),
                                fieldWithPath("totalSaved").type(JsonFieldType.NUMBER).description("누적 저축 금액"),
                                fieldWithPath("remainingBalance").type(JsonFieldType.NUMBER).description("저축 후 남은 잔액")
                        )
                ));
    }

    @DisplayName("즉시 저축 요청에서 필수값이 없으면 400과 공통 에러 응답을 반환한다")
    @Test
    void saveWithoutSubscriptionId() throws Exception {
        // when & then
        mockMvc.perform(post("/api/pass/save")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .with(currentUser())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceAccountId": "110-123-000000"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()))
                .andExpect(jsonPath("$.errors[*].field", hasItem("subscriptionId")))
                .andExpect(jsonPath("$.errors[*].message", hasItem("구독 ID는 필수입니다.")))
                .andDo(document("pass/save/validation-error",
                        requestHeaders(authorizationHeader()),
                        validationErrorResponseFields()
                ));

        verifyNoInteractions(passSavingService);
    }

    @DisplayName("PASS 위젯 조회는 ApiResponse를 반환한다")
    @Test
    void getWidget() throws Exception {
        // given
        given(passSavingService.getWidget(1L)).willReturn(PassWidgetResponse.of(45000, 182000, 500000));

        // when & then
        mockMvc.perform(get("/api/pass/widget")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .with(currentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.todaySaved").value(45000))
                .andExpect(jsonPath("$.data.remaining").value(318000))
                .andDo(document("pass/widget/success",
                        requestHeaders(authorizationHeader()),
                        apiResponseFields(
                                "PASS 위젯 정보",
                                fieldWithPath("todaySaved").type(JsonFieldType.NUMBER).description("오늘 저축 금액"),
                                fieldWithPath("weeklySaved").type(JsonFieldType.NUMBER).description("주간 누적 저축 금액"),
                                fieldWithPath("weeklyGoal").type(JsonFieldType.NUMBER).description("주간 목표 금액"),
                                fieldWithPath("progressRate").type(JsonFieldType.NUMBER).description("목표 대비 진행률"),
                                fieldWithPath("remaining").type(JsonFieldType.NUMBER).description("남은 목표 금액")
                        )
                ));
    }

    @DisplayName("PASS 이력 조회는 ApiResponse로 감싼 페이지 결과를 반환한다")
    @Test
    void getHistory() throws Exception {
        // given
        final SeedmoneyTransaction transaction = SeedmoneyTransaction.createSave(1L, 1L, 5000);
        ReflectionTestUtils.setField(transaction, "id", 1L);
        final Page<PassHistoryResponse> page = new PageImpl<>(
                List.of(PassHistoryResponse.from(transaction)),
                PageRequest.of(0, 20),
                1
        );
        given(passSavingService.getHistory(1L, 0, 20)).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/pass/history")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .param("page", "0")
                        .param("size", "20")
                        .with(currentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].transactionType").value("SAVE"))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andDo(document("pass/history/success",
                        requestHeaders(authorizationHeader()),
                        queryParameters(
                                parameterWithName("page").description("조회할 페이지 번호"),
                                parameterWithName("size").description("페이지 크기")
                        ),
                        relaxedApiResponseFields(
                                "PASS 저축 이력 페이지",
                                fieldWithPath("content").type(JsonFieldType.ARRAY).description("저축 이력 목록"),
                                fieldWithPath("content[].id").type(JsonFieldType.NUMBER).description("거래 ID"),
                                fieldWithPath("content[].transactionType").type(JsonFieldType.STRING).description("거래 유형"),
                                fieldWithPath("content[].amount").type(JsonFieldType.NUMBER).description("거래 금액"),
                                fieldWithPath("content[].counterpartyAccountMasked").type(JsonFieldType.VARIES).description("상대 계좌 마스킹 값"),
                                fieldWithPath("content[].createdAt").type(JsonFieldType.STRING).description("거래 시각"),
                                fieldWithPath("totalElements").type(JsonFieldType.NUMBER).description("전체 거래 건수"),
                                fieldWithPath("totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
                                fieldWithPath("size").type(JsonFieldType.NUMBER).description("페이지 크기"),
                                fieldWithPath("number").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                                fieldWithPath("numberOfElements").type(JsonFieldType.NUMBER).description("현재 페이지 요소 수"),
                                fieldWithPath("first").type(JsonFieldType.BOOLEAN).description("첫 페이지 여부"),
                                fieldWithPath("last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
                                fieldWithPath("empty").type(JsonFieldType.BOOLEAN).description("결과 비어 있음 여부")
                        )
                ));
    }
}
