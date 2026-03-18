package io.ssafy.p.j14c103.homerun.api.controller.world;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.world.GameWorldService;
import io.ssafy.p.j14c103.homerun.api.service.world.response.GameTurnResponse;
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
        given(gameWorldService.getTurn(anyInt())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/games/sessions/1001/turn"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data.turnNumber").value(12))
            .andExpect(jsonPath("$.data.currentDate").value("2026-01-01"))
            .andExpect(jsonPath("$.data.economicCycle.phase").value("BOOM"))
            .andExpect(jsonPath("$.data.economicCycle.description").value("경기 호황기"))
            .andExpect(jsonPath("$.data.news").isArray())
            .andExpect(jsonPath("$.data.news.length()").value(0));
    }

    @DisplayName("존재하지 않는 세션 ID면 400과 에러 응답을 반환한다")
    @Test
    void getTurnWithUnknownSessionId() throws Exception {
        // given
        given(gameWorldService.getTurn(anyInt()))
            .willThrow(HomerunException.from(ErrorCode.WORLD_SESSION_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/games/sessions/9999/turn"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorCode.WORLD_SESSION_NOT_FOUND.getCode()))
            .andExpect(jsonPath("$.message").value(ErrorCode.WORLD_SESSION_NOT_FOUND.getMessage()));
    }
}
