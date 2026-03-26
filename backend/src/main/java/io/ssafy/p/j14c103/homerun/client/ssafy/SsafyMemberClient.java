package io.ssafy.p.j14c103.homerun.client.ssafy;

import io.ssafy.p.j14c103.homerun.config.SsafyApiProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class SsafyMemberClient {

    private static final String PROJECT_USER_ID_PREFIX = "j14c103+";
    private static final String PROJECT_USER_ID_DOMAIN = "@ssafy.co.kr";
    private static final int HASH_LENGTH = 20;

    private final RestTemplate restTemplate;
    private final SsafyApiProperties properties;

    @SuppressWarnings("unchecked")
    public Map<String, Object> createMember(final String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }

        final String url = properties.getBaseUrl().replace("/edu", "") + "/member";
        final Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("apiKey", properties.getApiKey());
        requestBody.put("userId", toSsafyUserId(email));

        final Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
        if (response == null) {
            throw new RuntimeException("SSAFY 회원 생성 응답이 없습니다.");
        }

        return response;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> searchMember(final String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }

        final String url = properties.getBaseUrl().replace("/edu", "") + "/member/search";
        final Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("apiKey", properties.getApiKey());
        requestBody.put("userId", toSsafyUserId(email));

        final Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
        if (response == null) {
            throw new RuntimeException("SSAFY 회원 조회 응답이 없습니다.");
        }

        return response;
    }

    private String toSsafyUserId(final String email) {
        return PROJECT_USER_ID_PREFIX + hashEmail(email) + PROJECT_USER_ID_DOMAIN;
    }

    private String hashEmail(final String email) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(email.trim().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
            return toHex(hash).substring(0, HASH_LENGTH);
        } catch (final NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 해시를 생성할 수 없습니다.", exception);
        }
    }

    private String toHex(final byte[] hash) {
        final StringBuilder builder = new StringBuilder(hash.length * 2);
        for (final byte value : hash) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }
}
