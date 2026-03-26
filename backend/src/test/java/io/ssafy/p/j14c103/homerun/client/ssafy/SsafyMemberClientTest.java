package io.ssafy.p.j14c103.homerun.client.ssafy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import io.ssafy.p.j14c103.homerun.config.SsafyApiProperties;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.client.RestTemplate;

class SsafyMemberClientTest {

    @DisplayName("SSAFY 회원 생성과 조회는 프로젝트 prefix가 붙은 userId를 사용한다.")
    @Test
    @SuppressWarnings("unchecked")
    void createAndSearchMemberUsePrefixedUserId() {
        final RestTemplate restTemplate = mock(RestTemplate.class);
        final SsafyMemberClient client = new SsafyMemberClient(
                restTemplate,
                SsafyApiProperties.of("https://finopenapi.ssafy.io/ssafy/api/v1/edu", "test-api-key")
        );
        given(restTemplate.postForObject(any(String.class), any(Map.class), eq(Map.class)))
                .willReturn(Map.of("userKey", "test-user-key"));

        client.createMember("user@example.com");
        client.searchMember("user@example.com");

        final ArgumentCaptor<Map<String, Object>> requestBodyCaptor = ArgumentCaptor.forClass(Map.class);
        org.mockito.BDDMockito.then(restTemplate).should(org.mockito.Mockito.times(2))
                .postForObject(any(String.class), requestBodyCaptor.capture(), eq(Map.class));
        assertThat(requestBodyCaptor.getAllValues())
                .extracting(body -> body.get("userId"))
                .allSatisfy(userId -> {
                    assertThat(userId).isEqualTo("j14c103+b4c9a289323b21a01c3e@ssafy.co.kr");
                    assertThat(String.valueOf(userId)).hasSizeLessThanOrEqualTo(40);
                });
    }
}
