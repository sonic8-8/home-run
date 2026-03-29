package io.ssafy.p.j14c103.homerun.api.controller.game.news;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.game.news.GameNewsHistoryService;
import io.ssafy.p.j14c103.homerun.api.service.game.news.response.GameNewsHistoryResponse;
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

@WebMvcTest(GameNewsController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.homerun.local", uriPort = 443)
class GameNewsControllerTest extends RestDocsTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameNewsHistoryService gameNewsHistoryService;

    @DisplayName("뉴스 히스토리 조회는 게임 코어 응답 계약으로 지난 뉴스 목록을 반환한다.")
    @Test
    void getNewsHistory() throws Exception {
        // given
        final GameNewsHistoryResponse response = GameNewsHistoryResponse.of(
            List.of(
                GameNewsHistoryResponse.NewsHistoryResponse.of(
                    12,
                    "NEWS-012",
                    "채용 한파 심화",
                    LocalDate.of(2026, 1, 1)
                ),
                GameNewsHistoryResponse.NewsHistoryResponse.of(
                    11,
                    "NEWS-011",
                    "기준금리 동결",
                    LocalDate.of(2025, 12, 1)
                )
            )
        );
        given(gameNewsHistoryService.getNewsHistory(1L, 1001L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/games/sessions/{sessionId}/news/history", 1001L)
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data.newsHistories").isArray())
            .andExpect(jsonPath("$.data.newsHistories.length()").value(2))
            .andExpect(jsonPath("$.data.newsHistories[0].turnNumber").value(12))
            .andExpect(jsonPath("$.data.newsHistories[0].newsId").value("NEWS-012"))
            .andExpect(jsonPath("$.data.newsHistories[0].headline").value("채용 한파 심화"))
            .andExpect(jsonPath("$.data.newsHistories[0].publishedDate").value("2026-01-01"))
            .andExpect(jsonPath("$.data.newsHistories[1].turnNumber").value(11))
            .andDo(document("game-news/history/success",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                apiResponseFields(
                    "뉴스 히스토리 정보",
                    fieldWithPath("newsHistories").type(JsonFieldType.ARRAY).description("세션 뉴스 히스토리"),
                    fieldWithPath("newsHistories[].turnNumber").type(JsonFieldType.NUMBER).description("뉴스가 노출된 턴 번호"),
                    fieldWithPath("newsHistories[].newsId").type(JsonFieldType.STRING).description("뉴스 식별자"),
                    fieldWithPath("newsHistories[].headline").type(JsonFieldType.STRING).description("뉴스 제목"),
                    fieldWithPath("newsHistories[].publishedDate").type(JsonFieldType.STRING).description("뉴스 발행일")
                )
            ));
        then(gameNewsHistoryService).should().getNewsHistory(1L, 1001L);
    }

    @DisplayName("다른 사용자의 뉴스 히스토리 조회는 403을 반환한다.")
    @Test
    void getOtherUsersNewsHistory() throws Exception {
        // given
        given(gameNewsHistoryService.getNewsHistory(1L, 88L))
            .willThrow(new HomerunException(ErrorCode.GAME_SESSION_FORBIDDEN));

        // when & then
        mockMvc.perform(get("/api/games/sessions/{sessionId}/news/history", 88L)
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_SESSION_FORBIDDEN.getCode()))
            .andExpect(jsonPath("$.message").value(ErrorCode.GAME_SESSION_FORBIDDEN.getMessage()))
            .andDo(document("game-news/history/forbidden",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                basicErrorResponseFields()
            ));
    }

    @DisplayName("존재하지 않는 세션의 뉴스 히스토리 조회는 404를 반환한다.")
    @Test
    void getUnknownNewsHistory() throws Exception {
        // given
        given(gameNewsHistoryService.getNewsHistory(1L, 9999L))
            .willThrow(new HomerunException(ErrorCode.GAME_SESSION_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/games/sessions/{sessionId}/news/history", 9999L)
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_SESSION_NOT_FOUND.getCode()))
            .andExpect(jsonPath("$.message").value(ErrorCode.GAME_SESSION_NOT_FOUND.getMessage()))
            .andDo(document("game-news/history/not-found",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                basicErrorResponseFields()
            ));
    }
}
