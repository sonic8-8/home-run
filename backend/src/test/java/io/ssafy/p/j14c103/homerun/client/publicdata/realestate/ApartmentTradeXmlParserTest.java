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

class ApartmentTradeXmlParserTest {

    private final ApartmentTradeXmlParser parser = new ApartmentTradeXmlParser();

    @DisplayName("아파트 실거래가 XML에서 거래 금액과 거래 식별 정보를 파싱한다")
    @Test
    void parseItems() {
        // given
        String xml = """
            <response>
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
                  <item>
                    <aptDong> </aptDong>
                    <aptNm>창신쌍용1</aptNm>
                    <buildYear>1992</buildYear>
                    <dealAmount>42,800</dealAmount>
                    <dealDay>22</dealDay>
                    <dealMonth>12</dealMonth>
                    <dealYear>2024</dealYear>
                    <excluUseAr>106.62</excluUseAr>
                    <floor>12</floor>
                    <jibun>702</jibun>
                    <landLeaseholdGbn>N</landLeaseholdGbn>
                    <sggCd>11110</sggCd>
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
        List<ApartmentTradeXmlParser.ApartmentTradeRow> rows = parser.parse(xml);

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

    @DisplayName("아파트 실거래가 응답 resultCode가 성공이 아니면 직렬화 예외를 던진다")
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
            .isEqualTo(ErrorCode.GLOBAL_SERIALIZATION_ERROR);
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
