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

import io.ssafy.p.j14c103.homerun.api.service.game.turn.GetTurnStateService;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.response.TurnStateResponse;
import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
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

@WebMvcTest(GameTurnController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.homerun.local", uriPort = 443)
class GameTurnControllerTest extends RestDocsTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetTurnStateService getTurnStateService;

    @DisplayName("현재 턴 상태 조회는 게임 코어 응답 계약으로 현재 턴 정보를 반환한다.")
    @Test
    void getTurnState() throws Exception {
        // given
        final TurnStateResponse response = TurnStateResponse.of(
            12,
            LocalDate.of(2026, 1, 1),
            TurnStateResponse.EconomicCycleResponse.of(CyclePhase.BOOM, "경기 호황기"),
            List.of()
        );
        given(getTurnStateService.getTurnState(1L, 1001L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/games/sessions/{sessionId}/turn", 1001L)
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data.turnNumber").value(12))
            .andExpect(jsonPath("$.data.currentDate").value("2026-01-01"))
            .andExpect(jsonPath("$.data.month").value(1))
            .andExpect(jsonPath("$.data.economicCycle.phase").value("BOOM"))
            .andExpect(jsonPath("$.data.economicCycle.description").value("경기 호황기"))
            .andExpect(jsonPath("$.data.news").isArray())
            .andExpect(jsonPath("$.data.news.length()").value(0))
            .andDo(document("game-turn/state/success",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                apiResponseFields(
                    "현재 턴 정보",
                    fieldWithPath("turnNumber").type(JsonFieldType.NUMBER).description("현재 턴 번호"),
                    fieldWithPath("currentDate").type(JsonFieldType.STRING).description("현재 날짜"),
                    fieldWithPath("month").type(JsonFieldType.NUMBER).description("현재 월"),
                    fieldWithPath("economicCycle").type(JsonFieldType.OBJECT).description("경제 사이클 정보"),
                    fieldWithPath("economicCycle.phase").type(JsonFieldType.STRING).description("경제 사이클 단계"),
                    fieldWithPath("economicCycle.description").type(JsonFieldType.STRING).description("경제 사이클 설명"),
                    fieldWithPath("news").type(JsonFieldType.ARRAY).description("턴 뉴스 목록")
                )
            ));
        then(getTurnStateService).should().getTurnState(1L, 1001L);
    }

    @DisplayName("다른 사용자의 세션 턴 상태 조회는 403을 반환한다.")
    @Test
    void getOtherUsersTurnState() throws Exception {
        // given
        given(getTurnStateService.getTurnState(1L, 88L))
            .willThrow(new HomerunException(ErrorCode.GAME_SESSION_FORBIDDEN));

        // when & then
        mockMvc.perform(get("/api/games/sessions/{sessionId}/turn", 88L)
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_SESSION_FORBIDDEN.getCode()))
            .andExpect(jsonPath("$.message").value(ErrorCode.GAME_SESSION_FORBIDDEN.getMessage()))
            .andDo(document("game-turn/state/forbidden",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                basicErrorResponseFields()
            ));
    }

    @DisplayName("존재하지 않는 세션의 턴 상태 조회는 404를 반환한다.")
    @Test
    void getUnknownTurnState() throws Exception {
        // given
        given(getTurnStateService.getTurnState(1L, 9999L))
            .willThrow(new HomerunException(ErrorCode.GAME_SESSION_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/games/sessions/{sessionId}/turn", 9999L)
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_SESSION_NOT_FOUND.getCode()))
            .andExpect(jsonPath("$.message").value(ErrorCode.GAME_SESSION_NOT_FOUND.getMessage()))
            .andDo(document("game-turn/state/not-found",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                basicErrorResponseFields()
            ));
    }
}
