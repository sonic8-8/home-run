package io.ssafy.p.j14c103.homerun.api.controller.game.events;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
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
import io.ssafy.p.j14c103.homerun.api.service.game.events.GameEventService;
import io.ssafy.p.j14c103.homerun.api.service.game.events.response.PendingEventsResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.events.response.ResolveEventResponse;
import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventPresentationType;
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
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GameEventController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.homerun.local", uriPort = 443)
class GameEventControllerTest extends RestDocsTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GameEventService gameEventService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @DisplayName("pending 이벤트 조회는 events 배열 계약으로 응답한다")
    @Test
    void getPendingEvents() throws Exception {
        given(gameEventService.getPendingEvents(1L, 10L))
            .willReturn(createPendingEventsResponse());

        mockMvc.perform(get("/api/games/sessions/{sessionId}/events/pending", 10L)
                .with(currentUser())
                .header(AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.events.length()").value(1))
            .andExpect(jsonPath("$.data.events[0].eventId").value(301))
            .andExpect(jsonPath("$.data.events[0].type").value("JOB_TRANSFER"))
            .andExpect(jsonPath("$.data.events[0].choices[0].choiceId").value(701))
            .andExpect(jsonPath("$.data.events[0].sender").value("OO 기업 인사팀"))
            .andExpect(jsonPath("$.data.events[0].date").value("2026-01-01"))
            .andDo(document("game-events/pending/success",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                apiResponseFields(
                    "확인 대기 중인 이벤트 목록",
                    fieldWithPath("events").type(JsonFieldType.ARRAY).description("pending 이벤트 목록"),
                    fieldWithPath("events[].eventId").type(JsonFieldType.NUMBER).description("pending 이벤트 ID"),
                    fieldWithPath("events[].type").type(JsonFieldType.STRING).description("이벤트 표현 타입"),
                    fieldWithPath("events[].title").type(JsonFieldType.STRING).description("이벤트 제목"),
                    fieldWithPath("events[].description").type(JsonFieldType.STRING).description("이벤트 설명"),
                    fieldWithPath("events[].imageUrl").type(JsonFieldType.STRING).optional().description("이벤트 이미지 URL"),
                    fieldWithPath("events[].choices").type(JsonFieldType.ARRAY).optional().description("선택지 목록"),
                    fieldWithPath("events[].choices[].choiceId").type(JsonFieldType.NUMBER).description("선택지 ID"),
                    fieldWithPath("events[].choices[].choiceCode").type(JsonFieldType.STRING).description("선택지 코드"),
                    fieldWithPath("events[].choices[].choiceName").type(JsonFieldType.STRING).description("선택지 이름"),
                    fieldWithPath("events[].choices[].description").type(JsonFieldType.STRING).optional().description("선택지 설명"),
                    fieldWithPath("events[].sender").type(JsonFieldType.STRING).optional().description("발신자"),
                    fieldWithPath("events[].receiver").type(JsonFieldType.STRING).optional().description("수신자"),
                    fieldWithPath("events[].date").type(JsonFieldType.STRING).optional().description("이벤트 날짜"),
                    fieldWithPath("events[].offeredSalary").type(JsonFieldType.NUMBER).optional().description("제안 연봉"),
                    fieldWithPath("events[].currentSalary").type(JsonFieldType.NUMBER).optional().description("현재 연봉")
                )
            ));
    }

    @DisplayName("타인 세션의 pending 이벤트 조회는 403을 반환한다")
    @Test
    void getPendingEventsForbidden() throws Exception {
        given(gameEventService.getPendingEvents(1L, 99L))
            .willThrow(new HomerunException(ErrorCode.GAME_SESSION_FORBIDDEN));

        mockMvc.perform(get("/api/games/sessions/{sessionId}/events/pending", 99L)
                .with(currentUser())
                .header(AUTHORIZATION, "Bearer access-token"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(ErrorCode.GAME_SESSION_FORBIDDEN.getCode()))
            .andDo(document("game-events/pending/forbidden",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID")
                ),
                basicErrorResponseFields()
            ));
    }

    @DisplayName("이벤트 resolve는 결과 요약과 효과 목록을 반환한다")
    @Test
    void resolveEvent() throws Exception {
        given(gameEventService.resolveEvent(any(Long.class), any(Long.class), any(Integer.class), any()))
            .willReturn(createResolveEventResponse());

        mockMvc.perform(post("/api/games/sessions/{sessionId}/events/{eventId}/resolve", 10L, 301L)
                .with(currentUser())
                .header(AUTHORIZATION, "Bearer access-token")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(java.util.Map.of("choiceId", 701))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.eventId").value(301))
            .andExpect(jsonPath("$.data.choiceId").value(701))
            .andExpect(jsonPath("$.data.selectedChoiceCode").value("A"))
            .andExpect(jsonPath("$.data.resultEffects.length()").value(1))
            .andExpect(jsonPath("$.data.resultSummary").value("이직 제안을 수락했다."))
            .andDo(document("game-events/resolve/success",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID"),
                    parameterWithName("eventId").description("pending 이벤트 ID")
                ),
                requestFields(
                    fieldWithPath("choiceId").type(JsonFieldType.NUMBER).optional().description("선택한 choice ID")
                ),
                apiResponseFields(
                    "resolve 처리 결과",
                    fieldWithPath("eventId").type(JsonFieldType.NUMBER).description("resolve 된 pending 이벤트 ID"),
                    fieldWithPath("gameEventId").type(JsonFieldType.NUMBER).description("원본 이벤트 마스터 ID"),
                    fieldWithPath("choiceId").type(JsonFieldType.NUMBER).optional().description("적용된 선택지 ID"),
                    fieldWithPath("selectedChoiceCode").type(JsonFieldType.STRING).optional().description("적용된 선택지 코드"),
                    fieldWithPath("resultEffects").type(JsonFieldType.ARRAY).description("적용된 효과 목록"),
                    fieldWithPath("resultEffects[].effectOrder").type(JsonFieldType.NUMBER).description("효과 적용 순서"),
                    fieldWithPath("resultEffects[].applicationTimingType").type(JsonFieldType.STRING).description("효과 적용 시점"),
                    fieldWithPath("resultEffects[].targetTableName").type(JsonFieldType.STRING).description("대상 테이블명"),
                    fieldWithPath("resultEffects[].targetColumnName").type(JsonFieldType.STRING).description("대상 컬럼명"),
                    fieldWithPath("resultEffects[].operationType").type(JsonFieldType.STRING).description("연산 타입"),
                    fieldWithPath("resultEffects[].baseNumberValue").type(JsonFieldType.NUMBER).optional().description("기본 수치 값"),
                    fieldWithPath("resultEffects[].minNumberValue").type(JsonFieldType.NUMBER).optional().description("최소 수치 값"),
                    fieldWithPath("resultEffects[].maxNumberValue").type(JsonFieldType.NUMBER).optional().description("최대 수치 값"),
                    fieldWithPath("resultEffects[].baseTextValue").type(JsonFieldType.STRING).optional().description("기본 텍스트 값"),
                    fieldWithPath("resultEffects[].durationTurns").type(JsonFieldType.NUMBER).optional().description("지속 턴 수"),
                    fieldWithPath("resultEffects[].note").type(JsonFieldType.STRING).optional().description("효과 메모"),
                    fieldWithPath("resultSummary").type(JsonFieldType.STRING).description("resolve 결과 요약")
                )
            ));
    }

    @DisplayName("이벤트 resolve 입력이 잘못되면 400을 반환한다")
    @Test
    void resolveEventInvalidInput() throws Exception {
        given(gameEventService.resolveEvent(any(Long.class), any(Long.class), any(Integer.class), any()))
            .willThrow(new HomerunException(ErrorCode.INVALID_INPUT_VALUE));

        mockMvc.perform(post("/api/games/sessions/{sessionId}/events/{eventId}/resolve", 10L, 301L)
                .with(currentUser())
                .header(AUTHORIZATION, "Bearer access-token")
                .contentType(APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()))
            .andDo(document("game-events/resolve/invalid-input",
                requestHeaders(authorizationHeader()),
                pathParameters(
                    parameterWithName("sessionId").description("게임 세션 ID"),
                    parameterWithName("eventId").description("pending 이벤트 ID")
                ),
                requestFields(
                    fieldWithPath("choiceId").type(JsonFieldType.NUMBER).optional().description("선택한 choice ID")
                ),
                basicErrorResponseFields()
            ));
    }

    private PendingEventsResponse createPendingEventsResponse() {
        return PendingEventsResponse.of(List.of(
            PendingEventsResponse.PendingEventResponse.of(
                301,
                EventPresentationType.JOB_TRANSFER,
                "이직 제안",
                "더 좋은 조건으로 이직 제안이 도착했습니다.",
                "/images/events/job-transfer.png",
                List.of(
                    PendingEventsResponse.PendingEventChoiceResponse.of(701, "A", "수락", "연봉을 올리고 이직합니다.")
                ),
                "OO 기업 인사팀",
                "김싸피 님",
                LocalDate.of(2026, 1, 1),
                36_000_000,
                31_000_000
            )
        ));
    }

    private ResolveEventResponse createResolveEventResponse() {
        return ResolveEventResponse.of(
            301,
            1201,
            701,
            "A",
            List.of(
                ResolveEventResponse.ResolvedEffectResponse.of(
                    1,
                    "IMMEDIATE",
                    "game_career",
                    "salary",
                    "ADD",
                    5_000_000,
                    null,
                    null,
                    null,
                    null,
                    "연봉이 상승했습니다."
                )
            ),
            "이직 제안을 수락했다."
        );
    }
}
