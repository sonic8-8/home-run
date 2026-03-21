package io.ssafy.p.j14c103.homerun.client.publicdata.realestate;

import io.ssafy.p.j14c103.homerun.config.PublicDataApiProperties;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class ApartmentTradeClient {

    private final RestTemplate restTemplate;
    private final PublicDataApiProperties properties;

    public String fetch(String districtCode, YearMonth dealYearMonth, int pageNo, int numOfRows) {
        String url = UriComponentsBuilder
            .fromHttpUrl(properties.getApartmentTradeBaseUrl())
            .path("/getRTMSDataSvcAptTrade")
            .queryParam("serviceKey", properties.getServiceKey())
            .queryParam("LAWD_CD", districtCode)
            .queryParam("DEAL_YMD", formatYearMonth(dealYearMonth))
            .queryParam("pageNo", pageNo)
            .queryParam("numOfRows", numOfRows)
            .toUriString();

        String response = restTemplate.getForObject(url, String.class);
        if (response == null) {
            throw new IllegalStateException("아파트 실거래가 API 응답이 없습니다.");
        }
        return response;
    }

    private String formatYearMonth(YearMonth dealYearMonth) {
        return String.format("%04d%02d", dealYearMonth.getYear(), dealYearMonth.getMonthValue());
    }
}
