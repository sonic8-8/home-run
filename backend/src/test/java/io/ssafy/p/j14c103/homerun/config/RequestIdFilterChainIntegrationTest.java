package io.ssafy.p.j14c103.homerun.config;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.ssafy.p.j14c103.homerun.support.MockMvcIntegrationTestSupport;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Import(RequestIdFilterChainIntegrationTest.RequestIdEchoController.class)
class RequestIdFilterChainIntegrationTest extends MockMvcIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @DisplayName("RequestIdFilter가 Security filter chain에 연결되어 보호 API 요청에 requestId를 유지한다.")
    @Test
    void requestIdFilterIsWiredIntoSecurityFilterChain() throws Exception {
        // given
        String accessToken = jwtTokenProvider.createAccessToken(1L, "user@example.com");

        // when & then
        mockMvc.perform(get("/api/test/request-id")
                        .header(AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    @RestController
    static class RequestIdEchoController {

        @GetMapping("/api/test/request-id")
        Map<String, String> requestId() {
            return Map.of("requestId", MDC.get("requestId"));
        }
    }
}
