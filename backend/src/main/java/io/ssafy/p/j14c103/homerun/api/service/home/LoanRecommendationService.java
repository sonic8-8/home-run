package io.ssafy.p.j14c103.homerun.api.service.home;

import io.ssafy.p.j14c103.homerun.api.service.home.credit.CreditScore;
import io.ssafy.p.j14c103.homerun.api.service.home.credit.CreditScoreProvider;
import io.ssafy.p.j14c103.homerun.api.service.home.response.LoanRecommendationItem;
import io.ssafy.p.j14c103.homerun.api.service.home.response.LoanRecommendationResponse;
import io.ssafy.p.j14c103.homerun.client.fss.FssLoanClient;
import io.ssafy.p.j14c103.homerun.client.fss.FssLoanResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanRecommendationService {

    private static final int MAX_PER_CATEGORY = 5;

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
    private final CreditScoreProvider creditScoreProvider;

    /**
     * CSS 기반 카테고리별 대출 추천.
     * 신용점수 영향도 (실제 한국 금융시장 기반):
     * - 개인신용대출: 100% (무담보 → 신용점수가 금리 결정의 핵심)
     * - 전세자금대출:  40% (보증서 담보 → 신용 영향 제한적)
     * - 주택담보대출:  30% (부동산 담보 → 신용 영향 최소)
     */
    private static final double CREDIT_LOAN_CSS_WEIGHT = 1.0;
    private static final double JEONSE_LOAN_CSS_WEIGHT = 0.4;
    private static final double MORTGAGE_LOAN_CSS_WEIGHT = 0.3;

    public LoanRecommendationResponse getRecommendations(final Long userId) {
        // 1. CSS 점수 산출
        CreditScore css = creditScoreProvider.calculate(userId);

        // 2. FSS 상품 조회 + CSS 가중치 적용
        List<LoanRecommendationItem> creditItems = fetchItems(
                fssLoanClient.getCreditLoanProducts(), "개인신용대출", css, CREDIT_LOAN_CSS_WEIGHT);
        List<LoanRecommendationItem> jeonseItems = fetchItems(
                fssLoanClient.getRentHouseLoanProducts(), "전세자금대출", css, JEONSE_LOAN_CSS_WEIGHT);
        List<LoanRecommendationItem> mortgageItems = fetchItems(
                fssLoanClient.getMortgageLoanProducts(), "주택담보대출", css, MORTGAGE_LOAN_CSS_WEIGHT);

        // 3. 카테고리별 정렬 + 상위 N개
        creditItems = limitSorted(creditItems);
        jeonseItems = limitSorted(jeonseItems);
        mortgageItems = limitSorted(mortgageItems);

        // 4. 예상 최저금리 (개인신용대출 기준)
        double estimatedMinRate = creditItems.isEmpty()
                ? 0.0
                : creditItems.get(0).getEstimatedRate();

        return LoanRecommendationResponse.builder()
                .cssScore(css.getScore())
                .cssGrade(css.getGrade())
                .cssGradeLabel(css.getGradeLabel())
                .estimatedMinRate(estimatedMinRate)
                .creditLoans(creditItems)
                .jeonseLoans(jeonseItems)
                .mortgageLoans(mortgageItems)
                .build();
    }

    private List<LoanRecommendationItem> fetchItems(
            final FssLoanResponse fssResponse,
            final String productType,
            final CreditScore css,
            final double cssWeight) {

        if (fssResponse.isEmpty()) {
            return Collections.emptyList();
        }

        final Map<String, RateRange> rateMap = buildRateMap(fssResponse.getOptionList());

        return fssResponse.getBaseList().stream()
                .map(base -> toRecommendationItem(base, rateMap, productType, css, cssWeight))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
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

    private Optional<LoanRecommendationItem> toRecommendationItem(
            final Map<String, Object> base,
            final Map<String, RateRange> rateMap,
            final String productType,
            final CreditScore css,
            final double cssWeight) {

        final String finCoNo = (String) base.get("fin_co_no");
        final String finPrdtCd = (String) base.get("fin_prdt_cd");
        final String key = finCoNo + "|" + finPrdtCd;

        final RateRange rateRange = rateMap.get(key);
        if (rateRange == null) {
            return Optional.empty();
        }

        final String bankName = (String) base.get("kor_co_nm");
        final String productName = sanitizeProductName((String) base.get("fin_prdt_nm"));

        // CSS 기반 예상금리 계산 (가중치 적용)
        // 예: 개인신용=100%, 전세=40%, 주담보=30%
        double estimatedRate;
        double weightedCoefficient = css.rateCoefficient() * cssWeight;
        estimatedRate = rateRange.min + (rateRange.max - rateRange.min) * weightedCoefficient;
        estimatedRate = Math.round(estimatedRate * 100.0) / 100.0;

        return Optional.of(LoanRecommendationItem.builder()
                .productId(finPrdtCd)
                .bankName(bankName)
                .bankLogoUrl(BANK_LOGO_MAP.getOrDefault(bankName, "/images/banks/default.png"))
                .productName(productName)
                .productType(productType)
                .minRate(rateRange.min)
                .maxRate(rateRange.max)
                .estimatedRate(estimatedRate)
                .build());
    }

    private List<LoanRecommendationItem> limitSorted(List<LoanRecommendationItem> items) {
        return items.stream()
                .sorted(Comparator.comparingDouble(LoanRecommendationItem::getEstimatedRate))
                .limit(MAX_PER_CATEGORY)
                .collect(Collectors.toList());
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
