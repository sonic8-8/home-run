package io.ssafy.p.j14c103.homerun.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kis.api")
public class KisApiProperties {

    private final String baseUrl;
    private final String appKey;
    private final String appSecret;

    private KisApiProperties(final String baseUrl, final String appKey, final String appSecret) {
        this.baseUrl = baseUrl;
        this.appKey = appKey;
        this.appSecret = appSecret;
    }

    public static KisApiProperties of(final String baseUrl, final String appKey, final String appSecret) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("KIS API base URL은 필수입니다.");
        }
        if (appKey == null || appKey.isBlank()) {
            throw new IllegalArgumentException("KIS API app-key는 필수입니다.");
        }
        if (appSecret == null || appSecret.isBlank()) {
            throw new IllegalArgumentException("KIS API app-secret은 필수입니다.");
        }
        return new KisApiProperties(baseUrl, appKey, appSecret);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getAppSecret() {
        return appSecret;
    }
}
