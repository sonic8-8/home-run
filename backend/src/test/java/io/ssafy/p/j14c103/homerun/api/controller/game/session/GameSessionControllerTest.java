package io.ssafy.p.j14c103.homerun.api.controller.game.session;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.game.session.GameSessionService;
import io.ssafy.p.j14c103.homerun.api.service.game.session.request.CreateGameSessionServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.session.response.CreateGameSessionResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.session.response.GameSessionDetailResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.session.response.GameSessionListResponse;
import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
import io.ssafy.p.j14c103.homerun.domain.character.CharacterType;
import io.ssafy.p.j14c103.homerun.domain.character.career.JobType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.DataSourceType;
import io.ssafy.p.j14c103.homerun.domain.gamesession.SessionStatus;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
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
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GameSessionController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.homerun.local", uriPort = 443)
class GameSessionControllerTest extends RestDocsTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameSessionService gameSessionService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @DisplayName("세션 목록 조회는 3개 슬롯 응답을 ApiResponse로 반환한다.")
    @Test
    void getSessions() throws Exception {
        // given
        given(gameSessionService.getSessions(1L))
            .willReturn(createGameSessionListResponse());

        // when & then
        mockMvc.perform(get("/api/games/sessions")
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.sessions.length()").value(3))
            .andExpect(jsonPath("$.data.sessions[0].slotNumber").value(1))
            .andExpect(jsonPath("$.data.sessions[1].slotNumber").value(2))
            .andExpect(jsonPath("$.data.sessions[1].status").value("EMPTY"))
            .andExpect(jsonPath("$.data.sessions[2].slotNumber").value(3))
            .andDo(document("game-session/list/success",
                requestHeaders(authorizationHeader()),
                relaxedApiResponseFields(
                    "게임 세션 저장 슬롯 목록",
                    fieldWithPath("sessions").type(JsonFieldType.ARRAY).description("저장 슬롯 목록"),
                    fieldWithPath("sessions[].sessionId").type(JsonFieldType.VARIES).optional().description("게임 세션 ID"),
                    fieldWithPath("sessions[].slotNumber").type(JsonFieldType.NUMBER).description("슬롯 번호"),
                    fieldWithPath("sessions[].status").type(JsonFieldType.STRING).description("세션 상태"),
                    fieldWithPath("sessions[].characterName").type(JsonFieldType.VARIES).optional().description("캐릭터 이름"),
                    fieldWithPath("sessions[].characterType").type(JsonFieldType.VARIES).optional().description("캐릭터 타입"),
                    fieldWithPath("sessions[].jobType").type(JsonFieldType.VARIES).optional().description("직업 타입"),
                    fieldWithPath("sessions[].jobLabel").type(JsonFieldType.VARIES).optional().description("직업 표시 이름"),
                    fieldWithPath("sessions[].currentTurn").type(JsonFieldType.VARIES).optional().description("현재 턴"),
                    fieldWithPath("sessions[].totalAssets").type(JsonFieldType.VARIES).optional().description("총자산"),
                    fieldWithPath("sessions[].createdAt").type(JsonFieldType.VARIES).optional().description("세션 생성 시각")
                )
            ));
    }

    @DisplayName("세션 생성은 201과 생성 응답을 반환한다.")
    @Test
    void createSession() throws Exception {
        // given
        given(gameSessionService.create(any(Long.class), any(CreateGameSessionServiceRequest.class)))
            .willReturn(CreateGameSessionResponse.of(10L, 1, SessionStatus.IN_PROGRESS, 0, DataSourceType.MY_DATA));

        // when & then
        mockMvc.perform(post("/api/games/sessions")
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "slotNumber": 1,
                      "characterType": "FEMALE",
                      "characterName": "승환",
                      "regionCode": "11",
                      "districtCode": "11680",
                      "targetPropertyId": 1201,
                      "useMyData": true
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value(201))
            .andExpect(jsonPath("$.data.sessionId").value(10L))
            .andExpect(jsonPath("$.data.slotNumber").value(1))
            .andExpect(jsonPath("$.data.sessionStatus").value("IN_PROGRESS"))
            .andExpect(jsonPath("$.data.dataSourceType").value("MY_DATA"))
            .andDo(document("game-session/create/success",
                requestHeaders(authorizationHeader()),
                requestFields(
                    fieldWithPath("slotNumber").type(JsonFieldType.NUMBER).description("저장 슬롯 번호"),
                    fieldWithPath("characterType").type(JsonFieldType.STRING).description("캐릭터 타입"),
                    fieldWithPath("characterName").type(JsonFieldType.STRING).description("캐릭터 이름"),
                    fieldWithPath("jobType").type(JsonFieldType.STRING).optional().description("직업 타입. PROFILE 시작일 때만 필수"),
                    fieldWithPath("regionCode").type(JsonFieldType.STRING).description("목표 지역 코드"),
                    fieldWithPath("districtCode").type(JsonFieldType.STRING).description("목표 구 코드"),
                    fieldWithPath("targetPropertyId").type(JsonFieldType.NUMBER).description("목표 부동산 매물 ID"),
                    fieldWithPath("useMyData").type(JsonFieldType.BOOLEAN).description("내 자산 연동 사용 여부")
                ),
                apiResponseFields(
                    "생성된 게임 세션 요약",
                    fieldWithPath("sessionId").type(JsonFieldType.NUMBER).description("게임 세션 ID"),
                    fieldWithPath("slotNumber").type(JsonFieldType.NUMBER).description("저장 슬롯 번호"),
                    fieldWithPath("sessionStatus").type(JsonFieldType.STRING).description("세션 상태"),
                    fieldWithPath("currentTurn").type(JsonFieldType.NUMBER).description("현재 턴"),
                    fieldWithPath("dataSourceType").type(JsonFieldType.STRING).description("초기 데이터 소스 타입")
                )
            ));
    }

    @DisplayName("PROFILE 세션 생성 요청에서 jobType이 없으면 400과 필드 에러를 반환한다.")
    @Test
    void createSessionWithoutJobTypeForProfile() throws Exception {
        // when & then
        mockMvc.perform(post("/api/games/sessions")
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "slotNumber": 1,
                      "characterType": "FEMALE",
                      "characterName": "승환",
                      "regionCode": "11",
                      "districtCode": "11680",
                      "targetPropertyId": 1201,
                      "useMyData": false
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()))
            .andExpect(jsonPath("$.errors[*].field", hasItem("jobType")))
            .andDo(document("game-session/create/profile-jobtype-required",
                requestHeaders(authorizationHeader()),
                requestFields(
                    fieldWithPath("slotNumber").type(JsonFieldType.NUMBER).description("저장 슬롯 번호"),
                    fieldWithPath("characterType").type(JsonFieldType.STRING).description("캐릭터 타입"),
                    fieldWithPath("characterName").type(JsonFieldType.STRING).description("캐릭터 이름"),
                    fieldWithPath("jobType").type(JsonFieldType.STRING).optional().description("직업 타입. PROFILE 시작일 때만 필수"),
                    fieldWithPath("regionCode").type(JsonFieldType.STRING).description("목표 지역 코드"),
                    fieldWithPath("districtCode").type(JsonFieldType.STRING).description("목표 구 코드"),
                    fieldWithPath("targetPropertyId").type(JsonFieldType.NUMBER).description("목표 부동산 매물 ID"),
                    fieldWithPath("useMyData").type(JsonFieldType.BOOLEAN).description("내 자산 연동 사용 여부")
                ),
                validationErrorResponseFields()
            ));
    }

    @DisplayName("세션 생성 요청 검증 실패는 400과 필드 에러를 반환한다.")
    @Test
    void createSessionInvalidRequest() throws Exception {
        // when & then
        mockMvc.perform(post("/api/games/sessions")
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "slotNumber": 4,
                      "characterType": "FEMALE",
                      "characterName": "",
                      "jobType": "SMALL_BIZ",
                      "regionCode": "11",
                      "districtCode": "11680",
                      "targetPropertyId": 0,
                      "useMyData": true
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()))
            .andExpect(jsonPath("$.errors[*].field", hasItem("slotNumber")))
            .andExpect(jsonPath("$.errors[*].field", hasItem("characterName")))
            .andExpect(jsonPath("$.errors[*].field", hasItem("targetPropertyId")))
            .andDo(document("game-session/create/validation-error",
                requestHeaders(authorizationHeader()),
                requestFields(
                    fieldWithPath("slotNumber").type(JsonFieldType.NUMBER).description("저장 슬롯 번호"),
                    fieldWithPath("characterType").type(JsonFieldType.STRING).description("캐릭터 타입"),
                    fieldWithPath("characterName").type(JsonFieldType.STRING).description("캐릭터 이름"),
                    fieldWithPath("jobType").type(JsonFieldType.STRING).description("직업 타입"),
                    fieldWithPath("regionCode").type(JsonFieldType.STRING).description("목표 지역 코드"),
                    fieldWithPath("districtCode").type(JsonFieldType.STRING).description("목표 구 코드"),
                    fieldWithPath("targetPropertyId").type(JsonFieldType.NUMBER).description("목표 부동산 매물 ID"),
                    fieldWithPath("useMyData").type(JsonFieldType.BOOLEAN).description("내 자산 연동 사용 여부")
                ),
                validationErrorResponseFields()
            ));
    }

    @DisplayName("이미 사용 중인 슬롯으로 세션 생성 시 409를 반환한다.")
    @Test
    void createSessionConflict() throws Exception {
        // given
        given(gameSessionService.create(any(Long.class), any(CreateGameSessionServiceRequest.class)))
            .willThrow(new HomerunException(ErrorCode.GAME_SLOT_CONFLICT));

        // when & then
        mockMvc.perform(post("/api/games/sessions")
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "slotNumber": 1,
                      "characterType": "FEMALE",
                      "characterName": "승환",
                      "jobType": "SMALL_BIZ",
                      "regionCode": "11",
                      "districtCode": "11680",
                      "targetPropertyId": 1201,
                      "useMyData": true
                    }
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_SLOT_CONFLICT.getCode()))
            .andDo(document("game-session/create/slot-conflict",
                requestHeaders(authorizationHeader()),
                requestFields(
                    fieldWithPath("slotNumber").type(JsonFieldType.NUMBER).description("저장 슬롯 번호"),
                    fieldWithPath("characterType").type(JsonFieldType.STRING).description("캐릭터 타입"),
                    fieldWithPath("characterName").type(JsonFieldType.STRING).description("캐릭터 이름"),
                    fieldWithPath("jobType").type(JsonFieldType.STRING).description("직업 타입"),
                    fieldWithPath("regionCode").type(JsonFieldType.STRING).description("목표 지역 코드"),
                    fieldWithPath("districtCode").type(JsonFieldType.STRING).description("목표 구 코드"),
                    fieldWithPath("targetPropertyId").type(JsonFieldType.NUMBER).description("목표 부동산 매물 ID"),
                    fieldWithPath("useMyData").type(JsonFieldType.BOOLEAN).description("내 자산 연동 사용 여부")
                ),
                basicErrorResponseFields()
            ));
    }

    @DisplayName("세션 상세 조회는 세션 스냅샷을 반환한다.")
    @Test
    void getSessionDetail() throws Exception {
        // given
        given(gameSessionService.getSessionDetail(1L, 22L))
            .willReturn(GameSessionDetailResponse.of(
                22L,
                2,
                "세준",
                CharacterType.MALE,
                JobType.SMALL_BIZ,
                HousingType.STUDIO,
                "11",
                "11680",
                404L,
                DataSourceType.PROFILE,
                0,
                LocalDate.of(2026, 1, 1),
                CyclePhase.RECOVERY,
                13_000_000L,
                14_500_000L,
                SessionStatus.IN_PROGRESS,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 1, 1, 0, 0)
            ));

        // when & then
        mockMvc.perform(get("/api/games/sessions/{sessionId}", 22L)
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.sessionId").value(22L))
            .andExpect(jsonPath("$.data.slotNumber").value(2))
            .andExpect(jsonPath("$.data.characterName").value("세준"))
            .andExpect(jsonPath("$.data.cashBalance").value(13000000L))
            .andExpect(jsonPath("$.data.netWorth").value(14500000L))
            .andDo(document("game-session/detail/success",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                relaxedApiResponseFields(
                    "게임 세션 상세 정보",
                    fieldWithPath("sessionId").type(JsonFieldType.NUMBER).description("게임 세션 ID"),
                    fieldWithPath("slotNumber").type(JsonFieldType.NUMBER).description("저장 슬롯 번호"),
                    fieldWithPath("characterName").type(JsonFieldType.STRING).description("캐릭터 이름"),
                    fieldWithPath("characterType").type(JsonFieldType.STRING).description("캐릭터 타입"),
                    fieldWithPath("jobType").type(JsonFieldType.STRING).description("직업 타입"),
                    fieldWithPath("housingType").type(JsonFieldType.STRING).description("주거 타입"),
                    fieldWithPath("regionCode").type(JsonFieldType.STRING).description("목표 지역 코드"),
                    fieldWithPath("districtCode").type(JsonFieldType.STRING).description("목표 구 코드"),
                    fieldWithPath("targetPropertyId").type(JsonFieldType.NUMBER).description("목표 부동산 매물 ID"),
                    fieldWithPath("dataSourceType").type(JsonFieldType.STRING).description("초기 데이터 소스 타입"),
                    fieldWithPath("currentTurn").type(JsonFieldType.NUMBER).description("현재 턴"),
                    fieldWithPath("currentDate").type(JsonFieldType.STRING).description("현재 게임 날짜"),
                    fieldWithPath("cyclePhase").type(JsonFieldType.STRING).description("경제 사이클 상태"),
                    fieldWithPath("cashBalance").type(JsonFieldType.NUMBER).description("현금 잔액"),
                    fieldWithPath("netWorth").type(JsonFieldType.NUMBER).description("순자산"),
                    fieldWithPath("sessionStatus").type(JsonFieldType.STRING).description("세션 상태"),
                    fieldWithPath("createdAt").type(JsonFieldType.STRING).description("세션 생성 시각"),
                    fieldWithPath("lastPlayedAt").type(JsonFieldType.STRING).description("최근 플레이 시각")
                )
            ));
    }

    @DisplayName("존재하지 않는 세션 상세 조회는 404를 반환한다.")
    @Test
    void getSessionDetailNotFound() throws Exception {
        // given
        given(gameSessionService.getSessionDetail(1L, 99L))
            .willThrow(new HomerunException(ErrorCode.GAME_SESSION_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/games/sessions/{sessionId}", 99L)
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_SESSION_NOT_FOUND.getCode()))
            .andDo(document("game-session/detail/not-found",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                basicErrorResponseFields()
            ));
    }

    @DisplayName("다른 사용자의 세션 상세 조회는 403을 반환한다.")
    @Test
    void getSessionDetailForbidden() throws Exception {
        // given
        given(gameSessionService.getSessionDetail(1L, 88L))
            .willThrow(new HomerunException(ErrorCode.GAME_SESSION_FORBIDDEN));

        // when & then
        mockMvc.perform(get("/api/games/sessions/{sessionId}", 88L)
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_SESSION_FORBIDDEN.getCode()))
            .andDo(document("game-session/detail/forbidden",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                basicErrorResponseFields()
            ));
    }

    @DisplayName("세션 삭제는 200을 반환하고 삭제 서비스를 호출한다.")
    @Test
    void deleteSession() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/games/sessions/{sessionId}", 10L)
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andDo(document("game-session/delete/success",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                responseFields(
                    fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                    fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
                    fieldWithPath("data").type(JsonFieldType.NULL).description("삭제 결과 데이터 없음")
                )
            ));

        then(gameSessionService).should().delete(1L, 10L);
    }

    @DisplayName("존재하지 않는 세션 삭제는 404를 반환한다.")
    @Test
    void deleteSessionNotFound() throws Exception {
        // given
        willThrow(new HomerunException(ErrorCode.GAME_SESSION_NOT_FOUND))
            .given(gameSessionService)
            .delete(1L, 77L);

        // when & then
        mockMvc.perform(delete("/api/games/sessions/{sessionId}", 77L)
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_SESSION_NOT_FOUND.getCode()))
            .andDo(document("game-session/delete/not-found",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                basicErrorResponseFields()
            ));
    }

    @DisplayName("다른 사용자의 세션 삭제는 403을 반환한다.")
    @Test
    void deleteSessionForbidden() throws Exception {
        // given
        willThrow(new HomerunException(ErrorCode.GAME_SESSION_FORBIDDEN))
            .given(gameSessionService)
            .delete(1L, 66L);

        // when & then
        mockMvc.perform(delete("/api/games/sessions/{sessionId}", 66L)
                .with(currentUser())
                .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_SESSION_FORBIDDEN.getCode()))
            .andDo(document("game-session/delete/forbidden",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                basicErrorResponseFields()
            ));
    }

    private GameSessionListResponse createGameSessionListResponse() {
        return GameSessionListResponse.of(List.of(
            GameSessionListResponse.SessionSummaryResponse.of(
                11L,
                1,
                "IN_PROGRESS",
                "윤서",
                CharacterType.FEMALE,
                JobType.STARTUP,
                0,
                13_000_000L,
                LocalDateTime.of(2026, 1, 1, 0, 0)
            ),
            GameSessionListResponse.SessionSummaryResponse.empty(2),
            GameSessionListResponse.SessionSummaryResponse.of(
                33L,
                3,
                "IN_PROGRESS",
                "민지",
                CharacterType.FEMALE,
                JobType.MID_BIZ,
                0,
                15_000_000L,
                LocalDateTime.of(2026, 1, 3, 0, 0)
            )
        ));
    }
}
