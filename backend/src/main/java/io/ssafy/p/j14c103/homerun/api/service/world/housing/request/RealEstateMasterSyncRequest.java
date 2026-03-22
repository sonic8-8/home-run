package io.ssafy.p.j14c103.homerun.api.service.world.housing.request;

import java.time.YearMonth;
import java.util.List;

public class RealEstateMasterSyncRequest {

    private final List<String> regions;
    private final YearMonth fromYearMonth;
    private final YearMonth toYearMonth;

    private RealEstateMasterSyncRequest(
        List<String> regions,
        YearMonth fromYearMonth,
        YearMonth toYearMonth
    ) {
        this.regions = regions;
        this.fromYearMonth = fromYearMonth;
        this.toYearMonth = toYearMonth;
    }

    public static RealEstateMasterSyncRequest of(
        List<String> regions,
        YearMonth fromYearMonth,
        YearMonth toYearMonth
    ) {
        return new RealEstateMasterSyncRequest(regions, fromYearMonth, toYearMonth);
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
