package io.ssafy.p.j14c103.homerun.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

class SsafyRestClientConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(SsafyRestClientConfig.class)
            .withPropertyValues(
                    "ssafy.api.base-url=https://finopenapi.ssafy.io/ssafy/api/v1/edu",
                    "ssafy.api.api-key=test-api-key"
            );

    @DisplayName("SSAFY 클라이언트는 연결 2초, 읽기 5초 timeout이 적용된 request factory를 공유한다.")
    @Test
    void appliesTimeoutPolicyToSharedRequestFactory() {
        contextRunner.run(context -> {
            assertThat(context).hasBean("ssafyClientHttpRequestFactory");
            assertThat(context).hasSingleBean(RestTemplate.class);
            assertThat(context).hasSingleBean(RestClient.class);

            final ClientHttpRequestFactory requestFactory =
                    context.getBean("ssafyClientHttpRequestFactory", ClientHttpRequestFactory.class);
            assertThat(requestFactory).isInstanceOf(SimpleClientHttpRequestFactory.class);

            final RestTemplate restTemplate = context.getBean(RestTemplate.class);
            assertThat(restTemplate.getRequestFactory()).isSameAs(requestFactory);

            final SimpleClientHttpRequestFactory simpleRequestFactory =
                    (SimpleClientHttpRequestFactory) requestFactory;
            assertThat(readIntField(simpleRequestFactory, "connectTimeout")).isEqualTo(2_000);
            assertThat(readIntField(simpleRequestFactory, "readTimeout")).isEqualTo(5_000);
        });
    }

    private static int readIntField(final SimpleClientHttpRequestFactory requestFactory, final String fieldName) {
        try {
            final Field field = SimpleClientHttpRequestFactory.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getInt(requestFactory);
        } catch (final ReflectiveOperationException exception) {
            throw new IllegalStateException("SSAFY request factory timeout 값을 확인할 수 없습니다.", exception);
        }
    }
}
