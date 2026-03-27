package io.ssafy.p.j14c103.homerun.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.test.util.ReflectionTestUtils;

class KisRestClientConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(KisRestClientConfig.class)
            .withPropertyValues(
                    "kis.api.base-url=https://openapi.koreainvestment.com:9443",
                    "kis.api.app-key=test-app-key",
                    "kis.api.app-secret=test-app-secret"
            );

    @DisplayName("KIS RestClient는 2초 connect timeout과 5초 read timeout을 적용한다")
    @Test
    void bindKisRestClientWithTimeouts() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RestClient.class);
            assertThat(context).hasBean("kisClientHttpRequestFactory");

            ClientHttpRequestFactory requestFactory =
                    context.getBean("kisClientHttpRequestFactory", ClientHttpRequestFactory.class);
            assertThat(requestFactory).isInstanceOf(SimpleClientHttpRequestFactory.class);

            assertThat(timeoutMillis(requestFactory, "connectTimeout")).isEqualTo(2000);
            assertThat(timeoutMillis(requestFactory, "readTimeout")).isEqualTo(5000);
        });
    }

    private int timeoutMillis(final ClientHttpRequestFactory requestFactory, final String fieldName) {
        return Objects.requireNonNull((Number) ReflectionTestUtils.getField(requestFactory, fieldName)).intValue();
    }
}
