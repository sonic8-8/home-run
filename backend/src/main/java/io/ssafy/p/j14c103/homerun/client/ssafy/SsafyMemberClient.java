package io.ssafy.p.j14c103.homerun.client.ssafy;

import io.ssafy.p.j14c103.homerun.config.SsafyApiProperties;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class SsafyMemberClient {

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
        requestBody.put("userId", email);

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
        requestBody.put("userId", email);

        final Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
        if (response == null) {
            throw new RuntimeException("SSAFY 회원 조회 응답이 없습니다.");
        }

        return response;
    }
}
