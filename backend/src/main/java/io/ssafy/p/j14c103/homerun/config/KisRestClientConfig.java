package io.ssafy.p.j14c103.homerun.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(KisApiProperties.class)
public class KisRestClientConfig {

    @Bean
    public RestClient kisRestClient(final KisApiProperties kisApiProperties) {
        return RestClient.builder()
                .baseUrl(kisApiProperties.getBaseUrl())
                .build();
    }
}
