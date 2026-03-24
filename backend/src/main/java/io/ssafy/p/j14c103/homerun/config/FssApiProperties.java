package io.ssafy.p.j14c103.homerun.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fss.api")
public class FssApiProperties {

    private final String baseUrl;
    private final String authKey;

    private FssApiProperties(final String baseUrl, final String authKey) {
        this.baseUrl = baseUrl;
        this.authKey = authKey;
    }

    public static FssApiProperties of(final String baseUrl, final String authKey) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("FSS API base URL은 필수입니다.");
        }
        if (authKey == null || authKey.isBlank()) {
            throw new IllegalArgumentException("FSS API 인증키는 필수입니다.");
        }
        return new FssApiProperties(baseUrl, authKey);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getAuthKey() {
        return authKey;
    }
}
