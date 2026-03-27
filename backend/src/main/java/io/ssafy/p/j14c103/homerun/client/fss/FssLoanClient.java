package io.ssafy.p.j14c103.homerun.client.fss;

import io.ssafy.p.j14c103.homerun.config.FssApiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.*;

@Slf4j
@Component
public class FssLoanClient {

    private static final String TOP_FIN_GRP_NO_BANK = "020000";

    private final RestClient fssRestClient;
    private final FssApiProperties properties;

    public FssLoanClient(
        @Qualifier("fssRestClient") final RestClient fssRestClient,
        final FssApiProperties properties
    ) {
        this.fssRestClient = fssRestClient;
        this.properties = properties;
    }

    /**
     * 개인신용대출 상품 조회
     */
    public FssLoanResponse getCreditLoanProducts() {
        return fetchProducts("creditLoanProductsSearch", "개인신용대출");
    }

    /**
     * 전세자금대출 상품 조회
     */
    public FssLoanResponse getRentHouseLoanProducts() {
        return fetchProducts("rentHouseLoanProductsSearch", "전세자금대출");
    }

    /**
     * 주택담보대출 상품 조회
     */
    public FssLoanResponse getMortgageLoanProducts() {
        return fetchProducts("mortgageLoanProductsSearch", "주택담보대출");
    }

    @SuppressWarnings("unchecked")
    private FssLoanResponse fetchProducts(final String apiName, final String productType) {
        try {
            final Map<String, Object> response = (Map<String, Object>) fssRestClient.get()
                .uri(uriBuilder -> uriBuilder
                    .pathSegment(apiName + ".json")
                    .queryParam("auth", properties.getAuthKey())
                    .queryParam("topFinGrpNo", TOP_FIN_GRP_NO_BANK)
                    .queryParam("pageNo", 1)
                    .build())
                .retrieve()
                .body(Map.class);
            if (response == null) {
                log.warn("금감원 {} API 응답이 없습니다.", productType);
                return FssLoanResponse.empty();
            }

            final Map<String, Object> result = (Map<String, Object>) response.get("result");
            if (result == null) {
                log.warn("금감원 {} API result가 없습니다.", productType);
                return FssLoanResponse.empty();
            }

            final String errCd = (String) result.get("err_cd");
            if (!"000".equals(errCd)) {
                log.warn("금감원 {} API 에러: {} - {}", productType, errCd, result.get("err_msg"));
                return FssLoanResponse.empty();
            }

            final List<Map<String, Object>> baseList =
                    (List<Map<String, Object>>) result.getOrDefault("baseList", Collections.emptyList());
            final List<Map<String, Object>> optionList =
                    (List<Map<String, Object>>) result.getOrDefault("optionList", Collections.emptyList());

            return FssLoanResponse.of(baseList, optionList);
        } catch (final Exception e) {
            log.error("금감원 {} API 호출 실패", productType, e);
            return FssLoanResponse.empty();
        }
    }
}
