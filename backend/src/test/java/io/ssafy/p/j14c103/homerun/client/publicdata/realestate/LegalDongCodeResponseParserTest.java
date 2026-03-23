package io.ssafy.p.j14c103.homerun.client.publicdata.realestate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LegalDongCodeResponseParserTest {

    private final LegalDongCodeResponseParser parser = new LegalDongCodeResponseParser();

    @DisplayName("법정동 코드 XML에서 시도, 시군구, 법정동 계층 정보를 파싱한다")
    @Test
    void parseRows() {
        // given
        String xml = """
            <StanReginCd>
              <script/>
              <head>
                <totalCount>3</totalCount>
                <numOfRows>3</numOfRows>
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
                <locatjumin_cd>1171000000</locatjumin_cd>
                <locatjijuk_cd>1171000000</locatjijuk_cd>
                <locatadd_nm>서울특별시 송파구</locatadd_nm>
                <locat_order>24</locat_order>
                <locat_rm/>
                <locathigh_cd>1100000000</locathigh_cd>
                <locallow_nm>송파구</locallow_nm>
                <adpt_de/>
              </row>
              <row>
                <region_cd>1171010100</region_cd>
                <sido_cd>11</sido_cd>
                <sgg_cd>710</sgg_cd>
                <umd_cd>101</umd_cd>
                <ri_cd>00</ri_cd>
                <locatjumin_cd>1171010100</locatjumin_cd>
                <locatjijuk_cd>1171010100</locatjijuk_cd>
                <locatadd_nm>서울특별시 송파구 잠실동</locatadd_nm>
                <locat_order>1</locat_order>
                <locat_rm/>
                <locathigh_cd>1171000000</locathigh_cd>
                <locallow_nm>잠실동</locallow_nm>
                <adpt_de/>
              </row>
              <row>
                <region_cd>1171010200</region_cd>
                <sido_cd>11</sido_cd>
                <sgg_cd>710</sgg_cd>
                <umd_cd>102</umd_cd>
                <ri_cd>00</ri_cd>
                <locatjumin_cd>1171010200</locatjumin_cd>
                <locatjijuk_cd>1171010200</locatjijuk_cd>
                <locatadd_nm>서울특별시 송파구 신천동</locatadd_nm>
                <locat_order>2</locat_order>
                <locat_rm/>
                <locathigh_cd>1171000000</locathigh_cd>
                <locallow_nm>신천동</locallow_nm>
                <adpt_de/>
              </row>
            </StanReginCd>
            """;

        // when
        List<LegalDongCodeResponseParser.LegalDongCodeRow> rows = parser.parse(xml);

        // then
        assertThat(rows).hasSize(3);
        assertThat(rows.get(0).regionCode()).isEqualTo("11");
        assertThat(rows.get(0).districtCode()).isEqualTo("11710");
        assertThat(rows.get(0).legalDongCode()).isEqualTo("1171000000");
        assertThat(rows.get(0).parentLegalDongCode()).isEqualTo("1100000000");
        assertThat(rows.get(0).districtLevel()).isTrue();
        assertThat(rows.get(0).legalDongName()).isEqualTo("송파구");
        assertThat(rows.get(0).fullAddressName()).isEqualTo("서울특별시 송파구");

        assertThat(rows.get(1).regionCode()).isEqualTo("11");
        assertThat(rows.get(1).districtCode()).isEqualTo("11710");
        assertThat(rows.get(1).legalDongCode()).isEqualTo("1171010100");
        assertThat(rows.get(1).parentLegalDongCode()).isEqualTo("1171000000");
        assertThat(rows.get(1).districtLevel()).isFalse();
        assertThat(rows.get(1).legalDongName()).isEqualTo("잠실동");
        assertThat(rows.get(1).fullAddressName()).isEqualTo("서울특별시 송파구 잠실동");
    }

    @DisplayName("법정동 코드 no-data 응답이면 빈 목록을 반환한다")
    @Test
    void parseRowsWithNoDataResultCode() {
        // given
        String xml = """
            <RESULT>
              <resultCode>INFO-3</resultCode>
              <resultMsg>데이터없음 에러</resultMsg>
            </RESULT>
            """;

        // when
        List<LegalDongCodeResponseParser.LegalDongCodeRow> rows = parser.parse(xml);

        // then
        assertThat(rows).isEmpty();
    }

    @DisplayName("법정동 코드 응답 resultCode가 성공이 아니면 외부 응답 예외를 던진다")
    @Test
    void parseRowsWithFailureResultCode() {
        // given
        String xml = """
            <StanReginCd>
              <head>
                <RESULT>
                  <resultCode>ERROR-1</resultCode>
                  <resultMsg>INVALID REQUEST</resultMsg>
                </RESULT>
              </head>
            </StanReginCd>
            """;

        // when & then
        assertThatThrownBy(() -> parser.parse(xml))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.GLOBAL_EXTERNAL_RESPONSE_INVALID);
    }

    @DisplayName("법정동 코드 XML 문법이 깨지면 직렬화 예외를 던진다")
    @Test
    void parseRowsWithMalformedXml() {
        // given
        String xml = "<StanReginCd><head><RESULT><resultCode>INFO-0</resultCode></RESULT>";

        // when & then
        assertThatThrownBy(() -> parser.parse(xml))
            .isInstanceOf(HomerunException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.GLOBAL_SERIALIZATION_ERROR);
    }
}
