package io.ssafy.p.j14c103.homerun.api.controller.game.career;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ssafy.p.j14c103.homerun.api.controller.game.career.request.AcceptJobTransferRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.career.GameCareerService;
import io.ssafy.p.j14c103.homerun.api.service.game.career.response.JobOfferListResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.career.response.JobTransferResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.career.response.SalaryNegotiationResponse;
import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
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

@WebMvcTest(GameCareerController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.homerun.local", uriPort = 443)
class GameCareerControllerTest extends RestDocsTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GameCareerService gameCareerService;

    @DisplayName("연봉 협상은 게임 코어 공개 계약으로 협상 결과를 반환한다.")
    @Test
    void negotiateSalary() throws Exception {
        // given
        final SalaryNegotiationResponse response = SalaryNegotiationResponse.of(
            true,
            30_000_000,
            33_300_000,
            11,
            13,
            "연봉 협상에 성공했습니다!"
        );
        given(gameCareerService.negotiateSalary(1L, 1001L)).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/games/sessions/{sessionId}/career/negotiate", 1001L)
                .with(currentUser())
                .header(AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.success").value(true))
            .andExpect(jsonPath("$.data.previousSalary").value(30_000_000))
            .andExpect(jsonPath("$.data.newSalary").value(33_300_000))
            .andExpect(jsonPath("$.data.raiseRate").value(11))
            .andExpect(jsonPath("$.data.lastNegotiatedTurn").value(13))
            .andDo(document("game-career/negotiate/success",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                apiResponseFields(
                    "연봉 협상 결과",
                    fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("협상 성공 여부"),
                    fieldWithPath("previousSalary").type(JsonFieldType.NUMBER).description("협상 전 연봉"),
                    fieldWithPath("newSalary").type(JsonFieldType.NUMBER).description("협상 후 연봉"),
                    fieldWithPath("raiseRate").type(JsonFieldType.NUMBER).description("연봉 인상률"),
                    fieldWithPath("lastNegotiatedTurn").type(JsonFieldType.NUMBER).description("최근 협상 턴"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메시지")
                )
            ));
        then(gameCareerService).should().negotiateSalary(1L, 1001L);
    }

    @DisplayName("이직 오퍼 목록 조회는 게임 코어 공개 계약으로 오퍼 목록을 반환한다.")
    @Test
    void getJobOffers() throws Exception {
        // given
        final JobOfferListResponse response = JobOfferListResponse.of(
            List.of(
                JobOfferListResponse.JobOfferResponse.of(
                    "OFFER-001",
                    JobType.SMALL_BIZ,
                    "OO 중소기업",
                    30_000_000,
                    45_000_000,
                    1
                ),
                JobOfferListResponse.JobOfferResponse.of(
                    "OFFER-002",
                    JobType.LARGE_BIZ,
                    "OO 대기업",
                    30_000_000,
                    45_000_000,
                    2
                )
            ),
            10,
            true
        );
        given(gameCareerService.getJobOffers(1L, 1001L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/games/sessions/{sessionId}/career/job-offers", 1001L)
                .with(currentUser())
                .header(AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.offers.length()").value(2))
            .andExpect(jsonPath("$.data.offers[0].offerId").value("OFFER-001"))
            .andExpect(jsonPath("$.data.offers[0].jobType").value("SMALL_BIZ"))
            .andExpect(jsonPath("$.data.offers[1].probationTurns").value(2))
            .andExpect(jsonPath("$.data.offerChanceBonusRate").value(10))
            .andExpect(jsonPath("$.data.meetFriendBonusApplied").value(true))
            .andDo(document("game-career/job-offers/success",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                apiResponseFields(
                    "이직 오퍼 목록",
                    fieldWithPath("offers").type(JsonFieldType.ARRAY).description("이직 오퍼 목록"),
                    fieldWithPath("offers[].offerId").type(JsonFieldType.STRING).description("오퍼 ID"),
                    fieldWithPath("offers[].jobType").type(JsonFieldType.STRING).description("이직 후 직업 유형"),
                    fieldWithPath("offers[].companyName").type(JsonFieldType.STRING).description("회사 표시 이름"),
                    fieldWithPath("offers[].currentSalary").type(JsonFieldType.NUMBER).description("현재 연봉"),
                    fieldWithPath("offers[].offeredSalary").type(JsonFieldType.NUMBER).description("제안 연봉"),
                    fieldWithPath("offers[].probationTurns").type(JsonFieldType.NUMBER).optional().description("수습 턴 수"),
                    fieldWithPath("offerChanceBonusRate").type(JsonFieldType.NUMBER).description("오퍼 확률 보너스"),
                    fieldWithPath("meetFriendBonusApplied").type(JsonFieldType.BOOLEAN).description("네트워킹 보너스 적용 여부")
                )
            ));
        then(gameCareerService).should().getJobOffers(1L, 1001L);
    }

    @DisplayName("이직 수락은 게임 코어 공개 계약으로 이직 결과를 반환한다.")
    @Test
    void transfer() throws Exception {
        // given
        final AcceptJobTransferRequest request = AcceptJobTransferRequest.of("OFFER-004");
        final JobTransferResponse response = JobTransferResponse.of(
            JobType.SMALL_BIZ,
            JobType.LARGE_BIZ,
            "수습/인턴",
            45_000_000,
            17,
            true,
            "OO 대기업으로 이직했습니다."
        );
        given(gameCareerService.transfer(eq(1L), eq(1001L), any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/games/sessions/{sessionId}/career/transfer", 1001L)
                .with(currentUser())
                .header(AUTHORIZATION, "Bearer access-token")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.previousJobType").value("SMALL_BIZ"))
            .andExpect(jsonPath("$.data.newJobType").value("LARGE_BIZ"))
            .andExpect(jsonPath("$.data.newJobTitle").value("수습/인턴"))
            .andExpect(jsonPath("$.data.newSalary").value(45_000_000))
            .andExpect(jsonPath("$.data.probationEndTurn").value(17))
            .andExpect(jsonPath("$.data.tenureReset").value(true))
            .andDo(document("game-career/transfer/success",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                requestFields(
                    fieldWithPath("offerId").type(JsonFieldType.STRING).description("수락할 오퍼 ID")
                ),
                apiResponseFields(
                    "이직 결과",
                    fieldWithPath("previousJobType").type(JsonFieldType.STRING).description("이전 직업 유형"),
                    fieldWithPath("newJobType").type(JsonFieldType.STRING).description("새 직업 유형"),
                    fieldWithPath("newJobTitle").type(JsonFieldType.STRING).description("새 직함"),
                    fieldWithPath("newSalary").type(JsonFieldType.NUMBER).description("새 연봉"),
                    fieldWithPath("probationEndTurn").type(JsonFieldType.NUMBER).optional().description("수습 종료 턴"),
                    fieldWithPath("tenureReset").type(JsonFieldType.BOOLEAN).description("근속 턴수 초기화 여부"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메시지")
                )
            ));
        then(gameCareerService).should().transfer(eq(1L), eq(1001L), any());
    }

    @DisplayName("빈 offerId로 이직 수락을 요청하면 400을 반환한다.")
    @Test
    void transferWithBlankOfferId() throws Exception {
        // given
        final AcceptJobTransferRequest request = AcceptJobTransferRequest.of(" ");

        // when & then
        mockMvc.perform(post("/api/games/sessions/{sessionId}/career/transfer", 1001L)
                .with(currentUser())
                .header(AUTHORIZATION, "Bearer access-token")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()))
            .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_INPUT_VALUE.getMessage()))
            .andDo(document("game-career/transfer/invalid-input",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                requestFields(
                    fieldWithPath("offerId").type(JsonFieldType.STRING).description("수락할 오퍼 ID")
                ),
                basicErrorResponseFields()
            ));
    }

    @DisplayName("커리어 provider 실패가 facade 오류로 매핑되면 500을 반환한다.")
    @Test
    void negotiateSalaryWithProviderFailure() throws Exception {
        // given
        given(gameCareerService.negotiateSalary(1L, 1001L))
            .willThrow(new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID));

        // when & then
        mockMvc.perform(post("/api/games/sessions/{sessionId}/career/negotiate", 1001L)
                .with(currentUser())
                .header(AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code").value(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID.getCode()))
            .andExpect(jsonPath("$.message").value(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID.getMessage()))
            .andDo(document("game-career/negotiate/provider-failure",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                basicErrorResponseFields()
            ));
    }
}
