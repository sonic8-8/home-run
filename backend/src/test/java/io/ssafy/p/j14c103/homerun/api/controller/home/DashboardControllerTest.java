package io.ssafy.p.j14c103.homerun.api.controller.home;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.home.DashboardService;
import io.ssafy.p.j14c103.homerun.api.service.home.response.DashboardResponse;
import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.homerun.local", uriPort = 443)
class DashboardControllerTest extends RestDocsTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @DisplayName("대시보드 조회는 ApiResponse로 감싼 데이터를 반환한다")
    @Test
    void getDashboard() throws Exception {
        // given
        final DashboardResponse response = DashboardResponse.of(
                Money.of(42_300_000L),
                Money.of(2_800_000L),
                Money.of(1_420_000L),
                Money.zero(),
                Money.of(220_000L),
                7,
                Money.of(3_200_000L),
                Money.of(1_100_000L)
        );
        given(dashboardService.getDashboard(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/home/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .with(currentUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.totalAssets").value(42300000))
                .andExpect(jsonPath("$.data.monthlyIncome").value(2800000))
                .andExpect(jsonPath("$.data.mainAccountBalance").value(3200000))
                .andExpect(jsonPath("$.data.seedmoneyBalance").value(1100000))
                .andExpect(jsonPath("$.data.nextPaydayDays").value(7))
                .andDo(document("home/dashboard/success",
                        requestHeaders(authorizationHeader()),
                        apiResponseFields(
                                "대시보드 정보",
                                fieldWithPath("totalAssets").type(JsonFieldType.NUMBER).description("총자산"),
                                fieldWithPath("monthlyIncome").type(JsonFieldType.NUMBER).description("월 수입"),
                                fieldWithPath("monthlyExpense").type(JsonFieldType.NUMBER).description("월 지출"),
                                fieldWithPath("incomeChangeFromLastMonth").type(JsonFieldType.NUMBER).description("지난달 대비 수입 변화"),
                                fieldWithPath("expenseChangeFromLastMonth").type(JsonFieldType.NUMBER).description("지난달 대비 지출 변화"),
                                fieldWithPath("nextPaydayDays").type(JsonFieldType.NUMBER).description("다음 급여일까지 남은 일수"),
                                fieldWithPath("mainAccountBalance").type(JsonFieldType.NUMBER).description("메인 계좌 잔액"),
                                fieldWithPath("seedmoneyBalance").type(JsonFieldType.NUMBER).description("시드머니 계좌 잔액")
                        )
                ));
    }
}
