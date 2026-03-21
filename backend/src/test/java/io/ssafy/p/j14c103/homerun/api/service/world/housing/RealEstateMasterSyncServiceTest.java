package io.ssafy.p.j14c103.homerun.api.service.world.housing;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoInteractions;

import io.ssafy.p.j14c103.homerun.api.service.world.housing.request.RealEstateMasterSyncServiceRequest;
import io.ssafy.p.j14c103.homerun.client.publicdata.realestate.ApartmentTradeClient;
import io.ssafy.p.j14c103.homerun.client.publicdata.realestate.LegalDongCodeClient;
import io.ssafy.p.j14c103.homerun.client.publicdata.realestate.LegalDongCodeXmlParser;
import io.ssafy.p.j14c103.homerun.config.PublicDataApiProperties;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RealEstateMasterSyncServiceTest {

    @Mock
    private LegalDongCodeClient legalDongCodeClient;

    @Mock
    private ApartmentTradeClient apartmentTradeClient;

    @Mock
    private RealEstateMasterImportService realEstateMasterImportService;

    private RealEstateMasterSyncService realEstateMasterSyncService;

    @BeforeEach
    void setUp() {
        realEstateMasterSyncService = createSyncService(1000);
    }

    @DisplayName("서울과 광주 요청이면 지역별 법정동을 조회하고 구별 월별 실거래가를 수집한 뒤 적재 서비스에 위임한다")
    @Test
    void sync() {
        // given
        given(legalDongCodeClient.fetch("서울특별시", 1, 1000)).willReturn(SEOUL_LEGAL_DONG_XML);
        given(legalDongCodeClient.fetch("광주광역시", 1, 1000)).willReturn(GWANGJU_LEGAL_DONG_XML);

        given(apartmentTradeClient.fetch("11710", YearMonth.of(2024, 1), 1, 1000))
            .willReturn(APARTMENT_TRADE_XML);
        given(apartmentTradeClient.fetch("11710", YearMonth.of(2024, 2), 1, 1000))
            .willReturn(APARTMENT_TRADE_XML);
        given(apartmentTradeClient.fetch("24170", YearMonth.of(2024, 1), 1, 1000))
            .willReturn(APARTMENT_TRADE_XML);
        given(apartmentTradeClient.fetch("24170", YearMonth.of(2024, 2), 1, 1000))
            .willReturn(APARTMENT_TRADE_XML);

        // when
        realEstateMasterSyncService.sync(
            RealEstateMasterSyncServiceRequest.of(
                List.of("SEOUL", "GWANGJU"),
                YearMonth.of(2024, 1),
                YearMonth.of(2024, 2)
            )
        );

        // then
        then(legalDongCodeClient).should().fetch("서울특별시", 1, 1000);
        then(legalDongCodeClient).should().fetch("광주광역시", 1, 1000);
        then(apartmentTradeClient).should().fetch("11710", YearMonth.of(2024, 1), 1, 1000);
        then(apartmentTradeClient).should().fetch("11710", YearMonth.of(2024, 2), 1, 1000);
        then(apartmentTradeClient).should().fetch("24170", YearMonth.of(2024, 1), 1, 1000);
        then(apartmentTradeClient).should().fetch("24170", YearMonth.of(2024, 2), 1, 1000);
        then(realEstateMasterImportService).should(times(2)).importMaster(any());
    }

    @DisplayName("MVP 범위를 벗어난 지역 코드면 예외를 던지고 동기화를 진행하지 않는다")
    @Test
    void syncWithUnsupportedRegion() {
        // when & then
        assertThatThrownBy(() -> realEstateMasterSyncService.sync(
            RealEstateMasterSyncServiceRequest.of(
                List.of("BUSAN"),
                YearMonth.of(2024, 1),
                YearMonth.of(2024, 1)
            )
        )).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(legalDongCodeClient, apartmentTradeClient, realEstateMasterImportService);
    }

    @DisplayName("법정동과 실거래 응답이 페이지 크기만큼 차면 다음 페이지까지 이어서 수집한다")
    @Test
    void syncAcrossPages() {
        // given
        RealEstateMasterSyncService pageLoopSyncService = createSyncService(1);

        given(legalDongCodeClient.fetch("서울특별시", 1, 1)).willReturn(SEOUL_LEGAL_DONG_PAGE_1_XML);
        given(legalDongCodeClient.fetch("서울특별시", 2, 1)).willReturn(SEOUL_LEGAL_DONG_PAGE_2_XML);
        given(apartmentTradeClient.fetch("11710", YearMonth.of(2024, 1), 1, 1))
            .willReturn(APARTMENT_TRADE_PAGE_1_XML);
        given(apartmentTradeClient.fetch("11710", YearMonth.of(2024, 1), 2, 1))
            .willReturn(APARTMENT_TRADE_PAGE_2_XML);

        // when
        pageLoopSyncService.sync(
            RealEstateMasterSyncServiceRequest.of(
                List.of("SEOUL"),
                YearMonth.of(2024, 1),
                YearMonth.of(2024, 1)
            )
        );

        // then
        then(legalDongCodeClient).should().fetch("서울특별시", 1, 1);
        then(legalDongCodeClient).should().fetch("서울특별시", 2, 1);
        then(apartmentTradeClient).should().fetch("11710", YearMonth.of(2024, 1), 1, 1);
        then(apartmentTradeClient).should().fetch("11710", YearMonth.of(2024, 1), 2, 1);
        then(realEstateMasterImportService).should().importMaster(any());
    }

    private RealEstateMasterSyncService createSyncService(int pageSize) {
        return new RealEstateMasterSyncService(
            legalDongCodeClient,
            apartmentTradeClient,
            new LegalDongCodeXmlParser(),
            realEstateMasterImportService,
            PublicDataApiProperties.of(
                "https://apis.data.go.kr/1741000/StanReginCd",
                "https://apis.data.go.kr/1613000/RTMSDataSvcAptTrade",
                "test-service-key",
                pageSize
            )
        );
    }

    private static final String SEOUL_LEGAL_DONG_XML = """
        <StanReginCd>
          <head>
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
            <locathigh_cd>1171000000</locathigh_cd>
            <locallow_nm>잠실동</locallow_nm>
          </row>
        </StanReginCd>
        """;

    private static final String GWANGJU_LEGAL_DONG_XML = """
        <StanReginCd>
          <head>
            <RESULT>
              <resultCode>INFO-0</resultCode>
              <resultMsg>NOMAL SERVICE</resultMsg>
            </RESULT>
          </head>
          <row>
            <region_cd>2417011100</region_cd>
            <sido_cd>24</sido_cd>
            <sgg_cd>170</sgg_cd>
            <umd_cd>111</umd_cd>
            <ri_cd>00</ri_cd>
            <locatadd_nm>광주광역시 북구 운암동</locatadd_nm>
            <locathigh_cd>2417000000</locathigh_cd>
            <locallow_nm>운암동</locallow_nm>
          </row>
        </StanReginCd>
        """;

    private static final String APARTMENT_TRADE_XML = """
        <response>
          <header>
            <resultCode>000</resultCode>
            <resultMsg>OK</resultMsg>
          </header>
          <body>
            <items>
              <item>
                <aptNm>테스트아파트</aptNm>
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
            </items>
            <numOfRows>1</numOfRows>
            <pageNo>1</pageNo>
            <totalCount>1</totalCount>
          </body>
        </response>
        """;

    private static final String SEOUL_LEGAL_DONG_PAGE_1_XML = """
        <StanReginCd>
          <head>
            <totalCount>2</totalCount>
            <numOfRows>1</numOfRows>
            <pageNo>1</pageNo>
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
            <locathigh_cd>1100000000</locathigh_cd>
            <locallow_nm>송파구</locallow_nm>
          </row>
        </StanReginCd>
        """;

    private static final String SEOUL_LEGAL_DONG_PAGE_2_XML = """
        <StanReginCd>
          <head>
            <totalCount>2</totalCount>
            <numOfRows>1</numOfRows>
            <pageNo>2</pageNo>
            <RESULT>
              <resultCode>INFO-0</resultCode>
              <resultMsg>NOMAL SERVICE</resultMsg>
            </RESULT>
          </head>
          <row>
            <region_cd>1171010100</region_cd>
            <sido_cd>11</sido_cd>
            <sgg_cd>710</sgg_cd>
            <umd_cd>101</umd_cd>
            <ri_cd>00</ri_cd>
            <locatadd_nm>서울특별시 송파구 잠실동</locatadd_nm>
            <locathigh_cd>1171000000</locathigh_cd>
            <locallow_nm>잠실동</locallow_nm>
          </row>
        </StanReginCd>
        """;

    private static final String APARTMENT_TRADE_PAGE_1_XML = """
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
            <totalCount>2</totalCount>
          </body>
        </response>
        """;

    private static final String APARTMENT_TRADE_PAGE_2_XML = """
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
                <dealAmount>210,000</dealAmount>
                <dealDay>15</dealDay>
                <dealMonth>1</dealMonth>
                <dealYear>2024</dealYear>
                <excluUseAr>84.88</excluUseAr>
                <floor>15</floor>
                <jibun>35</jibun>
                <landLeaseholdGbn>N</landLeaseholdGbn>
                <sggCd>11710</sggCd>
                <umdNm>잠실동</umdNm>
              </item>
            </items>
            <numOfRows>1</numOfRows>
            <pageNo>2</pageNo>
            <totalCount>2</totalCount>
          </body>
        </response>
        """;
}
