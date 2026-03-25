package io.ssafy.p.j14c103.homerun.api.controller.home;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.home.CreditScoreService;
import io.ssafy.p.j14c103.homerun.api.service.home.credit.CreditScore;
import io.ssafy.p.j14c103.homerun.api.service.home.response.CreditScoreResponse;
import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
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

@WebMvcTest(CreditScoreController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.homerun.local", uriPort = 443)
class CreditScoreControllerTest extends RestDocsTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreditScoreService creditScoreService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @DisplayName("신용 점수 조회는 ApiResponse로 감싼 신용 정보를 반환한다")
    @Test
    void getCreditScore() throws Exception {
        // given
        final CreditScoreResponse response = CreditScoreResponse.of(
                CreditScore.of(300, 250, 120, 90, 80),
                "A",
                12_345_678L
        );
        given(creditScoreService.getCreditScore(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/credit/score")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .with(currentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.score").value(840))
                .andExpect(jsonPath("$.data.grade").value(2))
                .andExpect(jsonPath("$.data.ratingName").value("A"))
                .andExpect(jsonPath("$.data.totalAsset").value(12345678))
                .andDo(document("credit/score/success",
                        requestHeaders(authorizationHeader()),
                        apiResponseFields(
                                "신용 점수 정보",
                                fieldWithPath("score").type(JsonFieldType.NUMBER).description("신용 점수"),
                                fieldWithPath("grade").type(JsonFieldType.NUMBER).description("신용 등급"),
                                fieldWithPath("gradeLabel").type(JsonFieldType.STRING).description("신용 등급 라벨"),
                                fieldWithPath("paymentHistory").type(JsonFieldType.NUMBER).description("상환 이력 점수"),
                                fieldWithPath("amountsOwed").type(JsonFieldType.NUMBER).description("부채 수준 점수"),
                                fieldWithPath("creditLength").type(JsonFieldType.NUMBER).description("신용 이력 길이 점수"),
                                fieldWithPath("creditMix").type(JsonFieldType.NUMBER).description("신용 구성 점수"),
                                fieldWithPath("newCredit").type(JsonFieldType.NUMBER).description("신규 신용 점수"),
                                fieldWithPath("ratingName").type(JsonFieldType.STRING).description("외부 신용도 등급명"),
                                fieldWithPath("totalAsset").type(JsonFieldType.NUMBER).description("총자산"),
                                fieldWithPath("totalDebt").type(JsonFieldType.NUMBER).description("총부채"),
                                fieldWithPath("netAsset").type(JsonFieldType.NUMBER).description("순자산")
                        )
                ));
    }
}
