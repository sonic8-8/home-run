package io.ssafy.p.j14c103.homerun.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.observation.ObservationRegistry;
import java.lang.reflect.Field;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

class SsafyRestClientConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(SsafyRestClientConfig.class, RestClientBuilderTestConfig.class)
            .withPropertyValues(
                    "ssafy.api.base-url=https://finopenapi.ssafy.io/ssafy/api/v1/edu",
                    "ssafy.api.api-key=test-api-key"
            );

    @DisplayName("SSAFY 클라이언트는 timeout request factory와 observation wiring을 함께 구성한다.")
    @Test
    void appliesTimeoutPolicyAndObservationWiring() {
        contextRunner.run(context -> {
            assertThat(context).hasBean("ssafyClientHttpRequestFactory");
            assertThat(context).hasSingleBean(RestTemplate.class);
            assertThat(context).hasSingleBean(RestClient.class);
            assertThat(context).hasSingleBean(ObservationRegistry.class);

            final ClientHttpRequestFactory requestFactory =
                    context.getBean("ssafyClientHttpRequestFactory", ClientHttpRequestFactory.class);
            assertThat(requestFactory).isInstanceOf(SimpleClientHttpRequestFactory.class);

            final RestTemplate restTemplate = context.getBean(RestTemplate.class);
            assertThat(restTemplate.getRequestFactory()).isSameAs(requestFactory);

            final RestClient restClient = context.getBean(RestClient.class);
            final ObservationRegistry observationRegistry = context.getBean(ObservationRegistry.class);
            assertThat(ReflectionTestUtils.getField(restClient, "clientRequestFactory")).isSameAs(requestFactory);
            assertThat(ReflectionTestUtils.getField(restClient, "observationRegistry")).isSameAs(observationRegistry);

            final Object observationConvention = ReflectionTestUtils.getField(restClient, "observationConvention");
            assertThat(observationConvention).isInstanceOf(HomerunClientObservationConvention.class);
            assertThat(ReflectionTestUtils.getField(observationConvention, "client")).isEqualTo("ssafy");

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

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(SsafyApiProperties.class)
    static class RestClientBuilderTestConfig {

        @Bean
        ObservationRegistry observationRegistry() {
            return ObservationRegistry.create();
        }

        @Bean
        RestClient.Builder restClientBuilder(final ObservationRegistry observationRegistry) {
            return RestClient.builder()
                    .observationRegistry(observationRegistry);
        }
    }
}
