package io.ssafy.p.j14c103.homerun.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.observation.ObservationRegistry;
import java.util.Objects;
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

class KisRestClientConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(KisRestClientConfig.class, RestClientBuilderTestConfig.class)
            .withPropertyValues(
                    "kis.api.base-url=https://openapi.koreainvestment.com:9443",
                    "kis.api.app-key=test-app-key",
                    "kis.api.app-secret=test-app-secret"
            );

    @DisplayName("KIS RestClient는 timeout request factory와 observation wiring을 함께 구성한다")
    @Test
    void bindsKisRestClientWithTimeoutsAndObservation() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RestClient.class);
            assertThat(context).hasBean("kisClientHttpRequestFactory");
            assertThat(context).hasSingleBean(ObservationRegistry.class);

            final ClientHttpRequestFactory requestFactory =
                    context.getBean("kisClientHttpRequestFactory", ClientHttpRequestFactory.class);
            assertThat(requestFactory).isInstanceOf(SimpleClientHttpRequestFactory.class);
            assertThat(timeoutMillis(requestFactory, "connectTimeout")).isEqualTo(2_000);
            assertThat(timeoutMillis(requestFactory, "readTimeout")).isEqualTo(5_000);

            final RestClient restClient = context.getBean(RestClient.class);
            final ObservationRegistry observationRegistry = context.getBean(ObservationRegistry.class);
            assertThat(ReflectionTestUtils.getField(restClient, "clientRequestFactory")).isSameAs(requestFactory);
            assertThat(ReflectionTestUtils.getField(restClient, "observationRegistry")).isSameAs(observationRegistry);

            final Object observationConvention = ReflectionTestUtils.getField(restClient, "observationConvention");
            assertThat(observationConvention).isInstanceOf(HomerunClientObservationConvention.class);
            assertThat(ReflectionTestUtils.getField(observationConvention, "client")).isEqualTo("kis");
        });
    }

    private int timeoutMillis(final ClientHttpRequestFactory requestFactory, final String fieldName) {
        return Objects.requireNonNull((Number) ReflectionTestUtils.getField(requestFactory, fieldName)).intValue();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(KisApiProperties.class)
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
