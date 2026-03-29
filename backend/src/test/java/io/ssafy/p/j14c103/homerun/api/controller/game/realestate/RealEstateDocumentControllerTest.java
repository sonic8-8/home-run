package io.ssafy.p.j14c103.homerun.api.controller.game.realestate;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.game.realestate.RealEstateDocumentService;
import io.ssafy.p.j14c103.homerun.api.service.game.realestate.response.RealEstateDocumentResponse;
import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
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

@WebMvcTest(RealEstateDocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.homerun.local", uriPort = 443)
class RealEstateDocumentControllerTest extends RestDocsTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RealEstateDocumentService realEstateDocumentService;

    @DisplayName("등기부등본 조회는 ApiResponse로 감싼 문서 데이터를 반환한다")
    @Test
    void getDocuments() throws Exception {
        // given
        given(realEstateDocumentService.getDocument(1L, 1001L, 7L)).willReturn(sampleResponse());

        // when & then
        mockMvc.perform(get(
                        "/api/games/sessions/{sessionId}/real-estate/properties/{propertyId}/documents",
                        1001L,
                        7L
                )
                        .with(currentUser())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.propertyId").value(7L))
            .andExpect(jsonPath("$.data.propertyName").value("서초아트자이"))
            .andExpect(jsonPath("$.data.gapguRows[0].purpose").value("소유권보존"))
            .andExpect(jsonPath("$.data.eulguRows[0].details").value("채권최고액 금195,000,000원 채무자 김도윤 근저당권자 주식회사 한울저축은행"))
            .andExpect(jsonPath("$.data.checklistItems[0].trapId").value("TRAP-HN-001"))
            .andExpect(jsonPath("$.data.checklistItems[0].label").value("소유권 변동 이력 확인"))
            .andExpect(jsonPath("$.data.solution.verdict").value("위험"))
            .andExpect(jsonPath("$.data.solution.gapgu.verdict").value("위험"))
            .andExpect(jsonPath("$.data.solution.eulgu.verdict").value("정상"))
            .andDo(document("real-estate/documents/success",
                    requestHeaders(authorizationHeader()),
                    pathParameters(
                            parameterWithName("sessionId").description("게임 세션 ID"),
                            parameterWithName("propertyId").description("부동산 매물 ID")
                    ),
                    relaxedApiResponseFields(
                            "등기부등본 정보",
                            fieldWithPath("propertyId").type(JsonFieldType.NUMBER).description("부동산 매물 ID"),
                            fieldWithPath("propertyName").type(JsonFieldType.STRING).description("부동산 이름"),
                            fieldWithPath("address").type(JsonFieldType.STRING).description("주소"),
                            fieldWithPath("latitude").type(JsonFieldType.NUMBER).description("위도"),
                            fieldWithPath("longitude").type(JsonFieldType.NUMBER).description("경도"),
                            fieldWithPath("salePrice").type(JsonFieldType.NUMBER).description("매매가"),
                            fieldWithPath("gapguRows").type(JsonFieldType.ARRAY).description("갑구 등기 행 목록"),
                            fieldWithPath("gapguRows[].rankNo").type(JsonFieldType.STRING).description("갑구 순위 번호"),
                            fieldWithPath("gapguRows[].purpose").type(JsonFieldType.STRING).description("갑구 권리 목적"),
                            fieldWithPath("gapguRows[].receipt").type(JsonFieldType.STRING).description("갑구 접수일"),
                            fieldWithPath("gapguRows[].reason").type(JsonFieldType.STRING).description("갑구 원인"),
                            fieldWithPath("gapguRows[].details").type(JsonFieldType.STRING).description("갑구 상세 내용"),
                            fieldWithPath("eulguRows").type(JsonFieldType.ARRAY).description("을구 등기 행 목록"),
                            fieldWithPath("eulguRows[].rankNo").type(JsonFieldType.STRING).description("을구 순위 번호"),
                            fieldWithPath("eulguRows[].purpose").type(JsonFieldType.STRING).description("을구 권리 목적"),
                            fieldWithPath("eulguRows[].receipt").type(JsonFieldType.STRING).description("을구 접수일"),
                            fieldWithPath("eulguRows[].reason").type(JsonFieldType.STRING).description("을구 원인"),
                            fieldWithPath("eulguRows[].details").type(JsonFieldType.STRING).description("을구 상세 내용"),
                            fieldWithPath("checklistItems").type(JsonFieldType.ARRAY).description("계약 검토 체크리스트 항목"),
                            fieldWithPath("checklistItems[].trapId").type(JsonFieldType.STRING).description("계약 검토 제출에 사용하는 체크리스트 항목 ID"),
                            fieldWithPath("checklistItems[].label").type(JsonFieldType.STRING).description("사용자에게 노출하는 체크리스트 항목 문구"),
                            fieldWithPath("solution").type(JsonFieldType.OBJECT).description("위험도 해설 정보"),
                            fieldWithPath("solution.verdict").type(JsonFieldType.STRING).description("종합 판정"),
                            fieldWithPath("solution.gapgu").type(JsonFieldType.OBJECT).description("갑구 해설"),
                            fieldWithPath("solution.gapgu.verdict").type(JsonFieldType.STRING).description("갑구 판정"),
                            fieldWithPath("solution.gapgu.issueSummary").type(JsonFieldType.STRING).description("갑구 요약"),
                            fieldWithPath("solution.gapgu.keyPoints").type(JsonFieldType.ARRAY).description("갑구 핵심 포인트"),
                            fieldWithPath("solution.gapgu.feedbackCorrect").type(JsonFieldType.STRING).description("갑구 정답 피드백"),
                            fieldWithPath("solution.gapgu.feedbackWrong").type(JsonFieldType.STRING).description("갑구 오답 피드백"),
                            fieldWithPath("solution.eulgu").type(JsonFieldType.OBJECT).description("을구 해설"),
                            fieldWithPath("solution.eulgu.verdict").type(JsonFieldType.STRING).description("을구 판정"),
                            fieldWithPath("solution.eulgu.issueSummary").type(JsonFieldType.STRING).description("을구 요약"),
                            fieldWithPath("solution.eulgu.keyPoints").type(JsonFieldType.ARRAY).description("을구 핵심 포인트"),
                            fieldWithPath("solution.eulgu.feedbackCorrect").type(JsonFieldType.STRING).description("을구 정답 피드백"),
                            fieldWithPath("solution.eulgu.feedbackWrong").type(JsonFieldType.STRING).description("을구 오답 피드백")
                    )
            ));
    }

    @DisplayName("존재하지 않는 부동산 매물이면 에러 응답을 반환한다")
    @Test
    void getDocumentsWithUnknownProperty() throws Exception {
        // given
        given(realEstateDocumentService.getDocument(1L, 1001L, 999L))
            .willThrow(new HomerunException(ErrorCode.HOUSING_PROPERTY_NOT_FOUND));

        // when & then
        mockMvc.perform(get(
                        "/api/games/sessions/{sessionId}/real-estate/properties/{propertyId}/documents",
                        1001L,
                        999L
                )
                        .with(currentUser())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("HOUSING_001"))
            .andExpect(jsonPath("$.message").value("존재하지 않는 부동산 매물입니다."))
            .andDo(document("real-estate/documents/property-not-found",
                    requestHeaders(authorizationHeader()),
                    pathParameters(
                            parameterWithName("sessionId").description("게임 세션 ID"),
                            parameterWithName("propertyId").description("부동산 매물 ID")
                    ),
                    basicErrorResponseFields()
            ));
    }

    @DisplayName("존재하지 않는 게임 세션이면 에러 응답을 반환한다")
    @Test
    void getDocumentsWithUnknownSession() throws Exception {
        // given
        given(realEstateDocumentService.getDocument(1L, 9999L, 7L))
            .willThrow(new HomerunException(ErrorCode.GAME_SESSION_NOT_FOUND));

        // when & then
        mockMvc.perform(get(
                        "/api/games/sessions/{sessionId}/real-estate/properties/{propertyId}/documents",
                        9999L,
                        7L
                )
                        .with(currentUser())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("GAME_004"))
            .andExpect(jsonPath("$.message").value("존재하지 않는 게임 세션입니다."))
            .andDo(document("real-estate/documents/session-not-found",
                    requestHeaders(authorizationHeader()),
                    pathParameters(
                            parameterWithName("sessionId").description("게임 세션 ID"),
                            parameterWithName("propertyId").description("부동산 매물 ID")
                    ),
                    basicErrorResponseFields()
            ));
    }

    @DisplayName("다른 사용자의 세션이면 에러 응답을 반환한다")
    @Test
    void getDocumentsForbidden() throws Exception {
        // given
        given(realEstateDocumentService.getDocument(1L, 88L, 7L))
            .willThrow(new HomerunException(ErrorCode.GAME_SESSION_FORBIDDEN));

        // when & then
        mockMvc.perform(get(
                        "/api/games/sessions/{sessionId}/real-estate/properties/{propertyId}/documents",
                        88L,
                        7L
                )
                        .with(currentUser())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("GAME_002"))
            .andExpect(jsonPath("$.message").value("해당 게임 세션에 접근할 수 없습니다."))
            .andDo(document("real-estate/documents/forbidden",
                    requestHeaders(authorizationHeader()),
                    pathParameters(
                            parameterWithName("sessionId").description("게임 세션 ID"),
                            parameterWithName("propertyId").description("부동산 매물 ID")
                    ),
                    basicErrorResponseFields()
            ));
    }

    private RealEstateDocumentResponse sampleResponse() {
        return RealEstateDocumentResponse.of(
            7L,
            "서초아트자이",
            "서울특별시 서초구 반포대로 58",
            BigDecimal.valueOf(37.485551),
            BigDecimal.valueOf(127.011500),
            1_300_000_000L,
            "등기사항전부증명서",
            List.of(
                RealEstateDocumentResponse.RegistryRowResponse.of(
                    "1",
                    "소유권보존",
                    "2021년 3월 15일",
                    "보존",
                    "소유자 주식회사 청명하우징"
                )
            ),
            List.of(
                RealEstateDocumentResponse.RegistryRowResponse.of(
                    "1",
                    "근저당권설정",
                    "2025년 1월 17일",
                    "2025년 1월 10일 설정계약",
                    "채권최고액 금195,000,000원 채무자 김도윤 근저당권자 주식회사 한울저축은행"
                )
            ),
            List.of(
                RealEstateDocumentResponse.ChecklistItemResponse.of(
                    "TRAP-HN-001",
                    "소유권 변동 이력 확인"
                ),
                RealEstateDocumentResponse.ChecklistItemResponse.of(
                    "CHECK-HN-001",
                    "가등기 말소 여부 확인"
                )
            ),
            RealEstateDocumentResponse.SolutionResponse.of(
                "위험",
                RealEstateDocumentResponse.SectionSolutionResponse.of(
                    "위험",
                    "갑구 해설",
                    List.of("갑구 포인트"),
                    "갑구 정답 해설",
                    "갑구 오답 해설"
                ),
                RealEstateDocumentResponse.SectionSolutionResponse.of(
                    "정상",
                    "을구 해설",
                    List.of("을구 포인트"),
                    "을구 정답 해설",
                    "을구 오답 해설"
                )
            )
        );
    }
}
