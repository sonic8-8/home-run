package io.ssafy.p.j14c103.homerun.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import java.net.URI;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.observation.ClientRequestObservationContext;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
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

    @DisplayName("외부 HTTP observation은 공통 client 태그로 메트릭을 남긴다")
    @Test
    void recordsHttpClientMetricWithCommonClientTag() {
        for (final String client : new String[] {"ssafy", "kis", "fss"}) {
            try (SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry()) {
                final ObservationRegistry observationRegistry = ObservationRegistry.create();
                observationRegistry.observationConfig()
                        .observationHandler(new DefaultMeterObservationHandler(meterRegistry));

                final MockClientHttpRequest request =
                        new MockClientHttpRequest(HttpMethod.GET, URI.create("https://example.test/ping"));
                final ClientRequestObservationContext context = new ClientRequestObservationContext(request);
                context.setUriTemplate("/ping");
                context.setResponse(new MockClientHttpResponse(new byte[0], HttpStatus.OK));

                final Observation observation = Observation.start(
                        new HomerunClientObservationConvention(client),
                        () -> context,
                        observationRegistry
                );
                observation.stop();

                final Timer timer = meterRegistry.get("http.client.requests")
                        .tag(HomerunClientObservationConvention.CLIENT_TAG, client)
                        .timer();
                assertThat(timer.count()).isEqualTo(1);
            }
        }
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
