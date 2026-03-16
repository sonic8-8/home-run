package io.ssafy.p.j14c103.homerun.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Clock;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private static final String EMAIL_CLAIM = "email";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final SecretKey secretKey;
    private final JwtProperties jwtProperties;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    static JwtTokenProvider forTest(
            JwtProperties jwtProperties,
            java.time.Clock timeClock
    ) {
        return new JwtTokenProvider(jwtProperties) {
            @Override
            protected Instant now() {
                return Instant.now(timeClock);
            }

            @Override
            protected Clock jwtClock() {
                return () -> Date.from(Instant.now(timeClock));
            }
        };
    }

    public String createAccessToken(Long userId, String email) {
        return createToken(userId, email, ACCESS_TOKEN_TYPE, jwtProperties.getAccessTokenTtlSeconds());
    }

    public String createRefreshToken(Long userId, String email) {
        return createToken(userId, email, REFRESH_TOKEN_TYPE, jwtProperties.getRefreshTokenTtlSeconds());
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public Long getUserId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    public String getEmail(String token) {
        return parseClaims(token).get(EMAIL_CLAIM, String.class);
    }

    public String getTokenType(String token) {
        return parseClaims(token).get(TOKEN_TYPE_CLAIM, String.class);
    }

    public void validateRefreshToken(String token) {
        parseRefreshClaims(token);
    }

    public Long getRefreshTokenUserId(String token) {
        return Long.parseLong(parseRefreshClaims(token).getSubject());
    }

    public String getRefreshTokenEmail(String token) {
        return parseRefreshClaims(token).get(EMAIL_CLAIM, String.class);
    }

    public Instant getExpiresAt(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    private String createToken(
            Long userId,
            String email,
            String tokenType,
            long ttlSeconds
    ) {
        Instant issuedAt = now();
        Instant expiresAt = issuedAt.plusSeconds(ttlSeconds);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(EMAIL_CLAIM, email)
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .clock(jwtClock())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Claims parseRefreshClaims(String token) {
        Claims claims = parseRefreshClaimsOrThrow(token);
        String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);

        if (!REFRESH_TOKEN_TYPE.equals(tokenType)) {
            throw HomerunException.from(ErrorCode.AUTH_REFRESH_INVALID);
        }

        return claims;
    }

    private Claims parseRefreshClaimsOrThrow(String token) {
        try {
            return parseClaims(token);
        } catch (ExpiredJwtException exception) {
            throw HomerunException.from(ErrorCode.AUTH_REFRESH_EXPIRED);
        } catch (JwtException | IllegalArgumentException exception) {
            throw HomerunException.from(ErrorCode.AUTH_REFRESH_INVALID);
        }
    }

    protected Instant now() {
        return Instant.now(java.time.Clock.systemUTC());
    }

    protected Clock jwtClock() {
        return () -> Date.from(now());
    }
}
