package io.ssafy.p.j14c103.homerun.config;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "public-data.api")
public class PublicDataApiProperties {

    private final String legalDongBaseUrl;
    private final String apartmentTradeBaseUrl;
    private final String serviceKey;
    private final int pageSize;

    public PublicDataApiProperties(
        String legalDongBaseUrl,
        String apartmentTradeBaseUrl,
        String serviceKey,
        int pageSize
    ) {
        validate(legalDongBaseUrl, apartmentTradeBaseUrl, serviceKey, pageSize);
        this.legalDongBaseUrl = legalDongBaseUrl;
        this.apartmentTradeBaseUrl = apartmentTradeBaseUrl;
        this.serviceKey = serviceKey;
        this.pageSize = pageSize;
    }

    private void validate(
        String legalDongBaseUrl,
        String apartmentTradeBaseUrl,
        String serviceKey,
        int pageSize
    ) {
        if (legalDongBaseUrl == null || legalDongBaseUrl.isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
        if (apartmentTradeBaseUrl == null || apartmentTradeBaseUrl.isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
        if (serviceKey == null || serviceKey.isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
        if (pageSize <= 0) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }

    public String getLegalDongBaseUrl() {
        return legalDongBaseUrl;
    }

    public String getApartmentTradeBaseUrl() {
        return apartmentTradeBaseUrl;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public int getPageSize() {
        return pageSize;
    }
}
