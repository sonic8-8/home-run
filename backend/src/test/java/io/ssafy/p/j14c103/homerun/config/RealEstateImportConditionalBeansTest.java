package io.ssafy.p.j14c103.homerun.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.client.naver.NaverGeocodingClient;
import io.ssafy.p.j14c103.homerun.client.publicdata.realestate.ApartmentTradeClient;
import io.ssafy.p.j14c103.homerun.client.publicdata.realestate.ApartmentTradeResponseParser;
import io.ssafy.p.j14c103.homerun.client.publicdata.realestate.LegalDongCodeClient;
import io.ssafy.p.j14c103.homerun.client.publicdata.realestate.LegalDongCodeResponseParser;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestTemplate;

class RealEstateImportConditionalBeansTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(
            PublicDataApiPropertiesConfig.class,
            NaverGeocodingPropertiesConfig.class,
            LegalDongCodeClient.class,
            ApartmentTradeClient.class,
            LegalDongCodeResponseParser.class,
            ApartmentTradeResponseParser.class,
            NaverGeocodingClient.class
        )
        .withBean(RestTemplate.class, RestTemplate::new);

    @DisplayName("부동산 적재가 비활성화되면 외부 API 설정 없이도 import 전용 bean을 생성하지 않는다")
    @Test
    void doesNotCreateImportBeansWhenDisabled() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(PublicDataApiProperties.class);
            assertThat(context).doesNotHaveBean(NaverGeocodingProperties.class);
            assertThat(context).doesNotHaveBean(LegalDongCodeClient.class);
            assertThat(context).doesNotHaveBean(ApartmentTradeClient.class);
            assertThat(context).doesNotHaveBean(NaverGeocodingClient.class);
            assertThat(context).doesNotHaveBean(LegalDongCodeResponseParser.class);
            assertThat(context).doesNotHaveBean(ApartmentTradeResponseParser.class);
        });
    }

    @DisplayName("부동산 적재가 활성화되면 공공데이터 서비스 키 누락 시 컨텍스트가 실패한다")
    @Test
    void failsWithoutPublicDataServiceKeyWhenEnabled() {
        contextRunner
            .withPropertyValues(
                "app.real-estate-import.enabled=true",
                "naver.geocoding.base-url=https://maps.apigw.ntruss.com/map-geocode/v2/geocode",
                "naver.geocoding.client-id=test-client-id",
                "naver.geocoding.client-secret=test-client-secret"
            )
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(HomerunException.class);
            });
    }

    @DisplayName("부동산 적재가 활성화되면 네이버 지오코딩 키 누락 시 컨텍스트가 실패한다")
    @Test
    void failsWithoutNaverKeysWhenEnabled() {
        contextRunner
            .withPropertyValues(
                "app.real-estate-import.enabled=true",
                "public-data.api.legal-dong-base-url=https://apis.data.go.kr/1741000/StanReginCd",
                "public-data.api.apartment-trade-base-url=https://apis.data.go.kr/1613000/RTMSDataSvcAptTrade",
                "public-data.api.service-key=test-service-key",
                "public-data.api.page-size=500"
            )
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(HomerunException.class);
            });
    }
}
