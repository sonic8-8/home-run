package io.ssafy.p.j14c103.homerun.api.controller.game.session;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.game.session.EndingReportService;
import io.ssafy.p.j14c103.homerun.api.service.game.session.response.EndingReportResponse;
import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
import io.ssafy.p.j14c103.homerun.domain.gamesession.SessionStatus;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EndingReportController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.homerun.local", uriPort = 443)
class EndingReportControllerTest extends RestDocsTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EndingReportService endingReportService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @DisplayName("엔딩 리포트 조회는 요약과 world histories를 함께 반환한다")
    @Test
    void getEndingReport() throws Exception {
        given(endingReportService.getEndingReport(1L, 10L))
            .willReturn(createResponse());

        mockMvc.perform(get("/api/games/sessions/{sessionId}/ending", 10L)
                .with(currentUser())
                .header(AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.endingType").value("CLEAR"))
            .andExpect(jsonPath("$.data.spendingPattern.topCategory").value("식비"))
            .andExpect(jsonPath("$.data.achievements[0].name").value("첫 내집 마련"))
            .andExpect(jsonPath("$.data.newsHistories[0].newsId").value("NEWS-001"))
            .andExpect(jsonPath("$.data.eventHistories[0].gameEventId").value(2001))
            .andExpect(jsonPath("$.data.housingHistories[0].afterState.propertyId").value(301))
            .andExpect(jsonPath("$.data.housingSnapshot.currentHousingType").value("OWNED_APT"))
            .andDo(document("game-session/ending-report/success",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                relaxedApiResponseFields(
                    "엔딩 리포트와 세계관 이력",
                    fieldWithPath("endingType").description("엔딩 타입"),
                    fieldWithPath("grade").description("엔딩 등급"),
                    fieldWithPath("title").description("엔딩 타이틀"),
                    fieldWithPath("totalAssets").description("총자산"),
                    fieldWithPath("totalIncome").description("총수입"),
                    fieldWithPath("totalExpense").description("총지출"),
                    fieldWithPath("netProfit").description("순이익"),
                    fieldWithPath("spendingPattern.topCategory").description("최대 지출 카테고리"),
                    fieldWithPath("spendingPattern.topCategoryRatio").description("최대 지출 카테고리 비중"),
                    fieldWithPath("achievements").description("업적 목록"),
                    fieldWithPath("achievements[].name").description("업적 이름"),
                    fieldWithPath("achievements[].iconUrl").description("업적 아이콘 URL"),
                    fieldWithPath("newsHistories").description("뉴스 히스토리"),
                    fieldWithPath("eventHistories").description("이벤트 히스토리"),
                    fieldWithPath("housingHistories").description("주거 이동 이력"),
                    fieldWithPath("housingSnapshot.currentHousingType").description("현재 주거 형태"),
                    fieldWithPath("housingSnapshot.currentPropertyId").description("현재 매물 ID"),
                    fieldWithPath("housingSnapshot.targetPropertyId").description("목표 매물 ID")
                )
            ));
    }

    @DisplayName("존재하지 않는 세션이면 404를 반환한다")
    @Test
    void getEndingReportWithUnknownSession() throws Exception {
        willThrow(new HomerunException(ErrorCode.GAME_SESSION_NOT_FOUND))
            .given(endingReportService)
            .getEndingReport(1L, 999L);

        mockMvc.perform(get("/api/games/sessions/{sessionId}/ending", 999L)
                .with(currentUser())
                .header(AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_SESSION_NOT_FOUND.getCode()));
    }

    @DisplayName("엔딩 리포트가 아직 생성되지 않았으면 409를 반환한다")
    @Test
    void getEndingReportWhenNotReady() throws Exception {
        willThrow(new HomerunException(ErrorCode.ENDING_REPORT_NOT_READY))
            .given(endingReportService)
            .getEndingReport(1L, 10L);

        mockMvc.perform(get("/api/games/sessions/{sessionId}/ending", 10L)
                .with(currentUser())
                .header(AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(ErrorCode.ENDING_REPORT_NOT_READY.getCode()));
    }

    @DisplayName("다른 사용자의 세션이면 403을 반환한다")
    @Test
    void getEndingReportForbidden() throws Exception {
        willThrow(new HomerunException(ErrorCode.GAME_SESSION_FORBIDDEN))
            .given(endingReportService)
            .getEndingReport(1L, 10L);

        mockMvc.perform(get("/api/games/sessions/{sessionId}/ending", 10L)
                .with(currentUser())
                .header(AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_SESSION_FORBIDDEN.getCode()));
    }

    private EndingReportResponse createResponse() {
        return EndingReportResponse.create(
            SessionStatus.CLEAR,
            "S",
            "부동산 갑부",
            500_000_000L,
            120_000_000L,
            80_000_000L,
            40_000_000L,
            EndingReportResponse.SpendingPatternResponse.of("식비", BigDecimal.valueOf(35.2)),
            List.of(
                EndingReportResponse.AchievementResponse.of(
                    "첫 내집 마련",
                    "/images/badges/first-house.png"
                )
            ),
            List.of(
                EndingReportResponse.NewsHistoryResponse.of(
                    8,
                    "NEWS-001",
                    "금리 인하 기조 지속",
                    LocalDate.of(2026, 8, 1)
                )
            ),
            List.of(
                EndingReportResponse.EventHistoryResponse.of(
                    7,
                    2001,
                    "A",
                    "지원금을 신청했다.",
                    LocalDateTime.of(2026, 7, 18, 10, 30)
                )
            ),
            List.of(
                EndingReportResponse.HousingHistoryResponse.of(
                    12,
                    "자가 아파트를 마련했다.",
                    EndingReportResponse.HousingStateResponse.of(HousingType.VILLA, 202L),
                    EndingReportResponse.HousingStateResponse.of(HousingType.OWNED_APT, 301L)
                )
            ),
            EndingReportResponse.HousingSnapshotResponse.of(HousingType.OWNED_APT, 301L, 450L)
        );
    }
}
