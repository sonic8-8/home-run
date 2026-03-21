package io.ssafy.p.j14c103.homerun.config;

import java.time.YearMonth;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.real-estate-import")
public class RealEstateImportProperties {

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
        this.enabled = enabled;
        this.regions = regions;
        this.fromYearMonth = fromYearMonth;
        this.toYearMonth = toYearMonth;
        this.datasetTypes = datasetTypes;
    }

    public static RealEstateImportProperties of(
        boolean enabled,
        List<String> regions,
        YearMonth fromYearMonth,
        YearMonth toYearMonth,
        List<String> datasetTypes
    ) {
        if (regions == null || regions.isEmpty()) {
            throw new IllegalArgumentException("적재 대상 지역은 필수입니다.");
        }
        if (fromYearMonth == null || toYearMonth == null) {
            throw new IllegalArgumentException("적재 대상 기간은 필수입니다.");
        }
        if (datasetTypes == null || datasetTypes.isEmpty()) {
            throw new IllegalArgumentException("적재 대상 데이터셋은 필수입니다.");
        }

        return new RealEstateImportProperties(
            enabled,
            regions,
            fromYearMonth,
            toYearMonth,
            datasetTypes
        );
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
