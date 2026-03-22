package io.ssafy.p.j14c103.homerun.api.service.world.housing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.RealEstateMasterImportRequest;
import io.ssafy.p.j14c103.homerun.client.naver.NaverGeocodingClient;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingDistrictRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingLegalDongRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingRegionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateGeocodeCacheRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstateProperty;
import io.ssafy.p.j14c103.homerun.domain.world.housing.RealEstatePropertyRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.trade.ApartmentTradeRawRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class RealEstateMasterImportServiceTest {

    @Autowired
    private RealEstateMasterImportService realEstateMasterImportService;

    @Autowired
    private HousingRegionRepository housingRegionRepository;

    @Autowired
    private HousingDistrictRepository housingDistrictRepository;

    @Autowired
    private HousingLegalDongRepository housingLegalDongRepository;

    @Autowired
    private ApartmentTradeRawRepository apartmentTradeRawRepository;

    @Autowired
    private RealEstateGeocodeCacheRepository realEstateGeocodeCacheRepository;

    @Autowired
    private RealEstatePropertyRepository realEstatePropertyRepository;

    @MockitoBean
    private NaverGeocodingClient naverGeocodingClient;

    @AfterEach
    void tearDown() {
        realEstatePropertyRepository.deleteAllInBatch();
        apartmentTradeRawRepository.deleteAllInBatch();
        realEstateGeocodeCacheRepository.deleteAllInBatch();
        housingLegalDongRepository.deleteAllInBatch();
        housingDistrictRepository.deleteAllInBatch();
        housingRegionRepository.deleteAllInBatch();
    }

    @DisplayName("법정동 XML과 실거래 XML을 적재하면 raw 거래를 보존하고 대표 매물은 최신 거래 기준으로 정규화한다")
    @Test
    void importMaster() {
        // given
        given(naverGeocodingClient.geocode(contains("잠실동 35")))
            .willReturn(Optional.of(NaverGeocodingClient.GeocodingResult.of(
                BigDecimal.valueOf(37.5133012),
                BigDecimal.valueOf(127.1029384),
                "서울특별시 송파구 올림픽로 99",
                "서울특별시 송파구 잠실동 35"
            )));
        given(naverGeocodingClient.geocode(contains("신천동 17")))
            .willReturn(Optional.of(NaverGeocodingClient.GeocodingResult.of(
                BigDecimal.valueOf(37.5188123),
                BigDecimal.valueOf(127.0991234),
                "서울특별시 송파구 올림픽로 100",
                "서울특별시 송파구 신천동 17"
            )));

        // when
        realEstateMasterImportService.importMaster(
            RealEstateMasterImportRequest.of(
                LEGAL_DONG_XML,
                List.of(SEOUL_TRADE_XML)
            )
        );

        // then
        assertThat(housingRegionRepository.count()).isEqualTo(2);
        assertThat(housingDistrictRepository.count()).isEqualTo(2);
        assertThat(housingLegalDongRepository.count()).isEqualTo(3);
        assertThat(apartmentTradeRawRepository.count()).isEqualTo(3);
        assertThat(realEstatePropertyRepository.count()).isEqualTo(2);

        RealEstateProperty jamsilEls = realEstatePropertyRepository.findAll().stream()
            .filter(property -> property.getPropertyName().equals("잠실엘스"))
            .findFirst()
            .orElseThrow();

        assertThat(jamsilEls.getRegionCode()).isEqualTo("11");
        assertThat(jamsilEls.getDistrictCode()).isEqualTo("11710");
        assertThat(jamsilEls.getAddress()).contains("서울특별시 송파구 잠실동", "35");
        assertThat(jamsilEls.getBasePrice()).isEqualTo(Money.of(2_300_000_000L));
        assertThat(jamsilEls.getHousingType()).isEqualTo(HousingType.OWNED_APT);
        assertThat(jamsilEls.getLatitude()).isEqualByComparingTo(BigDecimal.valueOf(37.5133012));
        assertThat(jamsilEls.getLongitude()).isEqualByComparingTo(BigDecimal.valueOf(127.1029384));
    }

    @DisplayName("지오코딩이 실패한 거래는 raw와 대표 매물에는 남고 좌표만 비워 둔다")
    @Test
    void importMasterWithGeocodingFailure() {
        // given
        given(naverGeocodingClient.geocode(contains("잠실동 35")))
            .willReturn(Optional.empty());
        given(naverGeocodingClient.geocode(contains("잠실엘스")))
            .willReturn(Optional.empty());

        // when
        realEstateMasterImportService.importMaster(
            RealEstateMasterImportRequest.of(
                LEGAL_DONG_XML,
                List.of(ONE_PROPERTY_TRADE_XML)
            )
        );

        // then
        assertThat(apartmentTradeRawRepository.count()).isEqualTo(1);
        assertThat(realEstatePropertyRepository.count()).isEqualTo(1);

        RealEstateProperty property = realEstatePropertyRepository.findAll().get(0);
        assertThat(property.getLatitude()).isNull();
        assertThat(property.getLongitude()).isNull();
    }

    @DisplayName("같은 법정동 XML과 실거래 XML을 다시 적재해도 마스터와 raw 거래가 중복 저장되지 않는다")
    @Test
    void importMasterIdempotently() {
        // given
        given(naverGeocodingClient.geocode(contains("잠실동 35")))
            .willReturn(Optional.of(NaverGeocodingClient.GeocodingResult.of(
                BigDecimal.valueOf(37.5133012),
                BigDecimal.valueOf(127.1029384),
                "서울특별시 송파구 올림픽로 99",
                "서울특별시 송파구 잠실동 35"
            )));
        given(naverGeocodingClient.geocode(contains("신천동 17")))
            .willReturn(Optional.of(NaverGeocodingClient.GeocodingResult.of(
                BigDecimal.valueOf(37.5188123),
                BigDecimal.valueOf(127.0991234),
                "서울특별시 송파구 올림픽로 100",
                "서울특별시 송파구 신천동 17"
            )));

        RealEstateMasterImportRequest request = RealEstateMasterImportRequest.of(
            LEGAL_DONG_XML,
            List.of(SEOUL_TRADE_XML)
        );

        // when
        realEstateMasterImportService.importMaster(request);
        realEstateMasterImportService.importMaster(request);

        // then
        assertThat(housingRegionRepository.count()).isEqualTo(2);
        assertThat(housingDistrictRepository.count()).isEqualTo(2);
        assertThat(housingLegalDongRepository.count()).isEqualTo(3);
        assertThat(apartmentTradeRawRepository.count()).isEqualTo(3);
        assertThat(realEstatePropertyRepository.count()).isEqualTo(2);
    }

    @DisplayName("실거래의 법정동 이름이 마스터와 매핑되지 않으면 raw에는 남고 대표 매물은 생성되지 않는다")
    @Test
    void importMasterWithUnknownLegalDong() {
        // when
        realEstateMasterImportService.importMaster(
            RealEstateMasterImportRequest.of(
                LEGAL_DONG_XML,
                List.of(UNKNOWN_LEGAL_DONG_TRADE_XML)
            )
        );

        // then
        assertThat(apartmentTradeRawRepository.count()).isEqualTo(1);
        assertThat(realEstatePropertyRepository.count()).isZero();
    }

    @DisplayName("기본 주소 지오코딩이 실패하면 아파트명 기반 fallback query로 대표 매물을 생성한다")
    @Test
    void importMasterWithGeocodingFallback() {
        // given
        given(naverGeocodingClient.geocode(contains("잠실동 35")))
            .willReturn(Optional.empty());
        given(naverGeocodingClient.geocode(contains("잠실엘스")))
            .willReturn(Optional.of(NaverGeocodingClient.GeocodingResult.of(
                BigDecimal.valueOf(37.5133012),
                BigDecimal.valueOf(127.1029384),
                "서울특별시 송파구 올림픽로 99",
                "서울특별시 송파구 잠실동 35"
            )));

        // when
        realEstateMasterImportService.importMaster(
            RealEstateMasterImportRequest.of(
                LEGAL_DONG_XML,
                List.of(ONE_PROPERTY_TRADE_XML)
            )
        );

        // then
        assertThat(apartmentTradeRawRepository.count()).isEqualTo(1);
        assertThat(realEstatePropertyRepository.count()).isEqualTo(1);
    }

    @DisplayName("같은 매물을 다시 적재해도 지오코딩 성공 및 no-result 결과를 캐시해 외부 호출을 반복하지 않는다")
    @Test
    void importMasterWithGeocodingCache() {
        // given
        given(naverGeocodingClient.geocode(contains("잠실동 35")))
            .willReturn(Optional.empty());
        given(naverGeocodingClient.geocode(contains("잠실엘스")))
            .willReturn(Optional.of(NaverGeocodingClient.GeocodingResult.of(
                BigDecimal.valueOf(37.5133012),
                BigDecimal.valueOf(127.1029384),
                "서울특별시 송파구 올림픽로 99",
                "서울특별시 송파구 잠실동 35"
            )));

        RealEstateMasterImportRequest request = RealEstateMasterImportRequest.of(
            LEGAL_DONG_XML,
            List.of(ONE_PROPERTY_TRADE_XML)
        );

        // when
        realEstateMasterImportService.importMaster(request);
        realEstateMasterImportService.importMaster(request);

        // then
        assertThat(realEstateGeocodeCacheRepository.count()).isEqualTo(2);
        assertThat(realEstatePropertyRepository.count()).isEqualTo(1);
        then(naverGeocodingClient).should(times(1)).geocode(contains("잠실동 35"));
        then(naverGeocodingClient).should(times(1)).geocode(contains("잠실엘스"));
    }

    @DisplayName("이미 좌표가 있는 대표 매물은 재실행 지오코딩 실패로 좌표가 사라지지 않는다")
    @Test
    void importMasterKeepsExistingCoordinatesWhenGeocodingFailsOnRerun() {
        // given
        given(naverGeocodingClient.geocode(contains("잠실동 35")))
            .willReturn(Optional.of(NaverGeocodingClient.GeocodingResult.of(
                BigDecimal.valueOf(37.5133012),
                BigDecimal.valueOf(127.1029384),
                "서울특별시 송파구 올림픽로 99",
                "서울특별시 송파구 잠실동 35"
            )));

        RealEstateMasterImportRequest request = RealEstateMasterImportRequest.of(
            LEGAL_DONG_XML,
            List.of(ONE_PROPERTY_TRADE_XML)
        );

        // when
        realEstateMasterImportService.importMaster(request);
        realEstateGeocodeCacheRepository.deleteAllInBatch();
        given(naverGeocodingClient.geocode(contains("잠실동 35")))
            .willThrow(new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID));
        given(naverGeocodingClient.geocode(contains("잠실엘스")))
            .willThrow(new HomerunException(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID));
        realEstateMasterImportService.importMaster(request);

        // then
        RealEstateProperty property = realEstatePropertyRepository.findAll().get(0);
        assertThat(property.getLatitude()).isEqualByComparingTo(BigDecimal.valueOf(37.5133012));
        assertThat(property.getLongitude()).isEqualByComparingTo(BigDecimal.valueOf(127.1029384));
    }

    @DisplayName("실거래 no-data 응답은 무시하고 법정동 마스터만 적재한다")
    @Test
    void importMasterWithNoTradeData() {
        // when
        realEstateMasterImportService.importMaster(
            RealEstateMasterImportRequest.of(
                LEGAL_DONG_XML,
                List.of(APARTMENT_TRADE_NO_DATA_XML)
            )
        );

        // then
        assertThat(housingRegionRepository.count()).isEqualTo(2);
        assertThat(housingDistrictRepository.count()).isEqualTo(2);
        assertThat(housingLegalDongRepository.count()).isEqualTo(3);
        assertThat(apartmentTradeRawRepository.count()).isZero();
        assertThat(realEstatePropertyRepository.count()).isZero();
    }

    private static final String LEGAL_DONG_XML = """
        <StanReginCd>
          <head>
            <totalCount>5</totalCount>
            <numOfRows>5</numOfRows>
            <pageNo>1</pageNo>
            <type>XML</type>
            <RESULT>
              <resultCode>INFO-0</resultCode>
              <resultMsg>NOMAL SERVICE</resultMsg>
            </RESULT>
          </head>
          <row>
            <region_cd>1171000000</region_cd>
            <sido_cd>11</sido_cd>
            <sgg_cd>710</sgg_cd>
            <umd_cd>000</umd_cd>
            <ri_cd>00</ri_cd>
            <locatadd_nm>서울특별시 송파구</locatadd_nm>
            <locat_order>24</locat_order>
            <locathigh_cd>1100000000</locathigh_cd>
            <locallow_nm>송파구</locallow_nm>
          </row>
          <row>
            <region_cd>1171010100</region_cd>
            <sido_cd>11</sido_cd>
            <sgg_cd>710</sgg_cd>
            <umd_cd>101</umd_cd>
            <ri_cd>00</ri_cd>
            <locatadd_nm>서울특별시 송파구 잠실동</locatadd_nm>
            <locat_order>1</locat_order>
            <locathigh_cd>1171000000</locathigh_cd>
            <locallow_nm>잠실동</locallow_nm>
          </row>
          <row>
            <region_cd>1171010200</region_cd>
            <sido_cd>11</sido_cd>
            <sgg_cd>710</sgg_cd>
            <umd_cd>102</umd_cd>
            <ri_cd>00</ri_cd>
            <locatadd_nm>서울특별시 송파구 신천동</locatadd_nm>
            <locat_order>2</locat_order>
            <locathigh_cd>1171000000</locathigh_cd>
            <locallow_nm>신천동</locallow_nm>
          </row>
          <row>
            <region_cd>2417011100</region_cd>
            <sido_cd>24</sido_cd>
            <sgg_cd>170</sgg_cd>
            <umd_cd>111</umd_cd>
            <ri_cd>00</ri_cd>
            <locatadd_nm>광주광역시 북구 운암동</locatadd_nm>
            <locat_order>1</locat_order>
            <locathigh_cd>2417000000</locathigh_cd>
            <locallow_nm>운암동</locallow_nm>
          </row>
          <row>
            <region_cd>2629000000</region_cd>
            <sido_cd>26</sido_cd>
            <sgg_cd>290</sgg_cd>
            <umd_cd>000</umd_cd>
            <ri_cd>00</ri_cd>
            <locatadd_nm>부산광역시 남구</locatadd_nm>
            <locat_order>99</locat_order>
            <locathigh_cd>2600000000</locathigh_cd>
            <locallow_nm>남구</locallow_nm>
          </row>
        </StanReginCd>
        """;

    private static final String SEOUL_TRADE_XML = """
        <response>
          <header>
            <resultCode>000</resultCode>
            <resultMsg>OK</resultMsg>
          </header>
          <body>
            <items>
              <item>
                <aptNm>잠실엘스</aptNm>
                <buildYear>2008</buildYear>
                <dealAmount>200,000</dealAmount>
                <dealDay>5</dealDay>
                <dealMonth>1</dealMonth>
                <dealYear>2024</dealYear>
                <excluUseAr>84.88</excluUseAr>
                <floor>10</floor>
                <jibun>35</jibun>
                <landLeaseholdGbn>N</landLeaseholdGbn>
                <sggCd>11710</sggCd>
                <umdNm>잠실동</umdNm>
              </item>
              <item>
                <aptNm>잠실엘스</aptNm>
                <buildYear>2008</buildYear>
                <dealAmount>230,000</dealAmount>
                <dealDay>15</dealDay>
                <dealMonth>12</dealMonth>
                <dealYear>2024</dealYear>
                <excluUseAr>84.88</excluUseAr>
                <floor>18</floor>
                <jibun>35</jibun>
                <landLeaseholdGbn>N</landLeaseholdGbn>
                <sggCd>11710</sggCd>
                <umdNm>잠실동</umdNm>
              </item>
              <item>
                <aptNm>파크리오</aptNm>
                <buildYear>2008</buildYear>
                <dealAmount>180,000</dealAmount>
                <dealDay>2</dealDay>
                <dealMonth>6</dealMonth>
                <dealYear>2024</dealYear>
                <excluUseAr>84.99</excluUseAr>
                <floor>14</floor>
                <jibun>17</jibun>
                <landLeaseholdGbn>N</landLeaseholdGbn>
                <sggCd>11710</sggCd>
                <umdNm>신천동</umdNm>
              </item>
            </items>
            <numOfRows>3</numOfRows>
            <pageNo>1</pageNo>
            <totalCount>3</totalCount>
          </body>
        </response>
        """;

    private static final String ONE_PROPERTY_TRADE_XML = """
        <response>
          <header>
            <resultCode>000</resultCode>
            <resultMsg>OK</resultMsg>
          </header>
          <body>
            <items>
              <item>
                <aptNm>잠실엘스</aptNm>
                <buildYear>2008</buildYear>
                <dealAmount>200,000</dealAmount>
                <dealDay>5</dealDay>
                <dealMonth>1</dealMonth>
                <dealYear>2024</dealYear>
                <excluUseAr>84.88</excluUseAr>
                <floor>10</floor>
                <jibun>35</jibun>
                <landLeaseholdGbn>N</landLeaseholdGbn>
                <sggCd>11710</sggCd>
                <umdNm>잠실동</umdNm>
              </item>
            </items>
            <numOfRows>1</numOfRows>
            <pageNo>1</pageNo>
            <totalCount>1</totalCount>
          </body>
        </response>
        """;

    private static final String UNKNOWN_LEGAL_DONG_TRADE_XML = """
        <response>
          <header>
            <resultCode>000</resultCode>
            <resultMsg>OK</resultMsg>
          </header>
          <body>
            <items>
              <item>
                <aptNm>미등록아파트</aptNm>
                <buildYear>2001</buildYear>
                <dealAmount>95,000</dealAmount>
                <dealDay>3</dealDay>
                <dealMonth>3</dealMonth>
                <dealYear>2024</dealYear>
                <excluUseAr>59.92</excluUseAr>
                <floor>7</floor>
                <jibun>999</jibun>
                <landLeaseholdGbn>N</landLeaseholdGbn>
                <sggCd>11710</sggCd>
                <umdNm>없는동</umdNm>
              </item>
            </items>
            <numOfRows>1</numOfRows>
            <pageNo>1</pageNo>
            <totalCount>1</totalCount>
          </body>
        </response>
        """;

    private static final String APARTMENT_TRADE_NO_DATA_XML = """
        <RESULT>
          <resultCode>INFO-3</resultCode>
          <resultMsg>데이터없음 에러</resultMsg>
        </RESULT>
        """;
}
