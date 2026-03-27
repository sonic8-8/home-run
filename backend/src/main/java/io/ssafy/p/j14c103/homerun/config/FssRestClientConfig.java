package io.ssafy.p.j14c103.homerun.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(FssApiProperties.class)
public class FssRestClientConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    @Bean("fssClientHttpRequestFactory")
    public ClientHttpRequestFactory fssClientHttpRequestFactory() {
        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        return requestFactory;
    }

    @Bean("fssRestClient")
    public RestClient fssRestClient(
        final RestClient.Builder restClientBuilder,
        final FssApiProperties fssApiProperties,
        @Qualifier("fssClientHttpRequestFactory") final ClientHttpRequestFactory fssClientHttpRequestFactory
    ) {
        return restClientBuilder.clone()
            .baseUrl(fssApiProperties.getBaseUrl())
            .requestFactory(fssClientHttpRequestFactory)
            .observationConvention(new HomerunClientObservationConvention("fss"))
            .build();
    }
}
