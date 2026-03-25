package io.ssafy.p.j14c103.homerun.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;

class JwtAuthenticationEntryPointTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @DisplayName("인증 실패가 발생하면 401과 공통 ErrorResponse JSON을 반환한다.")
    @Test
    void commence() throws Exception {
        // given
        AuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint(objectMapper);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException exception = new UsernameNotFoundException("authentication required");

        // when
        entryPoint.commence(request, response, exception);

        JsonNode body = objectMapper.readTree(response.getContentAsString());

        // then
        assertThat(response.getStatus()).isEqualTo(ErrorCode.AUTH_UNAUTHORIZED.getStatus().value());
        assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
        assertThat(body.get("code").asText()).isEqualTo(ErrorCode.AUTH_UNAUTHORIZED.getCode());
        assertThat(body.get("message").asText()).isEqualTo(ErrorCode.AUTH_UNAUTHORIZED.getMessage());
        assertThat(body.get("errors").isArray()).isTrue();
        assertThat(body.get("errors").isEmpty()).isTrue();
    }
}
