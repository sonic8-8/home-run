package io.ssafy.p.j14c103.homerun.api.service.world.housing.request;

import java.time.YearMonth;
import java.util.List;

public class RealEstateMasterSyncServiceRequest {

    private final List<String> regions;
    private final YearMonth fromYearMonth;
    private final YearMonth toYearMonth;

    private RealEstateMasterSyncServiceRequest(
        List<String> regions,
        YearMonth fromYearMonth,
        YearMonth toYearMonth
    ) {
        this.regions = regions;
        this.fromYearMonth = fromYearMonth;
        this.toYearMonth = toYearMonth;
    }

    public static RealEstateMasterSyncServiceRequest of(
        List<String> regions,
        YearMonth fromYearMonth,
        YearMonth toYearMonth
    ) {
        return new RealEstateMasterSyncServiceRequest(regions, fromYearMonth, toYearMonth);
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
}
