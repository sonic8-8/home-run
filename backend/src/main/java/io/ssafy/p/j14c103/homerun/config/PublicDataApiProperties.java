package io.ssafy.p.j14c103.homerun.config;

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
        this.legalDongBaseUrl = legalDongBaseUrl;
        this.apartmentTradeBaseUrl = apartmentTradeBaseUrl;
        this.serviceKey = serviceKey;
        this.pageSize = pageSize;
    }

    public static PublicDataApiProperties of(
        String legalDongBaseUrl,
        String apartmentTradeBaseUrl,
        String serviceKey,
        int pageSize
    ) {
        if (legalDongBaseUrl == null || legalDongBaseUrl.isBlank()) {
            throw new IllegalArgumentException("법정동 API base URL은 필수입니다.");
        }
        if (apartmentTradeBaseUrl == null || apartmentTradeBaseUrl.isBlank()) {
            throw new IllegalArgumentException("아파트 실거래가 API base URL은 필수입니다.");
        }
        if (serviceKey == null || serviceKey.isBlank()) {
            throw new IllegalArgumentException("공공데이터 API service key는 필수입니다.");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("공공데이터 API page size는 0보다 커야 합니다.");
        }

        return new PublicDataApiProperties(
            legalDongBaseUrl,
            apartmentTradeBaseUrl,
            serviceKey,
            pageSize
        );
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
