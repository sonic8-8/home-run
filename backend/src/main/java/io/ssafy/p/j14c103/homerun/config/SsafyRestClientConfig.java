package io.ssafy.p.j14c103.homerun.config;

import java.time.Duration;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(SsafyApiProperties.class)
public class SsafyRestClientConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    @Bean("ssafyClientHttpRequestFactory")
    public ClientHttpRequestFactory ssafyClientHttpRequestFactory() {
        final ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
                .withTimeouts(CONNECT_TIMEOUT, READ_TIMEOUT);
        return ClientHttpRequestFactoryBuilder.simple().build(settings);
    }

    @Bean
    public RestTemplate ssafyRestTemplate(
            @Qualifier("ssafyClientHttpRequestFactory")
            final ClientHttpRequestFactory ssafyClientHttpRequestFactory) {
        return new RestTemplate(ssafyClientHttpRequestFactory);
    }

    @Bean
    public RestClient ssafyRestClient(
            final SsafyApiProperties properties,
            @Qualifier("ssafyClientHttpRequestFactory")
            final ClientHttpRequestFactory ssafyClientHttpRequestFactory) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(ssafyClientHttpRequestFactory)
                .build();
    }
}
