package io.ssafy.p.j14c103.homerun.api.service.home;

import io.ssafy.p.j14c103.homerun.api.service.home.response.LoanRecommendationItem;
import io.ssafy.p.j14c103.homerun.api.service.home.response.LoanRecommendationResponse;
import io.ssafy.p.j14c103.homerun.client.fss.FssLoanClient;
import io.ssafy.p.j14c103.homerun.client.fss.FssLoanResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanRecommendationService {

    private static final int MAX_RECOMMENDATIONS = 10;

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

    public LoanRecommendationResponse getRecommendations() {
        // 신용대출 + 전세자금대출 + 주택담보대출 모두 조회
        final List<LoanRecommendationItem> creditItems = fetchItems(
                fssLoanClient.getCreditLoanProducts(), "개인신용대출");
        final List<LoanRecommendationItem> rentItems = fetchItems(
                fssLoanClient.getRentHouseLoanProducts(), "전세자금대출");
        final List<LoanRecommendationItem> mortgageItems = fetchItems(
                fssLoanClient.getMortgageLoanProducts(), "주택담보대출");

        final List<LoanRecommendationItem> combined = Stream.of(
                        creditItems.stream(), rentItems.stream(), mortgageItems.stream())
                .flatMap(s -> s)
                .sorted(Comparator.comparingDouble(LoanRecommendationItem::getMinRate))
                .limit(MAX_RECOMMENDATIONS)
                .collect(Collectors.toList());

        return LoanRecommendationResponse.of(combined);
    }

    private List<LoanRecommendationItem> fetchItems(final FssLoanResponse fssResponse, final String productType) {
        if (fssResponse.isEmpty()) {
            return Collections.emptyList();
        }

        final Map<String, RateRange> rateMap = buildRateMap(fssResponse.getOptionList());

        return fssResponse.getBaseList().stream()
                .map(base -> toRecommendationItem(base, rateMap, productType))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
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

    private Optional<LoanRecommendationItem> toRecommendationItem(
            final Map<String, Object> base,
            final Map<String, RateRange> rateMap,
            final String productType) {
        final String finCoNo = (String) base.get("fin_co_no");
        final String finPrdtCd = (String) base.get("fin_prdt_cd");
        final String key = finCoNo + "|" + finPrdtCd;

        final RateRange rateRange = rateMap.get(key);
        if (rateRange == null) {
            return Optional.empty();
        }

        final String bankName = (String) base.get("kor_co_nm");
        final String productName = sanitizeProductName((String) base.get("fin_prdt_nm"));

        return Optional.of(LoanRecommendationItem.builder()
                .productId(finPrdtCd)
                .bankName(bankName)
                .bankLogoUrl(BANK_LOGO_MAP.getOrDefault(bankName, "/images/banks/default.png"))
                .productName(productName)
                .productType(productType)
                .minRate(rateRange.min)
                .maxRate(rateRange.max)
                .build());
    }

    private String sanitizeProductName(final String name) {
        if (name == null) {
            return "";
        }
        return name.replace("\n", " ").trim();
    }

    private Double toDouble(final Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (final NumberFormatException e) {
            return null;
        }
    }

    private static class RateRange {
        double min;
        double max;

        RateRange(final double min, final double max) {
            this.min = min;
            this.max = max;
        }

        RateRange merge(final RateRange other) {
            this.min = Math.min(this.min, other.min);
            this.max = Math.max(this.max, other.max);
            return this;
        }
    }
}
