package io.ssafy.p.j14c103.homerun.client.naver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.ssafy.p.j14c103.homerun.config.NaverGeocodingPropertiesConfig;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

@RestClientTest(
    value = NaverGeocodingClient.class,
    properties = {
        "app.real-estate-import.enabled=true",
        "naver.geocoding.base-url=https://maps.apigw.ntruss.com/map-geocode/v2/geocode",
        "naver.geocoding.client-id=test-client-id",
        "naver.geocoding.client-secret=test-client-secret"
    }
)
@AutoConfigureWebClient(registerRestTemplate = true)
@Import(NaverGeocodingPropertiesConfig.class)
class NaverGeocodingClientTest {

    @Autowired
    private NaverGeocodingClient naverGeocodingClient;

    @Autowired
    private MockRestServiceServer server;

    @DisplayName("네이버 지오코딩 요청 헤더와 JSON 응답을 파싱한다")
    @Test
    void geocode() {
        // given
        server.expect(requestTo(containsString("https://maps.apigw.ntruss.com/map-geocode/v2/geocode")))
            .andExpect(method(GET))
            .andExpect(header("x-ncp-apigw-api-key-id", "test-client-id"))
            .andExpect(header("x-ncp-apigw-api-key", "test-client-secret"))
            .andExpect(header(ACCEPT, containsString(MediaType.APPLICATION_JSON_VALUE)))
            .andRespond(withSuccess(
                """
                {
                  "status": "OK",
                  "meta": {
                    "totalCount": 1,
                    "page": 1,
                    "count": 1
                  },
                  "addresses": [
                    {
                      "roadAddress": "경기도 성남시 분당구 불정로 6 NAVER그린팩토리",
                      "jibunAddress": "경기도 성남시 분당구 정자동 178-1 NAVER그린팩토리",
                      "x": "127.1054328",
                      "y": "37.3595963",
                      "distance": 0.0
                    }
                  ],
                  "errorMessage": ""
                }
                """,
                MediaType.APPLICATION_JSON
            ));

        // when
        Optional<NaverGeocodingClient.GeocodingResult> result = naverGeocodingClient.geocode("분당구 불정로 6");

        // then
        assertThat(result).isPresent();
        assertThat(result.orElseThrow().latitude()).isEqualByComparingTo(BigDecimal.valueOf(37.3595963));
        assertThat(result.orElseThrow().longitude()).isEqualByComparingTo(BigDecimal.valueOf(127.1054328));
        assertThat(result.orElseThrow().roadAddress()).contains("불정로 6");
        assertThat(result.orElseThrow().jibunAddress()).contains("정자동 178-1");
        server.verify();
    }

    @DisplayName("네이버 지오코딩 결과 수가 0이면 empty를 반환한다")
    @Test
    void geocodeWithNoResult() {
        // given
        server.expect(requestTo(containsString("https://maps.apigw.ntruss.com/map-geocode/v2/geocode")))
            .andExpect(method(GET))
            .andRespond(withSuccess(
                """
                {
                  "status": "OK",
                  "meta": {
                    "totalCount": 0,
                    "page": 1,
                    "count": 0
                  },
                  "addresses": [],
                  "errorMessage": ""
                }
                """,
                MediaType.APPLICATION_JSON
            ));

        // when & then
        assertThat(naverGeocodingClient.geocode("서울특별시 강동구 고덕동 BL-3-1")).isEmpty();
    }

    @DisplayName("네이버 지오코딩 status가 OK가 아니면 외부 응답 예외를 던진다")
    @Test
    void geocodeWithFailureStatus() {
        // given
        server.expect(requestTo(containsString("https://maps.apigw.ntruss.com/map-geocode/v2/geocode")))
            .andExpect(method(GET))
            .andExpect(header(ACCEPT, containsString(MediaType.APPLICATION_JSON_VALUE)))
            .andRespond(withSuccess(
                """
                {
                  "status": "ERROR",
                  "meta": {
                    "totalCount": 0,
                    "page": 1,
                    "count": 0
                  },
                  "addresses": [],
                  "errorMessage": "invalid query"
                }
                """,
                MediaType.APPLICATION_JSON
            ));

        // when & then
        assertThatThrownBy(() -> naverGeocodingClient.geocode("bad query"))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
    }
}
