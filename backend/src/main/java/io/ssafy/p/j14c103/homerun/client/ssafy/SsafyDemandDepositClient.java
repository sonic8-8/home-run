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
public class SsafyDemandDepositClient {

    private final RestTemplate restTemplate;
    private final SsafyApiProperties properties;
    private final SsafyApiHeaderGenerator headerGenerator;

    /**
     * 수시입출금 상품 등록 (앱 관리자용, 1회)
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> createDemandDeposit(final String bankCode, final String accountName) {
        final String apiName = "createDemandDeposit";
        final String url = properties.getBaseUrl() + "/demandDeposit/" + apiName;

        final Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("Header", headerGenerator.generate(apiName, null));
        requestBody.put("bankCode", bankCode);
        requestBody.put("accountName", accountName);
        requestBody.put("accountDescription", "HomeRun 시드머니");

        final Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
        if (response == null) {
            throw new RuntimeException("SSAFY 수시입출금 상품 등록 응답이 없습니다.");
        }
        return (Map<String, Object>) response.get("REC");
    }

    /**
     * 수시입출금 계좌 생성 (유저별)
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> createDemandDepositAccount(final String userKey, final String accountTypeUniqueNo) {
        final String apiName = "createDemandDepositAccount";
        final String url = properties.getBaseUrl() + "/demandDeposit/" + apiName;

        final Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("Header", headerGenerator.generate(apiName, userKey));
        requestBody.put("accountTypeUniqueNo", accountTypeUniqueNo);

        final Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
        if (response == null) {
            throw new RuntimeException("SSAFY 수시입출금 계좌 생성 응답이 없습니다.");
        }
        return (Map<String, Object>) response.get("REC");
    }

    /**
     * 수시입출금 계좌 입금
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> depositAccount(final String userKey, final String accountNo, final long amount) {
        final String apiName = "updateDemandDepositAccountDeposit";
        final String url = properties.getBaseUrl() + "/demandDeposit/" + apiName;

        final Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("Header", headerGenerator.generate(apiName, userKey));
        requestBody.put("accountNo", accountNo);
        requestBody.put("transactionBalance", String.valueOf(amount));
        requestBody.put("transactionSummary", "시드머니 입금");

        final Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);
        if (response == null) {
            throw new RuntimeException("SSAFY 입금 API 응답이 없습니다.");
        }
        return (Map<String, Object>) response.get("REC");
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
    public Map<String, Object> transferAccount(
            final String userKey,
            final String depositAccountNo,
            final String withdrawalAccountNo,
            final long transactionBalance) {
        if (userKey == null || userKey.isBlank()) {
            throw new IllegalArgumentException("userKey는 필수입니다.");
        }
        if (depositAccountNo == null || depositAccountNo.isBlank()) {
            throw new IllegalArgumentException("입금 계좌번호는 필수입니다.");
        }
        if (withdrawalAccountNo == null || withdrawalAccountNo.isBlank()) {
            throw new IllegalArgumentException("출금 계좌번호는 필수입니다.");
        }
        if (transactionBalance <= 0) {
            throw new IllegalArgumentException("이체 금액은 0보다 커야 합니다.");
        }

        final String apiName = "updateDemandDepositAccountTransfer";
        final String url = properties.getBaseUrl() + "/demandDeposit/" + apiName;

        final Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("Header", headerGenerator.generate(apiName, userKey));
        requestBody.put("depositAccountNo", depositAccountNo);
        requestBody.put("depositTransactionSummary", "시드머니 저축");
        requestBody.put("transactionBalance", String.valueOf(transactionBalance));
        requestBody.put("withdrawalAccountNo", withdrawalAccountNo);
        requestBody.put("withdrawalTransactionSummary", "PASS 저축");

        final Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);

        if (response == null) {
            throw new RuntimeException("SSAFY 이체 API 응답이 없습니다.");
        }
        return response;
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
