package io.ssafy.p.j14c103.homerun.api.controller.home;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.api.service.home.DashboardSseService;
import io.ssafy.p.j14c103.homerun.docs.RestDocsTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@WebMvcTest(DashboardSseController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.homerun.local", uriPort = 443)
class DashboardSseControllerTest extends RestDocsTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardSseService dashboardSseService;

    @DisplayName("대시보드 SSE 구독은 text/event-stream 응답을 반환한다.")
    @Test
    void subscribe() throws Exception {
        given(dashboardSseService.subscribe(1L)).willAnswer(invocation -> {
            SseEmitter emitter = new SseEmitter();
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("SSE 연결 완료"));
            emitter.complete();
            return emitter;
        });

        MvcResult result = mockMvc.perform(get("/api/home/dashboard/subscribe")
                        .with(currentUser())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andDo(document("home/dashboard/subscribe/success",
                        requestHeaders(authorizationHeader()),
                        responseHeaders(
                                headerWithName(HttpHeaders.CONTENT_TYPE).description("SSE 응답 Content-Type")
                        )
                ));
    }
}
