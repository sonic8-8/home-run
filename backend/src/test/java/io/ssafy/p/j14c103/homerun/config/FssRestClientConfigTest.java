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

class FssRestClientConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(FssRestClientConfig.class, RestClientBuilderTestConfig.class);

    @DisplayName("FSS RestClient는 Spring builder, timeout request factory, observation wiring으로 구성된다")
    @Test
    void fssRestClientBean() {
        contextRunner
                .withPropertyValues(
                        "fss.api.base-url=https://finlife.fss.or.kr/finlifeapi",
                        "fss.api.auth-key=test-auth-key"
                )
                .run(context -> {
                    assertThat(context).hasBean("fssClientHttpRequestFactory");
                    assertThat(context).hasBean("fssRestClient");
                    assertThat(context).hasSingleBean(ClientHttpRequestFactory.class);
                    assertThat(context).hasSingleBean(RestClient.class);
                    assertThat(context).hasSingleBean(ObservationRegistry.class);

                    final SimpleClientHttpRequestFactory requestFactory =
                            (SimpleClientHttpRequestFactory) context.getBean("fssClientHttpRequestFactory");
                    assertThat(Objects.requireNonNull(ReflectionTestUtils.getField(requestFactory, "connectTimeout")))
                            .isEqualTo(2_000);
                    assertThat(Objects.requireNonNull(ReflectionTestUtils.getField(requestFactory, "readTimeout")))
                            .isEqualTo(5_000);

                    final RestClient restClient = context.getBean("fssRestClient", RestClient.class);
                    final ObservationRegistry observationRegistry = context.getBean(ObservationRegistry.class);
                    assertThat(ReflectionTestUtils.getField(restClient, "clientRequestFactory")).isSameAs(requestFactory);
                    assertThat(ReflectionTestUtils.getField(restClient, "observationRegistry")).isSameAs(observationRegistry);

                    final Object observationConvention = ReflectionTestUtils.getField(restClient, "observationConvention");
                    assertThat(observationConvention).isInstanceOf(HomerunClientObservationConvention.class);
                    assertThat(ReflectionTestUtils.getField(observationConvention, "client")).isEqualTo("fss");
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(FssApiProperties.class)
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
