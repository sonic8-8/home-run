package io.ssafy.p.j14c103.homerun.api.controller.game.realestate;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.game.realestate.RealEstatePropertyQueryService;
import io.ssafy.p.j14c103.homerun.api.service.game.realestate.response.RealEstatePropertyDetailResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.realestate.response.RealEstatePropertyListResponse;
import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
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

@WebMvcTest(RealEstatePropertyController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.homerun.local", uriPort = 443)
class RealEstatePropertyControllerTest extends RestDocsTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RealEstatePropertyQueryService realEstatePropertyQueryService;

    @DisplayName("세션 매물 목록 조회는 bounds 기준 ApiResponse를 반환한다")
    @Test
    void getProperties() throws Exception {
        // given
        given(realEstatePropertyQueryService.getProperties(1L, 1001L, "126.9000,37.4000,127.1000,37.6000"))
            .willReturn(sampleListResponse());

        // when & then
        mockMvc.perform(get("/api/games/sessions/{sessionId}/real-estate/properties", 1001L)
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                .queryParam("bounds", "126.9000,37.4000,127.1000,37.6000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.properties[0].propertyId").value(7L))
            .andExpect(jsonPath("$.data.properties[0].name").value("서초아트자이"))
            .andExpect(jsonPath("$.data.properties[1].recentPrice").value(980000000L))
            .andDo(document("real-estate/properties/list/success",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                queryParameters(
                    parameterWithName("bounds").description(
                        "지도 bounds. minLongitude,minLatitude,maxLongitude,maxLatitude 순서"
                    )
                ),
                apiResponseFields(
                    "세션 매물 목록",
                    fieldWithPath("properties").type(JsonFieldType.ARRAY).description("매물 목록"),
                    fieldWithPath("properties[].propertyId").type(JsonFieldType.NUMBER).description("매물 ID"),
                    fieldWithPath("properties[].name").type(JsonFieldType.STRING).description("매물 이름"),
                    fieldWithPath("properties[].recentPrice").type(JsonFieldType.NUMBER).description("현재 시세"),
                    fieldWithPath("properties[].latitude").type(JsonFieldType.NUMBER).description("위도"),
                    fieldWithPath("properties[].longitude").type(JsonFieldType.NUMBER).description("경도")
                )
            ));
        then(realEstatePropertyQueryService).should()
            .getProperties(1L, 1001L, "126.9000,37.4000,127.1000,37.6000");
    }

    @DisplayName("세션 매물 목록 조회는 다른 사용자의 세션이면 403을 반환한다")
    @Test
    void getPropertiesForbidden() throws Exception {
        // given
        given(realEstatePropertyQueryService.getProperties(1L, 88L, null))
            .willThrow(new HomerunException(ErrorCode.GAME_SESSION_FORBIDDEN));

        // when & then
        mockMvc.perform(get("/api/games/sessions/{sessionId}/real-estate/properties", 88L)
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_SESSION_FORBIDDEN.getCode()))
            .andDo(document("real-estate/properties/list/forbidden",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                queryParameters(
                    parameterWithName("bounds").optional().description(
                        "지도 bounds. minLongitude,minLatitude,maxLongitude,maxLatitude 순서"
                    )
                ),
                basicErrorResponseFields()
            ));
    }

    @DisplayName("세션 매물 상세 조회는 ApiResponse를 반환한다")
    @Test
    void getPropertyDetail() throws Exception {
        // given
        given(realEstatePropertyQueryService.getPropertyDetail(1L, 1001L, 7L))
            .willReturn(sampleDetailResponse());

        // when & then
        mockMvc.perform(get("/api/games/sessions/{sessionId}/real-estate/properties/{propertyId}", 1001L, 7L)
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.propertyId").value(7L))
            .andExpect(jsonPath("$.data.name").value("서초아트자이"))
            .andExpect(jsonPath("$.data.housingType").value("OWNED_APT"))
            .andDo(document("real-estate/properties/detail/success",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID"),
                    parameterWithName("propertyId").description("부동산 매물 ID")
                ),
                apiResponseFields(
                    "세션 매물 상세 정보",
                    fieldWithPath("propertyId").type(JsonFieldType.NUMBER).description("매물 ID"),
                    fieldWithPath("name").type(JsonFieldType.STRING).description("매물 이름"),
                    fieldWithPath("recentPrice").type(JsonFieldType.NUMBER).description("현재 시세"),
                    fieldWithPath("address").type(JsonFieldType.STRING).description("주소"),
                    fieldWithPath("latitude").type(JsonFieldType.NUMBER).description("위도"),
                    fieldWithPath("longitude").type(JsonFieldType.NUMBER).description("경도"),
                    fieldWithPath("housingType").type(JsonFieldType.STRING).description("주거 유형")
                )
            ));
        then(realEstatePropertyQueryService).should().getPropertyDetail(1L, 1001L, 7L);
    }

    @DisplayName("존재하지 않는 매물 상세 조회는 404를 반환한다")
    @Test
    void getPropertyDetailWithUnknownProperty() throws Exception {
        // given
        given(realEstatePropertyQueryService.getPropertyDetail(1L, 1001L, 999L))
            .willThrow(new HomerunException(ErrorCode.HOUSING_PROPERTY_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/games/sessions/{sessionId}/real-estate/properties/{propertyId}", 1001L, 999L)
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ErrorCode.HOUSING_PROPERTY_NOT_FOUND.getCode()))
            .andDo(document("real-estate/properties/detail/not-found",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID"),
                    parameterWithName("propertyId").description("부동산 매물 ID")
                ),
                basicErrorResponseFields()
            ));
    }

    private RealEstatePropertyListResponse sampleListResponse() {
        return RealEstatePropertyListResponse.from(List.of(
            RealEstatePropertyListResponse.PropertySummaryResponse.of(
                7L,
                "서초아트자이",
                1_300_000_000L,
                BigDecimal.valueOf(37.485551),
                BigDecimal.valueOf(127.011500)
            ),
            RealEstatePropertyListResponse.PropertySummaryResponse.of(
                8L,
                "반포리체",
                980_000_000L,
                BigDecimal.valueOf(37.503245),
                BigDecimal.valueOf(127.004812)
            )
        ));
    }

    private RealEstatePropertyDetailResponse sampleDetailResponse() {
        return RealEstatePropertyDetailResponse.of(
            7L,
            "서초아트자이",
            1_300_000_000L,
            "서울특별시 서초구 반포대로 58",
            BigDecimal.valueOf(37.485551),
            BigDecimal.valueOf(127.011500),
            HousingType.OWNED_APT
        );
    }
}
