package io.ssafy.p.j14c103.homerun.client.publicdata.realestate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ApartmentTradeResponseParserTest {

    private final ApartmentTradeResponseParser parser = new ApartmentTradeResponseParser();

    @DisplayName("아파트 실거래가 XML에서 거래 금액과 거래 식별 정보를 파싱한다")
    @Test
    void parseItems() {
        // given
        String xml = """
            <response>
              <script/>
              <header>
                <resultCode>000</resultCode>
                <resultMsg>OK</resultMsg>
              </header>
              <body>
                <items>
                  <item>
                    <aptDong> </aptDong>
                    <aptNm>창신쌍용1</aptNm>
                    <buildYear>1992</buildYear>
                    <buyerGbn> </buyerGbn>
                    <cdealDay> </cdealDay>
                    <cdealType> </cdealType>
                    <dealAmount>37,300</dealAmount>
                    <dealDay>23</dealDay>
                    <dealMonth>12</dealMonth>
                    <dealYear>2024</dealYear>
                    <dealingGbn> </dealingGbn>
                    <estateAgentSggNm> </estateAgentSggNm>
                    <excluUseAr>79.87</excluUseAr>
                    <floor>9</floor>
                    <jibun>702</jibun>
                    <landLeaseholdGbn>N</landLeaseholdGbn>
                    <rgstDate> </rgstDate>
                    <sggCd>11110</sggCd>
                    <slerGbn> </slerGbn>
                    <umdNm>창신동</umdNm>
                  </item>
                  <item>
                    <aptDong> </aptDong>
                    <aptNm>창신쌍용1</aptNm>
                    <buildYear>1992</buildYear>
                    <buyerGbn> </buyerGbn>
                    <cdealDay> </cdealDay>
                    <cdealType> </cdealType>
                    <dealAmount>42,800</dealAmount>
                    <dealDay>22</dealDay>
                    <dealMonth>12</dealMonth>
                    <dealYear>2024</dealYear>
                    <dealingGbn> </dealingGbn>
                    <estateAgentSggNm> </estateAgentSggNm>
                    <excluUseAr>106.62</excluUseAr>
                    <floor>12</floor>
                    <jibun>702</jibun>
                    <landLeaseholdGbn>N</landLeaseholdGbn>
                    <rgstDate> </rgstDate>
                    <sggCd>11110</sggCd>
                    <slerGbn> </slerGbn>
                    <umdNm>창신동</umdNm>
                  </item>
                </items>
                <numOfRows>10</numOfRows>
                <pageNo>1</pageNo>
                <totalCount>49</totalCount>
              </body>
            </response>
            """;

        // when
        List<ApartmentTradeResponseParser.ApartmentTradeRow> rows = parser.parse(xml);

        // then
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).districtCode()).isEqualTo("11110");
        assertThat(rows.get(0).legalDongName()).isEqualTo("창신동");
        assertThat(rows.get(0).apartmentName()).isEqualTo("창신쌍용1");
        assertThat(rows.get(0).jibun()).isEqualTo("702");
        assertThat(rows.get(0).dealDate()).isEqualTo(LocalDate.of(2024, 12, 23));
        assertThat(rows.get(0).dealAmount()).isEqualTo(Money.of(373_000_000L));
        assertThat(rows.get(0).exclusiveArea()).isEqualByComparingTo(BigDecimal.valueOf(79.87));
        assertThat(rows.get(0).floor()).isEqualTo(9);
        assertThat(rows.get(0).buildYear()).isEqualTo(1992);

        assertThat(rows.get(1).dealAmount()).isEqualTo(Money.of(428_000_000L));
        assertThat(rows.get(1).exclusiveArea()).isEqualByComparingTo(BigDecimal.valueOf(106.62));
        assertThat(rows.get(1).landLeasehold()).isFalse();
    }

    @DisplayName("아파트 실거래가 JSON 응답도 동일한 거래 정보로 파싱한다")
    @Test
    void parseItemsFromJson() {
        // given
        String json = """
            {
              "response": {
                "header": {
                  "resultCode": "000",
                  "resultMsg": "OK"
                },
                "body": {
                  "items": {
                    "item": [
                      {
                        "aptDong": 102,
                        "aptNm": "롯데캐슬골드",
                        "buildYear": 2005,
                        "dealAmount": "260,000",
                        "dealDay": "11",
                        "dealMonth": "03",
                        "dealYear": "2026",
                        "excluUseAr": "84.91",
                        "floor": "20",
                        "jibun": "29",
                        "landLeaseholdGbn": "N",
                        "sggCd": "11710",
                        "umdNm": "잠실동"
                      }
                    ]
                  },
                  "numOfRows": 10,
                  "pageNo": 1,
                  "totalCount": 1
                }
              }
            }
            """;

        // when
        List<ApartmentTradeResponseParser.ApartmentTradeRow> rows = parser.parse(json);

        // then
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).districtCode()).isEqualTo("11710");
        assertThat(rows.get(0).legalDongName()).isEqualTo("잠실동");
        assertThat(rows.get(0).apartmentName()).isEqualTo("롯데캐슬골드");
        assertThat(rows.get(0).dealDate()).isEqualTo(LocalDate.of(2026, 3, 11));
        assertThat(rows.get(0).dealAmount()).isEqualTo(Money.of(2_600_000_000L));
        assertThat(rows.get(0).exclusiveArea()).isEqualByComparingTo(BigDecimal.valueOf(84.91));
    }

    @DisplayName("아파트 실거래가 no-data 응답이면 빈 목록을 반환한다")
    @Test
    void parseItemsWithNoDataResultCode() {
        // given
        String xml = """
            <RESULT>
              <resultCode>INFO-3</resultCode>
              <resultMsg>데이터없음 에러</resultMsg>
            </RESULT>
            """;

        // when
        List<ApartmentTradeResponseParser.ApartmentTradeRow> rows = parser.parse(xml);

        // then
        assertThat(rows).isEmpty();
    }

    @DisplayName("아파트 실거래가 XML 앞에 BOM이 있어도 정상 파싱한다")
    @Test
    void parseItemsWithBom() {
        // given
        String xml = "\uFEFF<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + """
            <response>
              <script/>
              <header>
                <resultCode>000</resultCode>
                <resultMsg>OK</resultMsg>
              </header>
              <body>
                <items>
                  <item>
                    <aptNm>창신쌍용1</aptNm>
                    <buildYear>1992</buildYear>
                    <dealAmount>37,300</dealAmount>
                    <dealDay>23</dealDay>
                    <dealMonth>12</dealMonth>
                    <dealYear>2024</dealYear>
                    <excluUseAr>79.87</excluUseAr>
                    <floor>9</floor>
                    <jibun>702</jibun>
                    <landLeaseholdGbn>N</landLeaseholdGbn>
                    <sggCd>11110</sggCd>
                    <umdNm>창신동</umdNm>
                  </item>
                </items>
              </body>
            </response>
            """;

        // when
        List<ApartmentTradeResponseParser.ApartmentTradeRow> rows = parser.parse(xml);

        // then
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).dealAmount()).isEqualTo(Money.of(373_000_000L));
    }

    @DisplayName("아파트 실거래가 응답 resultCode가 성공이 아니면 외부 응답 예외를 던진다")
    @Test
    void parseItemsWithFailureResultCode() {
        // given
        String xml = """
            <response>
              <header>
                <resultCode>500</resultCode>
                <resultMsg>SERVICE ERROR</resultMsg>
              </header>
            </response>
            """;

        // when & then
        assertThatThrownBy(() -> parser.parse(xml))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
    }

    @DisplayName("아파트 실거래가 XML 문법이 깨지면 직렬화 예외를 던진다")
    @Test
    void parseItemsWithMalformedXml() {
        // given
        String xml = "<response><header><resultCode>000</resultCode></header><body><items>";

        // when & then
        assertThatThrownBy(() -> parser.parse(xml))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.GLOBAL_SERIALIZATION_ERROR);
    }
}
