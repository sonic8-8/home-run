package io.ssafy.p.j14c103.homerun.api.controller.world;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.world.GameWorldService;
import io.ssafy.p.j14c103.homerun.api.service.world.LatestTurnNewsService;
import io.ssafy.p.j14c103.homerun.api.service.world.response.GameTurnResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.response.LatestTurnNewsResponse;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GameWorldController.class)
@AutoConfigureMockMvc(addFilters = false)
class GameWorldControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameWorldService gameWorldService;

    @MockitoBean
    private LatestTurnNewsService latestTurnNewsService;

    @DisplayName("턴 조회 요청이 성공하면 현재 턴 상태와 경제 사이클 응답을 반환한다")
    @Test
    void getTurn() throws Exception {
        // given
        GameTurnResponse response = GameTurnResponse.of(
            12,
            LocalDate.of(2026, 1, 1),
            GameTurnResponse.EconomicCycleResponse.of(
                CyclePhase.BOOM,
                "경기 호황기"
            ),
            List.of()
        );
        given(gameWorldService.getTurn(anyLong())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/games/sessions/1001/turn"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data.turnNumber").value(12))
            .andExpect(jsonPath("$.data.currentDate").value("2026-01-01"))
            .andExpect(jsonPath("$.data.month").value(1))
            .andExpect(jsonPath("$.data.economicCycle.phase").value("BOOM"))
            .andExpect(jsonPath("$.data.economicCycle.description").value("경기 호황기"))
            .andExpect(jsonPath("$.data.news").isArray())
            .andExpect(jsonPath("$.data.news.length()").value(0));
    }

    @DisplayName("존재하지 않는 세션 ID면 400과 에러 응답을 반환한다")
    @Test
    void getTurnWithUnknownSessionId() throws Exception {
        // given
        given(gameWorldService.getTurn(anyLong()))
            .willThrow(new HomerunException(ErrorCode.WORLD_SESSION_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/games/sessions/9999/turn"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorCode.WORLD_SESSION_NOT_FOUND.getCode()))
            .andExpect(jsonPath("$.message").value(ErrorCode.WORLD_SESSION_NOT_FOUND.getMessage()));
    }

    @DisplayName("최신 턴 뉴스 조회 요청이 성공하면 현재 턴 뉴스 응답을 반환한다")
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
        given(latestTurnNewsService.getLatestTurnNews(anyLong())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/games/sessions/1001/news/latest"))
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
            .andExpect(jsonPath("$.data.news[0].economicCycleType").value("BOOM_TO_CRISIS"));
    }

    @DisplayName("최신 턴 뉴스 조회 중 존재하지 않는 세션 ID면 400과 에러 응답을 반환한다")
    @Test
    void getLatestTurnNewsWithUnknownSessionId() throws Exception {
        // given
        given(latestTurnNewsService.getLatestTurnNews(anyLong()))
            .willThrow(new HomerunException(ErrorCode.WORLD_SESSION_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/games/sessions/9999/news/latest"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorCode.WORLD_SESSION_NOT_FOUND.getCode()))
            .andExpect(jsonPath("$.message").value(ErrorCode.WORLD_SESSION_NOT_FOUND.getMessage()));
    }
}
