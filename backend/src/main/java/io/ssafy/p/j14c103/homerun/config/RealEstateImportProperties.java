package io.ssafy.p.j14c103.homerun.config;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.YearMonth;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.real-estate-import")
public class RealEstateImportProperties {

    private static final String SUPPORTED_DATASET_TYPE = "APT_SALE";

    private final boolean enabled;
    private final List<String> regions;
    private final YearMonth fromYearMonth;
    private final YearMonth toYearMonth;
    private final List<String> datasetTypes;

    public RealEstateImportProperties(
        boolean enabled,
        List<String> regions,
        YearMonth fromYearMonth,
        YearMonth toYearMonth,
        List<String> datasetTypes
    ) {
        validate(regions, fromYearMonth, toYearMonth, datasetTypes);
        this.enabled = enabled;
        this.regions = regions;
        this.fromYearMonth = fromYearMonth;
        this.toYearMonth = toYearMonth;
        this.datasetTypes = datasetTypes;
    }

    private void validate(
        List<String> regions,
        YearMonth fromYearMonth,
        YearMonth toYearMonth,
        List<String> datasetTypes
    ) {
        if (regions == null || regions.isEmpty()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
        if (fromYearMonth == null || toYearMonth == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
        if (fromYearMonth.isAfter(toYearMonth)) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
        if (datasetTypes == null || datasetTypes.isEmpty()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
        if (datasetTypes.stream().anyMatch(datasetType -> !SUPPORTED_DATASET_TYPE.equals(datasetType))) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<String> getRegions() {
        return regions;
    }

    public YearMonth getFromYearMonth() {
        return fromYearMonth;
    }

    public YearMonth getToYearMonth() {
        return toYearMonth;
    }

    public List<String> getDatasetTypes() {
        return datasetTypes;
    }
}
