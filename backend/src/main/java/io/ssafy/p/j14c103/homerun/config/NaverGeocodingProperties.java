package io.ssafy.p.j14c103.homerun.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "naver.geocoding")
public class NaverGeocodingProperties {

    private final String baseUrl;
    private final String clientId;
    private final String clientSecret;

    public NaverGeocodingProperties(
        String baseUrl,
        String clientId,
        String clientSecret
    ) {
        this.baseUrl = baseUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public static NaverGeocodingProperties of(
        String baseUrl,
        String clientId,
        String clientSecret
    ) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("네이버 지오코딩 base URL은 필수입니다.");
        }
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("네이버 지오코딩 client id는 필수입니다.");
        }
        if (clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalArgumentException("네이버 지오코딩 client secret은 필수입니다.");
        }

        return new NaverGeocodingProperties(baseUrl, clientId, clientSecret);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
