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

import io.ssafy.p.j14c103.homerun.api.service.game.session.EndingLogsService;
import io.ssafy.p.j14c103.homerun.api.service.game.session.response.GameTimelineResponse;
import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.LocalDate;
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

@WebMvcTest(EndingLogsController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.homerun.local", uriPort = 443)
class EndingLogsControllerTest extends RestDocsTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EndingLogsService endingLogsService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @DisplayName("엔딩 로그 조회는 turn 오름차순 timeline을 반환한다")
    @Test
    void getLogs() throws Exception {
        given(endingLogsService.getLogs(1L, 10L))
            .willReturn(createResponse());

        mockMvc.perform(get("/api/games/sessions/{sessionId}/logs", 10L)
                .with(currentUser())
                .header(AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.timeline.length()").value(2))
            .andExpect(jsonPath("$.data.timeline[0].turnNumber").value(1))
            .andExpect(jsonPath("$.data.timeline[1].turnNumber").value(12))
            .andDo(document("game-session/ending-logs/success",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                relaxedApiResponseFields(
                    "엔딩 로그 타임라인",
                    fieldWithPath("timeline").description("턴별 자산 시계열"),
                    fieldWithPath("timeline[].turnNumber").description("턴 번호"),
                    fieldWithPath("timeline[].date").description("기록 날짜"),
                    fieldWithPath("timeline[].cash").description("현금"),
                    fieldWithPath("timeline[].netAssets").description("순자산"),
                    fieldWithPath("timeline[].totalAssets").description("총자산"),
                    fieldWithPath("timeline[].stockValue").description("주식 평가금액"),
                    fieldWithPath("timeline[].loanBalance").description("대출 잔액"),
                    fieldWithPath("timeline[].salary").description("월급")
                )
            ));
    }

    @DisplayName("존재하지 않는 세션이면 404를 반환한다")
    @Test
    void getLogsWithUnknownSession() throws Exception {
        willThrow(new HomerunException(ErrorCode.GAME_SESSION_NOT_FOUND))
            .given(endingLogsService)
            .getLogs(1L, 999L);

        mockMvc.perform(get("/api/games/sessions/{sessionId}/logs", 999L)
                .with(currentUser())
                .header(AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_SESSION_NOT_FOUND.getCode()));
    }

    @DisplayName("엔딩 로그가 아직 준비되지 않았으면 409를 반환한다")
    @Test
    void getLogsWhenNotReady() throws Exception {
        willThrow(new HomerunException(ErrorCode.ENDING_REPORT_NOT_READY))
            .given(endingLogsService)
            .getLogs(1L, 10L);

        mockMvc.perform(get("/api/games/sessions/{sessionId}/logs", 10L)
                .with(currentUser())
                .header(AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(ErrorCode.ENDING_REPORT_NOT_READY.getCode()));
    }

    @DisplayName("다른 사용자의 세션이면 403을 반환한다")
    @Test
    void getLogsForbidden() throws Exception {
        willThrow(new HomerunException(ErrorCode.GAME_SESSION_FORBIDDEN))
            .given(endingLogsService)
            .getLogs(1L, 10L);

        mockMvc.perform(get("/api/games/sessions/{sessionId}/logs", 10L)
                .with(currentUser())
                .header(AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_SESSION_FORBIDDEN.getCode()));
    }

    private GameTimelineResponse createResponse() {
        return GameTimelineResponse.of(List.of(
            GameTimelineResponse.TimelineItemResponse.of(
                1,
                LocalDate.of(2026, 1, 1),
                13_000_000L,
                13_000_000L,
                13_000_000L,
                0L,
                0L,
                2_000_000L
            ),
            GameTimelineResponse.TimelineItemResponse.of(
                12,
                LocalDate.of(2026, 12, 1),
                5_000_000L,
                25_000_000L,
                75_000_000L,
                5_000_000L,
                50_000_000L,
                2_200_000L
            )
        ));
    }
}
