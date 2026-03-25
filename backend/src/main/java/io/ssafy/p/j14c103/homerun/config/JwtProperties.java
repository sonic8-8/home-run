package io.ssafy.p.j14c103.homerun.config;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    @NotBlank
    private String secret;

    @Positive
    private long accessTokenTtlSeconds;

    @Positive
    private long refreshTokenTtlSeconds;

    public static JwtProperties of(
            String secret,
            long accessTokenTtlSeconds,
            long refreshTokenTtlSeconds
    ) {
        validate(secret, accessTokenTtlSeconds, refreshTokenTtlSeconds);

        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret(secret);
        jwtProperties.setAccessTokenTtlSeconds(accessTokenTtlSeconds);
        jwtProperties.setRefreshTokenTtlSeconds(refreshTokenTtlSeconds);

        return jwtProperties;
    }

    private static void validate(
            String secret,
            long accessTokenTtlSeconds,
            long refreshTokenTtlSeconds
    ) {
        if (secret == null || secret.isBlank()) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
        if (accessTokenTtlSeconds <= 0 || refreshTokenTtlSeconds <= 0) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
    }
}
