package io.ssafy.p.j14c103.homerun.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.YearMonth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class RealEstatePropertiesBindingTest {

    private final ApplicationContextRunner publicDataRunner = new ApplicationContextRunner()
        .withUserConfiguration(PublicDataPropertiesTestConfig.class);

    private final ApplicationContextRunner naverRunner = new ApplicationContextRunner()
        .withUserConfiguration(NaverGeocodingPropertiesTestConfig.class);

    private final ApplicationContextRunner realEstateImportRunner = new ApplicationContextRunner()
        .withUserConfiguration(RealEstateImportPropertiesTestConfig.class);

    @DisplayName("공공데이터 API 설정이 모두 있으면 properties bean을 바인딩한다")
    @Test
    void bindPublicDataApiProperties() {
        publicDataRunner
            .withPropertyValues(
                "public-data.api.legal-dong-base-url=https://apis.data.go.kr/1741000/StanReginCd",
                "public-data.api.apartment-trade-base-url=https://apis.data.go.kr/1613000/RTMSDataSvcAptTrade",
                "public-data.api.service-key=test-service-key",
                "public-data.api.page-size=500"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(PublicDataApiProperties.class);
                PublicDataApiProperties properties = context.getBean(PublicDataApiProperties.class);
                assertThat(properties.getPageSize()).isEqualTo(500);
            });
    }

    @DisplayName("공공데이터 API page size가 0이면 설정 예외로 실패한다")
    @Test
    void bindPublicDataApiPropertiesWithInvalidPageSize() {
        publicDataRunner
            .withPropertyValues(
                "public-data.api.legal-dong-base-url=https://apis.data.go.kr/1741000/StanReginCd",
                "public-data.api.apartment-trade-base-url=https://apis.data.go.kr/1613000/RTMSDataSvcAptTrade",
                "public-data.api.service-key=test-service-key",
                "public-data.api.page-size=0"
            )
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(HomerunException.class);
            });
    }

    @DisplayName("네이버 지오코딩 client id가 비어 있으면 설정 예외로 실패한다")
    @Test
    void bindNaverGeocodingPropertiesWithBlankClientId() {
        naverRunner
            .withPropertyValues(
                "naver.geocoding.base-url=https://maps.apigw.ntruss.com/map-geocode/v2/geocode",
                "naver.geocoding.client-id=",
                "naver.geocoding.client-secret=test-client-secret"
            )
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(HomerunException.class);
            });
    }

    @DisplayName("부동산 적재 dataset type이 지원 범위를 벗어나면 설정 예외로 실패한다")
    @Test
    void bindRealEstateImportPropertiesWithUnsupportedDatasetType() {
        realEstateImportRunner
            .withPropertyValues(
                "app.real-estate-import.enabled=true",
                "app.real-estate-import.regions[0]=SEOUL",
                "app.real-estate-import.from-year-month=2024-01",
                "app.real-estate-import.to-year-month=2024-12",
                "app.real-estate-import.dataset-types[0]=VILLA_SALE"
            )
            .run(context -> {
                assertThat(context).hasFailed();
                assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(HomerunException.class);
            });
    }

    @DisplayName("부동산 적재 설정이 모두 있으면 properties bean을 바인딩한다")
    @Test
    void bindRealEstateImportProperties() {
        realEstateImportRunner
            .withPropertyValues(
                "app.real-estate-import.enabled=true",
                "app.real-estate-import.regions[0]=SEOUL",
                "app.real-estate-import.regions[1]=GWANGJU",
                "app.real-estate-import.from-year-month=2024-01",
                "app.real-estate-import.to-year-month=2024-12",
                "app.real-estate-import.dataset-types[0]=APT_SALE"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(RealEstateImportProperties.class);
                RealEstateImportProperties properties = context.getBean(RealEstateImportProperties.class);
                assertThat(properties.getRegions()).containsExactly("SEOUL", "GWANGJU");
                assertThat(properties.getFromYearMonth()).isEqualTo(YearMonth.of(2024, 1));
                assertThat(properties.getToYearMonth()).isEqualTo(YearMonth.of(2024, 12));
            });
    }

    @DisplayName("부동산 적재 설정은 쉼표로 구분한 문자열도 목록으로 바인딩한다")
    @Test
    void bindRealEstateImportPropertiesFromCommaSeparatedValues() {
        realEstateImportRunner
            .withPropertyValues(
                "app.real-estate-import.enabled=true",
                "app.real-estate-import.regions=SEOUL,GWANGJU",
                "app.real-estate-import.from-year-month=2024-01",
                "app.real-estate-import.to-year-month=2024-12",
                "app.real-estate-import.dataset-types=APT_SALE"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(RealEstateImportProperties.class);
                RealEstateImportProperties properties = context.getBean(RealEstateImportProperties.class);
                assertThat(properties.getRegions()).containsExactly("SEOUL", "GWANGJU");
                assertThat(properties.getDatasetTypes()).containsExactly("APT_SALE");
            });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(PublicDataApiProperties.class)
    static class PublicDataPropertiesTestConfig {
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(NaverGeocodingProperties.class)
    static class NaverGeocodingPropertiesTestConfig {
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(RealEstateImportProperties.class)
    static class RealEstateImportPropertiesTestConfig {
    }
}
