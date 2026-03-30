package io.ssafy.p.j14c103.homerun.config;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.RealEstateMasterSyncService;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.RealEstateMasterSyncRequest;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingRegionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnRealEstateImportEnabled
@EnableConfigurationProperties(RealEstateImportProperties.class)
public class RealEstateImportConfig {

    @Bean
    public ApplicationRunner realEstateImportRunner(
        RealEstateImportProperties properties,
        RealEstateMasterSyncService realEstateMasterSyncService,
        HousingRegionRepository housingRegionRepository
    ) {
        return arguments -> {
            if (housingRegionRepository.count() > 0) {
                log.info("부동산 마스터 데이터 이미 존재, 임포트 생략");
                return;
            }
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
