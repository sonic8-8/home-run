package io.ssafy.p.j14c103.homerun.api.controller.game.turn;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.game.turn.GameTurnNewsService;
import io.ssafy.p.j14c103.homerun.api.service.world.response.LatestTurnNewsResponse;
import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.LocalDate;
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

@WebMvcTest(GameTurnNewsController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.homerun.local", uriPort = 443)
class GameTurnNewsControllerTest extends RestDocsTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameTurnNewsService gameTurnNewsService;

    @DisplayName("최신 턴 뉴스 조회는 게임 코어 응답 계약으로 최신 뉴스 정보를 반환한다.")
    @Test
    void getLatestTurnNews() throws Exception {
        // given
        final LatestTurnNewsResponse response = LatestTurnNewsResponse.of(
            12,
            LocalDate.of(2026, 1, 1),
            List.of(LatestTurnNewsResponse.NewsItemResponse.of(
                "01500801.20200519071906001",
                "부동산 시장 과열 경고",
                "시장 과열 신호가 확인됐다.",
                "영남일보",
                LocalDate.of(2026, 1, 1),
                "BOOM_TO_CRISIS"
            ))
        );
        given(gameTurnNewsService.getLatestTurnNews(1L, 1001L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/games/sessions/{sessionId}/news/latest", 1001L)
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data.turnNumber").value(12))
            .andExpect(jsonPath("$.data.currentDate").value("2026-01-01"))
            .andExpect(jsonPath("$.data.news").isArray())
            .andExpect(jsonPath("$.data.news.length()").value(1))
            .andExpect(jsonPath("$.data.news[0].newsId").value("01500801.20200519071906001"))
            .andExpect(jsonPath("$.data.news[0].headline").value("부동산 시장 과열 경고"))
            .andExpect(jsonPath("$.data.news[0].content").value("시장 과열 신호가 확인됐다."))
            .andExpect(jsonPath("$.data.news[0].sourceName").value("영남일보"))
            .andExpect(jsonPath("$.data.news[0].publishedDate").value("2026-01-01"))
            .andExpect(jsonPath("$.data.news[0].economicCycleType").value("BOOM_TO_CRISIS"))
            .andDo(document("game-turn/news/latest/success",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                apiResponseFields(
                    "최신 턴 뉴스 정보",
                    fieldWithPath("turnNumber").type(JsonFieldType.NUMBER).description("현재 턴 번호"),
                    fieldWithPath("currentDate").type(JsonFieldType.STRING).description("현재 날짜"),
                    fieldWithPath("news").type(JsonFieldType.ARRAY).description("최신 턴 뉴스 목록"),
                    fieldWithPath("news[].newsId").type(JsonFieldType.STRING).description("뉴스 식별자"),
                    fieldWithPath("news[].headline").type(JsonFieldType.STRING).description("뉴스 제목"),
                    fieldWithPath("news[].content").type(JsonFieldType.STRING).description("뉴스 본문"),
                    fieldWithPath("news[].sourceName").type(JsonFieldType.STRING).description("뉴스 출처"),
                    fieldWithPath("news[].publishedDate").type(JsonFieldType.STRING).description("뉴스 발행일"),
                    fieldWithPath("news[].economicCycleType").type(JsonFieldType.STRING).description("경제 사이클 전환 유형")
                )
            ));
        then(gameTurnNewsService).should().getLatestTurnNews(1L, 1001L);
    }

    @DisplayName("다른 사용자의 세션 최신 턴 뉴스 조회는 403을 반환한다.")
    @Test
    void getOtherUsersLatestTurnNews() throws Exception {
        // given
        given(gameTurnNewsService.getLatestTurnNews(1L, 88L))
            .willThrow(new HomerunException(ErrorCode.GAME_SESSION_FORBIDDEN));

        // when & then
        mockMvc.perform(get("/api/games/sessions/{sessionId}/news/latest", 88L)
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_SESSION_FORBIDDEN.getCode()))
            .andExpect(jsonPath("$.message").value(ErrorCode.GAME_SESSION_FORBIDDEN.getMessage()))
            .andDo(document("game-turn/news/latest/forbidden",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                basicErrorResponseFields()
            ));
    }

    @DisplayName("존재하지 않는 세션의 최신 턴 뉴스 조회는 404를 반환한다.")
    @Test
    void getUnknownLatestTurnNews() throws Exception {
        // given
        given(gameTurnNewsService.getLatestTurnNews(1L, 9999L))
            .willThrow(new HomerunException(ErrorCode.GAME_SESSION_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/games/sessions/{sessionId}/news/latest", 9999L)
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_SESSION_NOT_FOUND.getCode()))
            .andExpect(jsonPath("$.message").value(ErrorCode.GAME_SESSION_NOT_FOUND.getMessage()))
            .andDo(document("game-turn/news/latest/not-found",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                basicErrorResponseFields()
            ));
    }
}
