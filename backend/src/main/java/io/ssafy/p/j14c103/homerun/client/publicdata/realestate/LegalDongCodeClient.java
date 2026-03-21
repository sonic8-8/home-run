package io.ssafy.p.j14c103.homerun.client.publicdata.realestate;

import io.ssafy.p.j14c103.homerun.config.PublicDataApiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class LegalDongCodeClient {

    private final RestTemplate restTemplate;
    private final PublicDataApiProperties properties;

    public String fetch(String regionAddress, int pageNo, int numOfRows) {
        String url = UriComponentsBuilder
            .fromHttpUrl(properties.getLegalDongBaseUrl())
            .path("/getStanReginCdList")
            .queryParam("ServiceKey", properties.getServiceKey())
            .queryParam("pageNo", pageNo)
            .queryParam("numOfRows", numOfRows)
            .queryParam("type", "xml")
            .queryParam("locatadd_nm", regionAddress)
            .toUriString();

        String response = restTemplate.getForObject(url, String.class);
        if (response == null) {
            throw new IllegalStateException("법정동 코드 API 응답이 없습니다.");
        }
        return response;
    }
}
