package io.ssafy.p.j14c103.homerun.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import jakarta.servlet.Filter;
import io.ssafy.p.j14c103.homerun.domain.user.auth.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

    private final JwtTokenProvider jwtTokenProvider = JwtTokenProvider.forTest(
            JwtProperties.of("01234567890123456789012345678901", 1800L, 1209600L),
            java.time.Clock.systemUTC()
    );

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @DisplayName("유효한 Access Token이면 인증 객체를 SecurityContext에 저장하고 다음 필터로 진행한다.")
    @Test
    void doFilterWithValidAccessToken() throws Exception {
        // given
        Filter filter = new JwtAuthenticationFilter(jwtTokenProvider);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        String accessToken = jwtTokenProvider.createAccessToken(1L, "user@example.com");

        request.addHeader(AUTHORIZATION, bearer(accessToken));

        // when
        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();

        // then
        assertThat(filterChain.getRequest()).isSameAs(request);
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(principal.getUserId()).isEqualTo(1L);
        assertThat(principal.getEmail()).isEqualTo("user@example.com");
    }

    @DisplayName("Authorization 헤더가 없으면 인증 없이 다음 필터로 진행한다.")
    @Test
    void doFilterWithoutAuthorizationHeader() throws Exception {
        // given
        Filter filter = new JwtAuthenticationFilter(jwtTokenProvider);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(filterChain.getRequest()).isSameAs(request);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @DisplayName("Authorization 헤더가 없어도 token 쿼리 파라미터가 있으면 인증 객체를 저장한다.")
    @Test
    void doFilterWithTokenQueryParameter() throws Exception {
        // given
        Filter filter = new JwtAuthenticationFilter(jwtTokenProvider);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        String accessToken = jwtTokenProvider.createAccessToken(1L, "user@example.com");

        request.setParameter("token", accessToken);

        // when
        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();

        // then
        assertThat(filterChain.getRequest()).isSameAs(request);
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(principal.getUserId()).isEqualTo(1L);
        assertThat(principal.getEmail()).isEqualTo("user@example.com");
    }

    @DisplayName("Refresh Token이면 인증 객체를 저장하지 않고 다음 필터로 진행한다.")
    @Test
    void doFilterWithRefreshToken() throws Exception {
        // given
        Filter filter = new JwtAuthenticationFilter(jwtTokenProvider);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        String refreshToken = jwtTokenProvider.createRefreshToken(1L, "user@example.com");

        request.addHeader(AUTHORIZATION, bearer(refreshToken));

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(filterChain.getRequest()).isSameAs(request);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @DisplayName("잘못된 토큰이면 인증 객체를 저장하지 않고 다음 필터로 진행한다.")
    @Test
    void doFilterWithInvalidToken() throws Exception {
        // given
        Filter filter = new JwtAuthenticationFilter(jwtTokenProvider);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        request.addHeader(AUTHORIZATION, bearer("invalid-token"));

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(filterChain.getRequest()).isSameAs(request);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
