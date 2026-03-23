package io.ssafy.p.j14c103.homerun.client.publicdata.realestate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.ssafy.p.j14c103.homerun.config.PublicDataApiPropertiesConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

@RestClientTest(
    value = LegalDongCodeClient.class,
    properties = {
        "app.real-estate-import.enabled=true",
        "public-data.api.legal-dong-base-url=https://apis.data.go.kr/1741000/StanReginCd",
        "public-data.api.apartment-trade-base-url=https://apis.data.go.kr/1613000/RTMSDataSvcAptTrade",
        "public-data.api.service-key=test-service-key",
        "public-data.api.page-size=500"
    }
)
@AutoConfigureWebClient(registerRestTemplate = true)
@Import(PublicDataApiPropertiesConfig.class)
class LegalDongCodeClientTest {

    @Autowired
    private LegalDongCodeClient legalDongCodeClient;

    @Autowired
    private MockRestServiceServer server;

    @DisplayName("법정동 코드 API 요청 URL과 파라미터를 조합해 XML 응답을 조회한다")
    @Test
    void fetch() {
        // given
        server.expect(requestTo(containsString(
                "https://apis.data.go.kr/1741000/StanReginCd/getStanReginCdList"
            )))
            .andExpect(method(GET))
            .andExpect(requestTo(containsString("ServiceKey=test-service-key")))
            .andExpect(requestTo(containsString("pageNo=1")))
            .andExpect(requestTo(containsString("numOfRows=500")))
            .andExpect(requestTo(containsString("type=xml")))
            .andExpect(requestTo(containsString("locatadd_nm=%EC%84%9C%EC%9A%B8%ED%8A%B9%EB%B3%84%EC%8B%9C")))
            .andRespond(withSuccess("<StanReginCd/>", MediaType.APPLICATION_XML));

        // when
        String result = legalDongCodeClient.fetch("서울특별시", 1, 500);

        // then
        assertThat(result).isEqualTo("<StanReginCd/>");
        server.verify();
    }
}
