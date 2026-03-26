package io.ssafy.p.j14c103.homerun.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestIdFilterTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @DisplayName("HTTP 요청이 들어오면 requestId를 생성하고 요청 종료 후 MDC를 정리한다.")
    @Test
    void generateRequestIdAndClearMdcAfterRequest() throws Exception {
        // given
        RequestIdFilter filter = new RequestIdFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        CapturingFilterChain filterChain = new CapturingFilterChain();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(filterChain.requestIdInChain).isNotBlank();
        assertThat(MDC.get("requestId")).isNull();
    }

    @DisplayName("필터 체인에서 예외가 발생해도 requestId를 정리한다.")
    @Test
    void clearMdcWhenFilterChainThrows() {
        // given
        RequestIdFilter filter = new RequestIdFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = (req, res) -> {
            throw new IllegalStateException("boom");
        };

        // when & then
        assertThatThrownBy(() -> filter.doFilter(request, response, filterChain))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");
        assertThat(MDC.get("requestId")).isNull();
    }

    private static final class CapturingFilterChain implements FilterChain {

        private String requestIdInChain;

        @Override
        public void doFilter(
                jakarta.servlet.ServletRequest request,
                jakarta.servlet.ServletResponse response
        ) {
            requestIdInChain = MDC.get("requestId");
        }
    }
}
