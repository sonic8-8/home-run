package io.ssafy.p.j14c103.homerun.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties({SsafyApiProperties.class, SsafyAccountProperties.class})
public class SsafyRestClientConfig {

    @Bean
    public RestTemplate ssafyRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public RestClient ssafyRestClient(final SsafyApiProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }
}
