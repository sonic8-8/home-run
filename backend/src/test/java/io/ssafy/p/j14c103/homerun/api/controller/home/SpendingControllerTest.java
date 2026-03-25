package io.ssafy.p.j14c103.homerun.api.controller.home;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.home.SpendingService;
import io.ssafy.p.j14c103.homerun.api.service.home.response.SpendingCategoryDetail;
import io.ssafy.p.j14c103.homerun.api.service.home.response.SpendingResponse;
import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.spending.SpendingCategory;
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

@WebMvcTest(SpendingController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.homerun.local", uriPort = 443)
class SpendingControllerTest extends RestDocsTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpendingService spendingService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @DisplayName("소비 분석 조회는 Principal 기반으로 ApiResponse를 반환한다")
    @Test
    void getSpending() throws Exception {
        // given
        final SpendingResponse response = SpendingResponse.of(
                "202603",
                Money.of(1_420_000L),
                List.of(SpendingCategoryDetail.of(
                        SpendingCategory.LIVING,
                        Money.of(450_000L),
                        Money.of(1_420_000L)
                ))
        );
        given(spendingService.getSpending(1L, "202603")).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/home/spending")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .param("month", "202603")
                        .with(currentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.month").value("202603"))
                .andExpect(jsonPath("$.data.totalExpense").value(1420000))
                .andExpect(jsonPath("$.data.categories[0].category").value("LIVING"))
                .andExpect(jsonPath("$.data.categories[0].categoryName").value("생활"))
                .andDo(document("home/spending/success",
                        requestHeaders(authorizationHeader()),
                        queryParameters(
                                parameterWithName("month").description("조회 월(yyyyMM)")
                        ),
                        apiResponseFields(
                                "월별 소비 분석 정보",
                                fieldWithPath("month").type(JsonFieldType.STRING).description("조회 월"),
                                fieldWithPath("totalExpense").type(JsonFieldType.NUMBER).description("총 지출 금액"),
                                fieldWithPath("categories").type(JsonFieldType.ARRAY).description("카테고리별 소비 목록"),
                                fieldWithPath("categories[].category").type(JsonFieldType.STRING).description("카테고리 코드"),
                                fieldWithPath("categories[].categoryName").type(JsonFieldType.STRING).description("카테고리 이름"),
                                fieldWithPath("categories[].amount").type(JsonFieldType.NUMBER).description("카테고리별 지출 금액"),
                                fieldWithPath("categories[].ratio").type(JsonFieldType.NUMBER).description("카테고리별 지출 비율")
                        )
                ));
    }
}
