package io.ssafy.p.j14c103.homerun.config;

import static org.assertj.core.api.Assertions.assertThat;

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

    @DisplayName("FSS RestClient는 Spring builder와 timeout request factory로 구성된다")
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

                SimpleClientHttpRequestFactory requestFactory = (SimpleClientHttpRequestFactory) context
                    .getBean("fssClientHttpRequestFactory");
                assertThat(Objects.requireNonNull(ReflectionTestUtils.getField(requestFactory, "connectTimeout")))
                    .isEqualTo(2000);
                assertThat(Objects.requireNonNull(ReflectionTestUtils.getField(requestFactory, "readTimeout")))
                    .isEqualTo(5000);

                RestClient restClient = context.getBean("fssRestClient", RestClient.class);
                Object clientRequestFactory = ReflectionTestUtils.getField(restClient, "clientRequestFactory");
                assertThat(clientRequestFactory).isSameAs(requestFactory);
            });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(FssApiProperties.class)
    static class RestClientBuilderTestConfig {

        @Bean
        RestClient.Builder restClientBuilder() {
            return RestClient.builder();
        }
    }
}
