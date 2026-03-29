package io.ssafy.p.j14c103.homerun.api.controller.game.realestate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ssafy.p.j14c103.homerun.api.controller.game.realestate.request.SubmitContractReviewRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.realestate.RealEstateContractReviewService;
import io.ssafy.p.j14c103.homerun.api.service.game.realestate.response.ContractReviewResponse;
import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RealEstateContractReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.homerun.local", uriPort = 443)
class RealEstateContractReviewControllerTest extends RestDocsTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RealEstateContractReviewService realEstateContractReviewService;

    @DisplayName("계약 검토 성공은 게임 코어 공개 계약으로 응답을 반환한다")
    @Test
    void reviewContract() throws Exception {
        // given
        final SubmitContractReviewRequest request = SubmitContractReviewRequest.of(List.of("TRAP-01", "TRAP-02"));
        given(realEstateContractReviewService.review(eq(1L), eq(1001L), eq(7L), any()))
            .willReturn(ContractReviewResponse.of(
                true,
                2,
                2,
                "SAFE",
                "서류 검토를 통과했습니다."
            ));

        // when & then
        mockMvc.perform(post("/api/games/sessions/{sessionId}/real-estate/properties/{propertyId}/contract", 1001L, 7L)
                .with(currentUser())
                .header(AUTHORIZATION, "Bearer access-token")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.success").value(true))
            .andExpect(jsonPath("$.data.trapsDetected").value(2))
            .andExpect(jsonPath("$.data.trapsCorrectlyIdentified").value(2))
            .andExpect(jsonPath("$.data.contractResult").value("SAFE"))
            .andDo(document("real-estate/contract/success",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID"),
                    parameterWithName("propertyId").description("부동산 매물 ID")
                ),
                requestFields(
                    fieldWithPath("checkedTraps").type(JsonFieldType.ARRAY).description("사용자가 체크한 함정 ID 목록")
                ),
                apiResponseFields(
                    "부동산 계약 검토 결과",
                    fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("계약 검토 및 구매 검증 통과 여부"),
                    fieldWithPath("trapsDetected").type(JsonFieldType.NUMBER).description("검토 대상 함정 수"),
                    fieldWithPath("trapsCorrectlyIdentified").type(JsonFieldType.NUMBER).description("정확히 식별한 함정 수"),
                    fieldWithPath("contractResult").type(JsonFieldType.STRING).description("계약 검토 결과"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메시지")
                )
            ));
        then(realEstateContractReviewService).should().review(eq(1L), eq(1001L), eq(7L), any());
    }

    @DisplayName("계약 검토 실패는 200 응답에서 success false를 반환한다")
    @Test
    void reviewContractFailed() throws Exception {
        // given
        final SubmitContractReviewRequest request = SubmitContractReviewRequest.of(List.of("TRAP-02"));
        given(realEstateContractReviewService.review(eq(1L), eq(1001L), eq(7L), any()))
            .willReturn(ContractReviewResponse.of(
                false,
                2,
                1,
                "TRAPPED",
                "위험한 계약입니다."
            ));

        // when & then
        mockMvc.perform(post("/api/games/sessions/{sessionId}/real-estate/properties/{propertyId}/contract", 1001L, 7L)
                .with(currentUser())
                .header(AUTHORIZATION, "Bearer access-token")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.success").value(false))
            .andExpect(jsonPath("$.data.contractResult").value("TRAPPED"))
            .andExpect(jsonPath("$.data.message").value("위험한 계약입니다."));
    }

    @DisplayName("세션의 목표 매물과 다른 계약 검토 요청이면 400을 반환한다")
    @Test
    void reviewContractTargetMismatch() throws Exception {
        // given
        final SubmitContractReviewRequest request = SubmitContractReviewRequest.of(List.of("TRAP-01"));
        given(realEstateContractReviewService.review(eq(1L), eq(1001L), eq(99L), any()))
            .willThrow(new HomerunException(ErrorCode.HOUSING_CONTRACT_REVIEW_TARGET_MISMATCH));

        // when & then
        mockMvc.perform(post("/api/games/sessions/{sessionId}/real-estate/properties/{propertyId}/contract", 1001L, 99L)
                .with(currentUser())
                .header(AUTHORIZATION, "Bearer access-token")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorCode.HOUSING_CONTRACT_REVIEW_TARGET_MISMATCH.getCode()))
            .andDo(document("real-estate/contract/target-mismatch",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID"),
                    parameterWithName("propertyId").description("부동산 매물 ID")
                ),
                requestFields(
                    fieldWithPath("checkedTraps").type(JsonFieldType.ARRAY).description("사용자가 체크한 함정 ID 목록")
                ),
                basicErrorResponseFields()
            ));
    }

    @DisplayName("checkedTraps가 없으면 400을 반환한다")
    @Test
    void reviewContractInvalidInput() throws Exception {
        // when & then
        mockMvc.perform(post("/api/games/sessions/{sessionId}/real-estate/properties/{propertyId}/contract", 1001L, 7L)
                .with(currentUser())
                .header(AUTHORIZATION, "Bearer access-token")
                .contentType("application/json")
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()))
            .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_INPUT_VALUE.getMessage()))
            .andExpect(jsonPath("$.errors[*].field", hasItem("checkedTraps")));
    }

    @DisplayName("종료된 세션의 계약 검토 요청이면 409를 반환한다")
    @Test
    void reviewContractClosedSession() throws Exception {
        // given
        final SubmitContractReviewRequest request = SubmitContractReviewRequest.of(List.of("TRAP-01"));
        given(realEstateContractReviewService.review(eq(1L), eq(1001L), eq(7L), any()))
            .willThrow(new HomerunException(ErrorCode.GAME_SESSION_CLOSED));

        // when & then
        mockMvc.perform(post("/api/games/sessions/{sessionId}/real-estate/properties/{propertyId}/contract", 1001L, 7L)
                .with(currentUser())
                .header(AUTHORIZATION, "Bearer access-token")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_SESSION_CLOSED.getCode()));
    }
}
