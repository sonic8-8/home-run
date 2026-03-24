package io.ssafy.p.j14c103.homerun.api.controller.game.realestate;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.game.realestate.RealEstateDocumentService;
import io.ssafy.p.j14c103.homerun.api.service.game.realestate.response.RealEstateDocumentResponse;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RealEstateDocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
class RealEstateDocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RealEstateDocumentService realEstateDocumentService;

    @DisplayName("등기부등본 조회는 ApiResponse로 감싼 문서 데이터를 반환한다")
    @Test
    void getDocuments() throws Exception {
        // given
        given(realEstateDocumentService.getDocument(1001L, 7L)).willReturn(sampleResponse());

        // when & then
        mockMvc.perform(get("/api/games/sessions/1001/real-estate/properties/7/documents"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.propertyId").value(7L))
            .andExpect(jsonPath("$.data.propertyName").value("서초아트자이"))
            .andExpect(jsonPath("$.data.gapguRows[0].purpose").value("소유권보존"))
            .andExpect(jsonPath("$.data.eulguRows[0].details").value("채권최고액 금195,000,000원 채무자 김도윤 근저당권자 주식회사 한울저축은행"))
            .andExpect(jsonPath("$.data.solution.verdict").value("위험"))
            .andExpect(jsonPath("$.data.solution.gapgu.verdict").value("위험"))
            .andExpect(jsonPath("$.data.solution.eulgu.verdict").value("정상"));
    }

    @DisplayName("존재하지 않는 부동산 매물이면 에러 응답을 반환한다")
    @Test
    void getDocumentsWithUnknownProperty() throws Exception {
        // given
        given(realEstateDocumentService.getDocument(1001L, 999L))
            .willThrow(new HomerunException(ErrorCode.HOUSING_PROPERTY_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/games/sessions/1001/real-estate/properties/999/documents"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("HOUSING_001"))
            .andExpect(jsonPath("$.message").value("존재하지 않는 부동산 매물입니다."));
    }

    @DisplayName("존재하지 않는 게임 세션이면 에러 응답을 반환한다")
    @Test
    void getDocumentsWithUnknownSession() throws Exception {
        // given
        given(realEstateDocumentService.getDocument(9999L, 7L))
            .willThrow(new HomerunException(ErrorCode.WORLD_SESSION_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/games/sessions/9999/real-estate/properties/7/documents"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("WORLD_001"))
            .andExpect(jsonPath("$.message").value("존재하지 않는 게임 세션입니다."));
    }

    private RealEstateDocumentResponse sampleResponse() {
        return RealEstateDocumentResponse.of(
            7L,
            "서초아트자이",
            "서울특별시 서초구 반포대로 58",
            BigDecimal.valueOf(37.485551),
            BigDecimal.valueOf(127.011500),
            1_300_000_000L,
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
