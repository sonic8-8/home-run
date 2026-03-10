package io.ssafy.p.j14c103.homerun.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ssafy.api")
public class SsafyApiProperties {

    private final String baseUrl;
    private final String apiKey;

    private SsafyApiProperties(final String baseUrl, final String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    public static SsafyApiProperties of(final String baseUrl, final String apiKey) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("SSAFY API base URL은 필수입니다.");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("SSAFY API KEY는 필수입니다.");
        }
        return new SsafyApiProperties(baseUrl, apiKey);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }
}
