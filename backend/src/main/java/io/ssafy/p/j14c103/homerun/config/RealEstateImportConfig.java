package io.ssafy.p.j14c103.homerun.config;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.RealEstateMasterSyncService;
import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.RealEstateMasterSyncServiceRequest;
import java.util.List;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RealEstateImportProperties.class)
public class RealEstateImportConfig {

    @Bean
    @ConditionalOnProperty(name = "app.real-estate-import.enabled", havingValue = "true")
    public ApplicationRunner realEstateImportRunner(
        RealEstateImportProperties properties,
        RealEstateMasterSyncService realEstateMasterSyncService
    ) {
        return arguments -> {
            validateDatasetTypes(properties.getDatasetTypes());

            realEstateMasterSyncService.sync(
                RealEstateMasterSyncServiceRequest.of(
                    properties.getRegions(),
                    properties.getFromYearMonth(),
                    properties.getToYearMonth()
                )
            );
        };
    }

    private void validateDatasetTypes(List<String> datasetTypes) {
        if (datasetTypes == null || datasetTypes.isEmpty()) {
            throw new IllegalArgumentException("적재 대상 데이터셋은 필수입니다.");
        }
        if (datasetTypes.stream().anyMatch(datasetType -> !"APT_SALE".equals(datasetType))) {
            throw new IllegalArgumentException("현재 지원하는 적재 데이터셋은 APT_SALE만 가능합니다.");
        }
    }
}
