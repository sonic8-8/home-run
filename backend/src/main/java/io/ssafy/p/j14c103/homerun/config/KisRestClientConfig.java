package io.ssafy.p.j14c103.homerun.config;

import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(KisApiProperties.class)
public class KisRestClientConfig {

    @Bean("kisClientHttpRequestFactory")
    public ClientHttpRequestFactory kisClientHttpRequestFactory() {
        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(2));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        return requestFactory;
    }

    @Bean
    public RestClient kisRestClient(
            final RestClient.Builder restClientBuilder,
            final KisApiProperties kisApiProperties,
            @Qualifier("kisClientHttpRequestFactory")
            final ClientHttpRequestFactory kisClientHttpRequestFactory
    ) {
        return restClientBuilder.clone()
                .baseUrl(kisApiProperties.getBaseUrl())
                .requestFactory(kisClientHttpRequestFactory)
                .observationConvention(new HomerunClientObservationConvention("kis"))
                .build();
    }
}
