package io.ssafy.p.j14c103.homerun.api.controller.game.turn;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ssafy.p.j14c103.homerun.api.controller.game.turn.request.SubmitTurnSlotsRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.CommitTurnService;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.GameTurnActionService;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.GameTurnStateService;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.SubmitTurnSlotsService;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.response.AvailableActionsResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.response.CommitTurnResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.response.TurnPreviewResponse;
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

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GameTurnStateService gameTurnStateService;

    @MockitoBean
    private GameTurnActionService gameTurnActionService;

    @MockitoBean
    private SubmitTurnSlotsService submitTurnSlotsService;

    @MockitoBean
    private CommitTurnService commitTurnService;

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
        given(gameTurnStateService.getTurnState(1L, 1001L)).willReturn(response);

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
        then(gameTurnStateService).should().getTurnState(1L, 1001L);
    }

    @DisplayName("다른 사용자의 세션 턴 상태 조회는 403을 반환한다.")
    @Test
    void getOtherUsersTurnState() throws Exception {
        // given
        given(gameTurnStateService.getTurnState(1L, 88L))
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
        given(gameTurnStateService.getTurnState(1L, 9999L))
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

    @DisplayName("선택 가능 행동 조회는 게임 코어 응답 계약으로 행동 목록을 반환한다.")
    @Test
    void getAvailableActions() throws Exception {
        // given
        final AvailableActionsResponse response = AvailableActionsResponse.of(
            List.of(
                AvailableActionsResponse.ActionResponse.of(
                    "HOBBY",
                    "취미",
                    "/images/actions/hobby.png",
                    AvailableActionsResponse.ActionEffectResponse.of(-100_000, 0, -2, -6, 6, 0)
                )
            ),
            List.of(
                AvailableActionsResponse.ActionResponse.of(
                    "STUDY",
                    "공부",
                    "/images/actions/study.png",
                    AvailableActionsResponse.ActionEffectResponse.of(0, 0, 6, 3, 0, 8)
                ),
                AvailableActionsResponse.ActionResponse.of(
                    "REST",
                    "휴식",
                    "/images/actions/rest.png",
                    AvailableActionsResponse.ActionEffectResponse.of(0, 3, -12, -8, 2, 0)
                )
            )
        );
        given(gameTurnActionService.getAvailableActions(1L, 1001L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/games/sessions/{sessionId}/turn/actions", 1001L)
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data.shopping").isArray())
            .andExpect(jsonPath("$.data.shopping.length()").value(1))
            .andExpect(jsonPath("$.data.shopping[0].actionType").value("HOBBY"))
            .andExpect(jsonPath("$.data.shopping[0].label").value("취미"))
            .andExpect(jsonPath("$.data.shopping[0].iconUrl").value("/images/actions/hobby.png"))
            .andExpect(jsonPath("$.data.shopping[0].effects.cash").value(-100000))
            .andExpect(jsonPath("$.data.activities").isArray())
            .andExpect(jsonPath("$.data.activities.length()").value(2))
            .andExpect(jsonPath("$.data.activities[0].actionType").value("STUDY"))
            .andExpect(jsonPath("$.data.activities[0].effects.knowledge").value(8))
            .andExpect(jsonPath("$.data.activities[1].actionType").value("REST"))
            .andExpect(jsonPath("$.data.activities[1].effects.fatigue").value(-12))
            .andDo(document("game-turn/actions/success",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                apiResponseFields(
                    "선택 가능 행동 목록",
                    fieldWithPath("shopping").type(JsonFieldType.ARRAY).description("쇼핑 카테고리 행동 목록"),
                    fieldWithPath("shopping[].actionType").type(JsonFieldType.STRING).description("행동 타입"),
                    fieldWithPath("shopping[].label").type(JsonFieldType.STRING).description("행동 라벨"),
                    fieldWithPath("shopping[].iconUrl").type(JsonFieldType.STRING).description("행동 아이콘 URL"),
                    fieldWithPath("shopping[].effects").type(JsonFieldType.OBJECT).description("행동 효과"),
                    fieldWithPath("shopping[].effects.cash").type(JsonFieldType.NUMBER).description("현금 변화량"),
                    fieldWithPath("shopping[].effects.health").type(JsonFieldType.NUMBER).description("체력 변화량"),
                    fieldWithPath("shopping[].effects.fatigue").type(JsonFieldType.NUMBER).description("피로 변화량"),
                    fieldWithPath("shopping[].effects.stress").type(JsonFieldType.NUMBER).description("스트레스 변화량"),
                    fieldWithPath("shopping[].effects.happiness").type(JsonFieldType.NUMBER).description("행복 변화량"),
                    fieldWithPath("shopping[].effects.knowledge").type(JsonFieldType.NUMBER).description("지식 변화량"),
                    fieldWithPath("activities").type(JsonFieldType.ARRAY).description("활동 카테고리 행동 목록"),
                    fieldWithPath("activities[].actionType").type(JsonFieldType.STRING).description("행동 타입"),
                    fieldWithPath("activities[].label").type(JsonFieldType.STRING).description("행동 라벨"),
                    fieldWithPath("activities[].iconUrl").type(JsonFieldType.STRING).description("행동 아이콘 URL"),
                    fieldWithPath("activities[].effects").type(JsonFieldType.OBJECT).description("행동 효과"),
                    fieldWithPath("activities[].effects.cash").type(JsonFieldType.NUMBER).description("현금 변화량"),
                    fieldWithPath("activities[].effects.health").type(JsonFieldType.NUMBER).description("체력 변화량"),
                    fieldWithPath("activities[].effects.fatigue").type(JsonFieldType.NUMBER).description("피로 변화량"),
                    fieldWithPath("activities[].effects.stress").type(JsonFieldType.NUMBER).description("스트레스 변화량"),
                    fieldWithPath("activities[].effects.happiness").type(JsonFieldType.NUMBER).description("행복 변화량"),
                    fieldWithPath("activities[].effects.knowledge").type(JsonFieldType.NUMBER).description("지식 변화량")
                )
            ));
        then(gameTurnActionService).should().getAvailableActions(1L, 1001L);
    }

    @DisplayName("다른 사용자의 세션 행동 목록 조회는 403을 반환한다.")
    @Test
    void getOtherUsersAvailableActions() throws Exception {
        // given
        given(gameTurnActionService.getAvailableActions(1L, 88L))
            .willThrow(new HomerunException(ErrorCode.GAME_SESSION_FORBIDDEN));

        // when & then
        mockMvc.perform(get("/api/games/sessions/{sessionId}/turn/actions", 88L)
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_SESSION_FORBIDDEN.getCode()))
            .andExpect(jsonPath("$.message").value(ErrorCode.GAME_SESSION_FORBIDDEN.getMessage()))
            .andDo(document("game-turn/actions/forbidden",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                basicErrorResponseFields()
            ));
    }

    @DisplayName("종료된 세션의 행동 목록 조회는 409를 반환한다.")
    @Test
    void getClosedSessionAvailableActions() throws Exception {
        // given
        given(gameTurnActionService.getAvailableActions(1L, 77L))
            .willThrow(new HomerunException(ErrorCode.GAME_SESSION_CLOSED));

        // when & then
        mockMvc.perform(get("/api/games/sessions/{sessionId}/turn/actions", 77L)
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_SESSION_CLOSED.getCode()))
            .andExpect(jsonPath("$.message").value(ErrorCode.GAME_SESSION_CLOSED.getMessage()))
            .andDo(document("game-turn/actions/closed",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                basicErrorResponseFields()
            ));
    }

    @DisplayName("턴 슬롯 제출은 preview와 draft 저장 결과를 반환한다.")
    @Test
    void submitTurnSlots() throws Exception {
        // given
        final SubmitTurnSlotsRequest request = SubmitTurnSlotsRequest.of(
            List.of(
                SubmitTurnSlotsRequest.TurnSlotRequest.of(0, "STUDY"),
                SubmitTurnSlotsRequest.TurnSlotRequest.of(1, "REST"),
                SubmitTurnSlotsRequest.TurnSlotRequest.of(2, "SIDE_JOB")
            )
        );
        final TurnPreviewResponse response = TurnPreviewResponse.of(
            List.of(
                TurnPreviewResponse.PreviewSlotResponse.of(0, "STUDY", false),
                TurnPreviewResponse.PreviewSlotResponse.of(1, "REST", false),
                TurnPreviewResponse.PreviewSlotResponse.of(2, "SIDE_JOB", false)
            ),
            430_000L,
            TurnPreviewResponse.PreviewStatChangesResponse.of(3, -14, -8, 4, 8)
        );
        given(submitTurnSlotsService.submitTurnSlots(eq(1L), eq(1001L), any()))
            .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/games/sessions/{sessionId}/turn/slots", 1001L)
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data.slots.length()").value(3))
            .andExpect(jsonPath("$.data.slots[0].slotIndex").value(0))
            .andExpect(jsonPath("$.data.slots[0].actionType").value("STUDY"))
            .andExpect(jsonPath("$.data.previewCashChange").value(430000))
            .andExpect(jsonPath("$.data.previewStatChanges.health").value(3))
            .andExpect(jsonPath("$.data.previewStatChanges.knowledge").value(8))
            .andDo(document("game-turn/slots/success",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                requestFields(
                    fieldWithPath("slots").description("제출할 턴 슬롯 3개"),
                    fieldWithPath("slots[].slotIndex").description("슬롯 인덱스"),
                    fieldWithPath("slots[].actionType").description("선택한 행동 타입")
                ),
                apiResponseFields(
                    "턴 슬롯 preview",
                    fieldWithPath("slots").type(JsonFieldType.ARRAY).description("preview 기준 슬롯 목록"),
                    fieldWithPath("slots[].slotIndex").type(JsonFieldType.NUMBER).description("슬롯 인덱스"),
                    fieldWithPath("slots[].actionType").type(JsonFieldType.STRING).description("preview 행동 타입"),
                    fieldWithPath("slots[].forcedAction").type(JsonFieldType.BOOLEAN).description("강제 행동 여부"),
                    fieldWithPath("previewCashChange").type(JsonFieldType.NUMBER).description("예상 현금 변화량"),
                    fieldWithPath("previewStatChanges").type(JsonFieldType.OBJECT).description("예상 스탯 변화량"),
                    fieldWithPath("previewStatChanges.health").type(JsonFieldType.NUMBER).description("체력 변화량"),
                    fieldWithPath("previewStatChanges.fatigue").type(JsonFieldType.NUMBER).description("피로 변화량"),
                    fieldWithPath("previewStatChanges.stress").type(JsonFieldType.NUMBER).description("스트레스 변화량"),
                    fieldWithPath("previewStatChanges.happiness").type(JsonFieldType.NUMBER).description("행복 변화량"),
                    fieldWithPath("previewStatChanges.knowledge").type(JsonFieldType.NUMBER).description("지식 변화량")
                )
            ));
        then(submitTurnSlotsService).should()
            .submitTurnSlots(eq(1L), eq(1001L), any());
    }

    @DisplayName("턴 슬롯이 3개보다 적으면 400과 validation error를 반환한다.")
    @Test
    void submitTurnSlotsWithInvalidSlotCount() throws Exception {
        // given
        final SubmitTurnSlotsRequest request = SubmitTurnSlotsRequest.of(
            List.of(
                SubmitTurnSlotsRequest.TurnSlotRequest.of(0, "STUDY"),
                SubmitTurnSlotsRequest.TurnSlotRequest.of(1, "REST")
            )
        );

        // when & then
        mockMvc.perform(post("/api/games/sessions/{sessionId}/turn/slots", 1001L)
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()))
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors[0].field").value("slots"))
            .andDo(document("game-turn/slots/invalid-slot-count",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                requestFields(
                    fieldWithPath("slots").description("제출할 턴 슬롯 목록"),
                    fieldWithPath("slots[].slotIndex").description("슬롯 인덱스"),
                    fieldWithPath("slots[].actionType").description("선택한 행동 타입")
                ),
                validationErrorResponseFields()
            ));
    }

    @DisplayName("턴 커밋은 확정 결과와 자산 스냅샷, 스탯 변화량을 반환한다.")
    @Test
    void commitTurn() throws Exception {
        // given
        final CommitTurnResponse response = CommitTurnResponse.of(
            12,
            List.of(
                CommitTurnResponse.SettlementLogItemResponse.of(
                    "MARKET_UPDATE",
                    "경제 사이클을 다음 국면으로 갱신한다",
                    0L,
                    CommitTurnResponse.StatChangesResponse.of(0, 0, 0, 0, 0)
                ),
                CommitTurnResponse.SettlementLogItemResponse.of(
                    "ACTION_RESULT",
                    "턴 행동 결과를 반영한다",
                    430_000L,
                    CommitTurnResponse.StatChangesResponse.of(3, -14, -8, 4, 8)
                )
            ),
            CommitTurnResponse.UpdatedAssetsResponse.of(2_820_000L, 0L, 0L, 1_820_000L),
            CommitTurnResponse.StatChangesResponse.of(3, -14, -8, 4, 8),
            CommitTurnResponse.FlagsResponse.of(false, false, false, false, true)
        );
        given(commitTurnService.commitTurn(1L, 1001L)).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/games/sessions/{sessionId}/turn/commit", 1001L)
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.message").value("OK"))
            .andExpect(jsonPath("$.data.turnNumber").value(12))
            .andExpect(jsonPath("$.data.settlementLog.length()").value(2))
            .andExpect(jsonPath("$.data.settlementLog[0].phase").value("MARKET_UPDATE"))
            .andExpect(jsonPath("$.data.settlementLog[1].cashChange").value(430000))
            .andExpect(jsonPath("$.data.updatedAssets.cash").value(2820000))
            .andExpect(jsonPath("$.data.updatedAssets.netAssets").value(1820000))
            .andExpect(jsonPath("$.data.statChanges.health").value(3))
            .andExpect(jsonPath("$.data.statChanges.fatigue").value(-14))
            .andExpect(jsonPath("$.data.statChanges.knowledge").value(8))
            .andExpect(jsonPath("$.data.flags.hasEvent").value(true))
            .andDo(document("game-turn/commit/success",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                apiResponseFields(
                    "턴 커밋 결과",
                    fieldWithPath("turnNumber").type(JsonFieldType.NUMBER).description("커밋된 턴 번호"),
                    fieldWithPath("settlementLog").type(JsonFieldType.ARRAY).description("턴 커밋 skeleton 기준 정산 로그"),
                    fieldWithPath("settlementLog[].phase").type(JsonFieldType.STRING).description("정산 단계 식별자"),
                    fieldWithPath("settlementLog[].description").type(JsonFieldType.STRING).description("정산 단계 설명"),
                    fieldWithPath("settlementLog[].cashChange").type(JsonFieldType.NUMBER).description("해당 단계 현금 변화량"),
                    fieldWithPath("settlementLog[].statChanges").type(JsonFieldType.OBJECT).description("해당 단계 스탯 변화량"),
                    fieldWithPath("settlementLog[].statChanges.health").type(JsonFieldType.NUMBER).description("체력 변화량"),
                    fieldWithPath("settlementLog[].statChanges.fatigue").type(JsonFieldType.NUMBER).description("피로 변화량"),
                    fieldWithPath("settlementLog[].statChanges.stress").type(JsonFieldType.NUMBER).description("스트레스 변화량"),
                    fieldWithPath("settlementLog[].statChanges.happiness").type(JsonFieldType.NUMBER).description("행복 변화량"),
                    fieldWithPath("settlementLog[].statChanges.knowledge").type(JsonFieldType.NUMBER).description("지식 변화량"),
                    fieldWithPath("updatedAssets").type(JsonFieldType.OBJECT).description("커밋 직후 자산 스냅샷"),
                    fieldWithPath("updatedAssets.cash").type(JsonFieldType.NUMBER).description("현금"),
                    fieldWithPath("updatedAssets.loan").type(JsonFieldType.NUMBER).description("대출 잔액"),
                    fieldWithPath("updatedAssets.realEstateValue").type(JsonFieldType.NUMBER).description("부동산 자산 가치"),
                    fieldWithPath("updatedAssets.netAssets").type(JsonFieldType.NUMBER).description("순자산"),
                    fieldWithPath("statChanges").type(JsonFieldType.OBJECT).description("이번 턴 스탯 변화량"),
                    fieldWithPath("statChanges.health").type(JsonFieldType.NUMBER).description("체력 변화량"),
                    fieldWithPath("statChanges.fatigue").type(JsonFieldType.NUMBER).description("피로 변화량"),
                    fieldWithPath("statChanges.stress").type(JsonFieldType.NUMBER).description("스트레스 변화량"),
                    fieldWithPath("statChanges.happiness").type(JsonFieldType.NUMBER).description("행복 변화량"),
                    fieldWithPath("statChanges.knowledge").type(JsonFieldType.NUMBER).description("지식 변화량"),
                    fieldWithPath("flags").type(JsonFieldType.OBJECT).description("턴 커밋 결과 플래그"),
                    fieldWithPath("flags.isBankrupt").type(JsonFieldType.BOOLEAN).description("파산 여부"),
                    fieldWithPath("flags.isCleared").type(JsonFieldType.BOOLEAN).description("클리어 여부"),
                    fieldWithPath("flags.isBurnout").type(JsonFieldType.BOOLEAN).description("번아웃 여부"),
                    fieldWithPath("flags.isForcedResignation").type(JsonFieldType.BOOLEAN).description("강제 퇴사 여부"),
                    fieldWithPath("flags.hasEvent").type(JsonFieldType.BOOLEAN).description("추가 이벤트 존재 여부")
                )
            ));
        then(commitTurnService).should().commitTurn(1L, 1001L);
    }

    @DisplayName("이미 커밋된 턴을 다시 커밋하면 409를 반환한다.")
    @Test
    void commitAlreadyCommittedTurn() throws Exception {
        // given
        given(commitTurnService.commitTurn(1L, 1001L))
            .willThrow(new HomerunException(ErrorCode.GAME_TURN_ALREADY_COMMITTED));

        // when & then
        mockMvc.perform(post("/api/games/sessions/{sessionId}/turn/commit", 1001L)
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_TURN_ALREADY_COMMITTED.getCode()))
            .andExpect(jsonPath("$.message").value(ErrorCode.GAME_TURN_ALREADY_COMMITTED.getMessage()))
            .andDo(document("game-turn/commit/already-committed",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                basicErrorResponseFields()
            ));
    }
}
