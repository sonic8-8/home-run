package io.ssafy.p.j14c103.homerun.config;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.RealEstateMasterSyncService;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.RealEstateMasterSyncRequest;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnRealEstateImportEnabled
@EnableConfigurationProperties(RealEstateImportProperties.class)
public class RealEstateImportConfig {

    @Bean
    public ApplicationRunner realEstateImportRunner(
        RealEstateImportProperties properties,
        RealEstateMasterSyncService realEstateMasterSyncService
    ) {
        return arguments -> {
            realEstateMasterSyncService.sync(
                RealEstateMasterSyncRequest.of(
                    properties.getRegions(),
                    properties.getFromYearMonth(),
                    properties.getToYearMonth()
                )
            );
        };
    }
}
