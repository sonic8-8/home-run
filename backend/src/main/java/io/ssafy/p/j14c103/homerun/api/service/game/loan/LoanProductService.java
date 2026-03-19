package io.ssafy.p.j14c103.homerun.api.service.game.loan;

import io.ssafy.p.j14c103.homerun.api.service.game.loan.response.LoanProductDetailResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.loan.response.LoanProductResponse;
import io.ssafy.p.j14c103.homerun.client.fss.FssLoanClient;
import io.ssafy.p.j14c103.homerun.client.fss.FssLoanResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 대출 상품 조회 서비스 (금감원 FSS 기반).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoanProductService {

    private static final Map<String, String> BANK_LOGO_MAP = Map.ofEntries(
            Map.entry("우리은행", "/images/banks/woori.png"),
            Map.entry("한국스탠다드차타드은행", "/images/banks/sc.png"),
            Map.entry("아이엠뱅크", "/images/banks/im.png"),
            Map.entry("부산은행", "/images/banks/busan.png"),
            Map.entry("광주은행", "/images/banks/gwangju.png"),
            Map.entry("제주은행", "/images/banks/jeju.png"),
            Map.entry("전북은행", "/images/banks/jeonbuk.png"),
            Map.entry("경남은행", "/images/banks/gyeongnam.png"),
            Map.entry("중소기업은행", "/images/banks/ibk.png"),
            Map.entry("한국산업은행", "/images/banks/kdb.png"),
            Map.entry("국민은행", "/images/banks/kb.png"),
            Map.entry("신한은행", "/images/banks/shinhan.png"),
            Map.entry("농협은행주식회사", "/images/banks/nh.png"),
            Map.entry("주식회사 하나은행", "/images/banks/hana.png"),
            Map.entry("주식회사 케이뱅크", "/images/banks/kbank.png"),
            Map.entry("수협은행", "/images/banks/suhyup.png"),
            Map.entry("주식회사 카카오뱅크", "/images/banks/kakao.png")
    );

    private final FssLoanClient fssLoanClient;

    /**
     * 대출 상품 목록 조회.
     *
     * @param category ALL, CREDIT, JEONSE, MORTGAGE
     * @param page     0-based 페이지
     * @param size     페이지 크기
     */
    public List<LoanProductResponse> getProducts(final String category, final int page, final int size) {
        final List<LoanProductResponse> allProducts = fetchAllProducts(category);

        final int start = page * size;
        if (start >= allProducts.size()) {
            return Collections.emptyList();
        }
        final int end = Math.min(start + size, allProducts.size());
        return allProducts.subList(start, end);
    }

    /**
     * 대출 상품 상세 조회.
     */
    public Optional<LoanProductDetailResponse> getProductDetail(final String productId) {
        final List<FssProductData> allData = new ArrayList<>();
        allData.addAll(parseProducts(fssLoanClient.getCreditLoanProducts(), "개인신용대출"));
        allData.addAll(parseProducts(fssLoanClient.getRentHouseLoanProducts(), "전세자금대출"));
        allData.addAll(parseProducts(fssLoanClient.getMortgageLoanProducts(), "주택담보대출"));

        return allData.stream()
                .filter(d -> productId.equals(d.productId))
                .findFirst()
                .map(d -> LoanProductDetailResponse.builder()
                        .productId(d.productId)
                        .bankName(d.bankName)
                        .bankLogoUrl(BANK_LOGO_MAP.getOrDefault(d.bankName, "/images/banks/default.png"))
                        .productName(d.productName)
                        .productType(d.productType)
                        .minRate(d.minRate)
                        .maxRate(d.maxRate)
                        .features(extractFeatures(d))
                        .build());
    }

    /**
     * 상품 금리 조회 (confirm 시 사용).
     *
     * @return 최소 금리. 상품 없으면 기본 3.49%
     */
    public double getProductRate(final String productId) {
        return getProductDetail(productId)
                .map(LoanProductDetailResponse::getMinRate)
                .orElse(3.49);
    }

    /**
     * 상품 유형 조회.
     *
     * @return 상품 유형. 없으면 null
     */
    public String getProductType(final String productId) {
        return getProductDetail(productId)
                .map(LoanProductDetailResponse::getProductType)
                .orElse(null);
    }

    private List<LoanProductResponse> fetchAllProducts(final String category) {
        final List<FssProductData> allData = new ArrayList<>();

        if ("ALL".equals(category) || "CREDIT".equals(category)) {
            allData.addAll(parseProducts(fssLoanClient.getCreditLoanProducts(), "개인신용대출"));
        }
        if ("ALL".equals(category) || "JEONSE".equals(category)) {
            allData.addAll(parseProducts(fssLoanClient.getRentHouseLoanProducts(), "전세자금대출"));
        }
        if ("ALL".equals(category) || "MORTGAGE".equals(category)) {
            allData.addAll(parseProducts(fssLoanClient.getMortgageLoanProducts(), "주택담보대출"));
        }

        return allData.stream()
                .sorted(Comparator.comparingDouble(d -> d.minRate))
                .map(d -> LoanProductResponse.builder()
                        .productId(d.productId)
                        .bankName(d.bankName)
                        .bankLogoUrl(BANK_LOGO_MAP.getOrDefault(d.bankName, "/images/banks/default.png"))
                        .productName(d.productName)
                        .productType(d.productType)
                        .minRate(d.minRate)
                        .maxRate(d.maxRate)
                        .build())
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private List<FssProductData> parseProducts(final FssLoanResponse fssResponse, final String productType) {
        if (fssResponse.isEmpty()) {
            return Collections.emptyList();
        }

        final Map<String, RateRange> rateMap = buildRateMap(fssResponse.getOptionList());
        final List<FssProductData> results = new ArrayList<>();

        for (final Map<String, Object> base : fssResponse.getBaseList()) {
            final String finCoNo = (String) base.get("fin_co_no");
            final String finPrdtCd = (String) base.get("fin_prdt_cd");
            final String key = finCoNo + "|" + finPrdtCd;

            final RateRange rateRange = rateMap.get(key);
            if (rateRange == null) {
                continue;
            }

            final String bankName = (String) base.get("kor_co_nm");
            final String productName = sanitize((String) base.get("fin_prdt_nm"));

            results.add(new FssProductData(
                    finPrdtCd, bankName, productName, productType,
                    rateRange.min, rateRange.max, base));
        }

        return results;
    }

    private Map<String, RateRange> buildRateMap(final List<Map<String, Object>> optionList) {
        final Map<String, RateRange> rateMap = new HashMap<>();

        for (final Map<String, Object> option : optionList) {
            final String key = option.get("fin_co_no") + "|" + option.get("fin_prdt_cd");
            final Double min = toDouble(option.get("lend_rate_min"));
            final Double max = toDouble(option.get("lend_rate_max"));
            if (min == null || max == null) {
                continue;
            }
            rateMap.merge(key, new RateRange(min, max), RateRange::merge);
        }

        return rateMap;
    }

    private List<String> extractFeatures(final FssProductData data) {
        final List<String> features = new ArrayList<>();
        final Map<String, Object> base = data.rawBase;

        addFeature(features, base, "join_way", "가입 방법");
        addFeature(features, base, "loan_inci_expn", "부대비용");
        addFeature(features, base, "erly_rpay_fee", "중도상환수수료");
        addFeature(features, base, "dly_rate", "연체이자율");

        return features;
    }

    private void addFeature(final List<String> features, final Map<String, Object> base,
                            final String key, final String label) {
        final Object value = base.get(key);
        if (value != null && !value.toString().isBlank()) {
            features.add(label + ": " + sanitize(value.toString()));
        }
    }

    private String sanitize(final String value) {
        return value == null ? "" : value.replace("\n", " ").trim();
    }

    private Double toDouble(final Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (final NumberFormatException e) {
            return null;
        }
    }

    private record FssProductData(
            String productId, String bankName, String productName,
            String productType, double minRate, double maxRate,
            Map<String, Object> rawBase
    ) {}

    private static class RateRange {
        double min;
        double max;

        RateRange(double min, double max) {
            this.min = min;
            this.max = max;
        }

        RateRange merge(RateRange other) {
            this.min = Math.min(this.min, other.min);
            this.max = Math.max(this.max, other.max);
            return this;
        }
    }
}
