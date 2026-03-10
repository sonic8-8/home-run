package io.ssafy.p.j14c103.homerun.infrastructure.ssafy;

import io.ssafy.p.j14c103.homerun.config.SsafyApiProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SsafyDemandDepositClient {

    private final RestTemplate restTemplate;
    private final SsafyApiProperties properties;
    private final SsafyApiHeaderGenerator headerGenerator;

    public SsafyDemandDepositClient(
            final RestTemplate restTemplate,
            final SsafyApiProperties properties,
            final SsafyApiHeaderGenerator headerGenerator) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.headerGenerator = headerGenerator;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> inquireAccountList(final String userKey) {
        if (userKey == null || userKey.isBlank()) {
            throw new IllegalArgumentException("userKey는 필수입니다.");
        }

        final String apiName = "inquireDemandDepositAccountList";
        final String url = properties.getBaseUrl() + "/demandDeposit/" + apiName;

        final Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("Header", headerGenerator.generate(apiName, userKey));

        final Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);

        return extractRecList(response);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> inquireTransactionHistory(
            final String userKey,
            final String accountNo,
            final String startDate,
            final String endDate) {
        if (userKey == null || userKey.isBlank()) {
            throw new IllegalArgumentException("userKey는 필수입니다.");
        }
        if (accountNo == null || accountNo.isBlank()) {
            throw new IllegalArgumentException("계좌번호는 필수입니다.");
        }

        final String apiName = "inquireTransactionHistoryList";
        final String url = properties.getBaseUrl() + "/demandDeposit/" + apiName;

        final Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("Header", headerGenerator.generate(apiName, userKey));
        requestBody.put("accountNo", accountNo);
        requestBody.put("startDate", startDate);
        requestBody.put("endDate", endDate);
        requestBody.put("transactionType", "A");
        requestBody.put("orderByType", "ASC");

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
        final Object list = recMap.get("list");
        if (list == null) {
            return Collections.emptyList();
        }
        return (List<Map<String, Object>>) list;
    }
}
