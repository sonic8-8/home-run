package io.ssafy.p.j14c103.homerun.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnRealEstateImportEnabled
@EnableConfigurationProperties(PublicDataApiProperties.class)
public class PublicDataApiPropertiesConfig {
}
