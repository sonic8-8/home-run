package io.ssafy.p.j14c103.homerun.config;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
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
        validate(baseUrl, clientId, clientSecret);
        this.baseUrl = baseUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    private void validate(
        String baseUrl,
        String clientId,
        String clientSecret
    ) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
        if (clientId == null || clientId.isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
        if (clientSecret == null || clientSecret.isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
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
