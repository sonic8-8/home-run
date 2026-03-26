package io.ssafy.p.j14c103.homerun.config;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(
        classes = RequestIdFilterChainIntegrationTest.TestApplication.class,
        properties = {
                "jwt.secret=01234567890123456789012345678901",
                "jwt.access-token-ttl-seconds=1800",
                "jwt.refresh-token-ttl-seconds=1209600"
        }
)
@AutoConfigureMockMvc
class RequestIdFilterChainIntegrationTest {

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

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            JpaRepositoriesAutoConfiguration.class,
            RedisAutoConfiguration.class,
            RedisRepositoriesAutoConfiguration.class
    })
    @Import({
            SecurityConfig.class,
            JwtProperties.class,
            JwtTokenProvider.class,
            RequestIdEchoController.class
    })
    static class TestApplication {
    }

    @RestController
    static class RequestIdEchoController {

        @GetMapping("/api/test/request-id")
        Map<String, String> requestId() {
            return Map.of("requestId", MDC.get("requestId"));
        }
    }
}
