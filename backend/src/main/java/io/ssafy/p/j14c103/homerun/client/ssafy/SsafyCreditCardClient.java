package io.ssafy.p.j14c103.homerun.client.ssafy;

import io.ssafy.p.j14c103.homerun.config.SsafyApiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SsafyCreditCardClient {

    private final RestTemplate restTemplate;
    private final SsafyApiProperties properties;
    private final SsafyApiHeaderGenerator headerGenerator;

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> inquireSignUpCreditCardList(final String userKey) {
        if (userKey == null || userKey.isBlank()) {
            throw new IllegalArgumentException("userKey는 필수입니다.");
        }

        final String apiName = "inquireSignUpCreditCardList";
        final String url = properties.getBaseUrl() + "/creditCard/" + apiName;

        final Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("Header", headerGenerator.generate(apiName, userKey));

        final Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);

        return extractRecList(response);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> inquireCreditCardTransactionList(
            final String userKey,
            final String cardNo,
            final String cvc,
            final String startDate,
            final String endDate) {
        if (userKey == null || userKey.isBlank()) {
            throw new IllegalArgumentException("userKey는 필수입니다.");
        }
        if (cardNo == null || cardNo.isBlank()) {
            throw new IllegalArgumentException("카드번호는 필수입니다.");
        }

        final String apiName = "inquireCreditCardTransactionList";
        final String url = properties.getBaseUrl() + "/creditCard/" + apiName;

        final Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("Header", headerGenerator.generate(apiName, userKey));
        requestBody.put("cardNo", cardNo);
        requestBody.put("cvc", cvc);
        requestBody.put("startDate", startDate);
        requestBody.put("endDate", endDate);

        final Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);

        return extractTransactionList(response);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractRecList(final Map<String, Object> response) {
        if (response == null) {
            return Collections.emptyList();
        }
        final Object rec = response.get("REC");
        if (rec == null) {
            return Collections.emptyList();
        }
        if (rec instanceof List) {
            return (List<Map<String, Object>>) rec;
        }
        return List.of((Map<String, Object>) rec);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractTransactionList(final Map<String, Object> response) {
        if (response == null) {
            return Collections.emptyList();
        }
        final Object rec = response.get("REC");
        if (rec == null) {
            return Collections.emptyList();
        }
        final Map<String, Object> recMap = (Map<String, Object>) rec;
        final Object list = recMap.get("transactionList");
        if (list == null) {
            return Collections.emptyList();
        }
        return (List<Map<String, Object>>) list;
    }
}
