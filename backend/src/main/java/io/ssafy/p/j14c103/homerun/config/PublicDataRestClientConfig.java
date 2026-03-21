package io.ssafy.p.j14c103.homerun.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PublicDataApiProperties.class)
public class PublicDataRestClientConfig {
}
