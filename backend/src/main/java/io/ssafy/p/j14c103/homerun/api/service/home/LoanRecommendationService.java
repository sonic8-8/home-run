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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanRecommendationService {

    private static final int MAX_PER_CATEGORY = 5;

    private static final double JEONSE_LOAN_CSS_WEIGHT = 0.4;
    private static final double MORTGAGE_LOAN_CSS_WEIGHT = 0.3;

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

    private static final List<String> CREDIT_RATE_KEYS = List.of(
            "crdt_grad_1",
            "crdt_grad_4",
            "crdt_grad_5",
            "crdt_grad_6",
            "crdt_grad_10",
            "crdt_grad_11",
            "crdt_grad_12",
            "crdt_grad_13"
    );

    private final FssLoanClient fssLoanClient;
    private final CreditScoreProvider creditScoreProvider;

    public LoanRecommendationResponse getRecommendations(final Long userId) {
        final CreditScore css = creditScoreProvider.calculate(userId);

        final List<LoanRecommendationItem> creditItems = limitSorted(
                buildCreditLoanItems(fssLoanClient.getCreditLoanProducts(), css));
        final List<LoanRecommendationItem> jeonseItems = limitSorted(
                buildJeonseLoanItems(fssLoanClient.getRentHouseLoanProducts(), css));
        final List<LoanRecommendationItem> mortgageItems = limitSorted(
                buildMortgageLoanItems(fssLoanClient.getMortgageLoanProducts(), css));

        final double estimatedMinRate = creditItems.isEmpty()
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

    private List<LoanRecommendationItem> buildCreditLoanItems(
            final FssLoanResponse fssResponse,
            final CreditScore css
    ) {
        if (fssResponse.isEmpty()) {
            return List.of();
        }

        final Map<String, List<Map<String, Object>>> optionMap = groupCreditLoanOptions(fssResponse.getOptionList());
        final List<LoanRecommendationItem> items = new ArrayList<>();

        for (final Map<String, Object> base : fssResponse.getBaseList()) {
            final List<Map<String, Object>> options = optionMap.get(keyOf(base));
            if (options == null || options.isEmpty()) {
                continue;
            }

            final CreditRateSelection selection = selectCreditRate(options, css.getScore());
            if (selection == null) {
                continue;
            }

            items.add(LoanRecommendationItem.builder()
                    .productId((String) base.get("fin_prdt_cd"))
                    .bankName((String) base.get("kor_co_nm"))
                    .bankLogoUrl(resolveBankLogo((String) base.get("kor_co_nm")))
                    .productName(sanitizeText((String) base.get("fin_prdt_nm")))
                    .productType("개인신용대출")
                    .minRate(selection.minRate)
                    .maxRate(selection.maxRate)
                    .estimatedRate(selection.estimatedRate)
                    .joinWay(sanitizeNullable(base.get("join_way")))
                    .creditProductTypeName(sanitizeNullable(base.get("crdt_prdt_type_nm")))
                    .build());
        }

        return items;
    }

    private List<LoanRecommendationItem> buildJeonseLoanItems(
            final FssLoanResponse fssResponse,
            final CreditScore css
    ) {
        if (fssResponse.isEmpty()) {
            return List.of();
        }

        final Map<String, List<Map<String, Object>>> optionMap = groupLoanOptions(fssResponse.getOptionList());
        final List<LoanRecommendationItem> items = new ArrayList<>();

        for (final Map<String, Object> base : fssResponse.getBaseList()) {
            final List<Map<String, Object>> options = optionMap.get(keyOf(base));
            if (options == null || options.isEmpty()) {
                continue;
            }

            final LoanOptionSelection selection = selectLoanOption(options, css.rateCoefficient(), JEONSE_LOAN_CSS_WEIGHT, false);
            if (selection == null) {
                continue;
            }

            items.add(LoanRecommendationItem.builder()
                    .productId((String) base.get("fin_prdt_cd"))
                    .bankName((String) base.get("kor_co_nm"))
                    .bankLogoUrl(resolveBankLogo((String) base.get("kor_co_nm")))
                    .productName(sanitizeText((String) base.get("fin_prdt_nm")))
                    .productType("전세자금대출")
                    .minRate(selection.minRate)
                    .maxRate(selection.maxRate)
                    .estimatedRate(selection.estimatedRate)
                    .joinWay(sanitizeNullable(base.get("join_way")))
                    .averageRate(selection.averageRate)
                    .rateTypeName(selection.rateTypeName)
                    .repaymentTypeName(selection.repaymentTypeName)
                    .loanLimit(sanitizeNullable(base.get("loan_lmt")))
                    .build());
        }

        return items;
    }

    private List<LoanRecommendationItem> buildMortgageLoanItems(
            final FssLoanResponse fssResponse,
            final CreditScore css
    ) {
        if (fssResponse.isEmpty()) {
            return List.of();
        }

        final Map<String, List<Map<String, Object>>> optionMap = groupLoanOptions(fssResponse.getOptionList());
        final List<LoanRecommendationItem> items = new ArrayList<>();

        for (final Map<String, Object> base : fssResponse.getBaseList()) {
            final List<Map<String, Object>> options = optionMap.get(keyOf(base));
            if (options == null || options.isEmpty()) {
                continue;
            }

            final LoanOptionSelection selection = selectLoanOption(options, css.rateCoefficient(), MORTGAGE_LOAN_CSS_WEIGHT, true);
            if (selection == null) {
                continue;
            }

            items.add(LoanRecommendationItem.builder()
                    .productId((String) base.get("fin_prdt_cd"))
                    .bankName((String) base.get("kor_co_nm"))
                    .bankLogoUrl(resolveBankLogo((String) base.get("kor_co_nm")))
                    .productName(sanitizeText((String) base.get("fin_prdt_nm")))
                    .productType("주택담보대출")
                    .minRate(selection.minRate)
                    .maxRate(selection.maxRate)
                    .estimatedRate(selection.estimatedRate)
                    .joinWay(sanitizeNullable(base.get("join_way")))
                    .averageRate(selection.averageRate)
                    .rateTypeName(selection.rateTypeName)
                    .repaymentTypeName(selection.repaymentTypeName)
                    .loanLimit(sanitizeNullable(base.get("loan_lmt")))
                    .mortgageTypeName(selection.mortgageTypeName)
                    .build());
        }

        return items;
    }

    private Map<String, List<Map<String, Object>>> groupCreditLoanOptions(final List<Map<String, Object>> optionList) {
        final Map<String, List<Map<String, Object>>> optionMap = new HashMap<>();

        for (final Map<String, Object> option : optionList) {
            if (!isCreditLoanRateOption(option)) {
                continue;
            }
            optionMap.computeIfAbsent(keyOf(option), key -> new ArrayList<>()).add(option);
        }

        return optionMap;
    }

    private Map<String, List<Map<String, Object>>> groupLoanOptions(final List<Map<String, Object>> optionList) {
        final Map<String, List<Map<String, Object>>> optionMap = new HashMap<>();

        for (final Map<String, Object> option : optionList) {
            final Double minRate = toDouble(option.get("lend_rate_min"));
            final Double maxRate = toDouble(option.get("lend_rate_max"));
            if (minRate == null || maxRate == null) {
                continue;
            }

            optionMap.computeIfAbsent(keyOf(option), key -> new ArrayList<>()).add(option);
        }

        return optionMap;
    }

    private CreditRateSelection selectCreditRate(final List<Map<String, Object>> options, final int cssScore) {
        final List<Double> allRates = new ArrayList<>();
        Double selectedRate = null;

        for (final Map<String, Object> option : options) {
            collectCreditRates(option, allRates);

            final Double optionRate = resolveCreditRate(option, cssScore);
            if (optionRate == null) {
                continue;
            }

            if (selectedRate == null || optionRate < selectedRate) {
                selectedRate = optionRate;
            }
        }

        if (selectedRate == null) {
            return null;
        }

        if (allRates.isEmpty()) {
            allRates.add(selectedRate);
        }

        double minRate = allRates.get(0);
        double maxRate = allRates.get(0);
        for (final Double rate : allRates) {
            minRate = Math.min(minRate, rate);
            maxRate = Math.max(maxRate, rate);
        }

        return new CreditRateSelection(
                round(minRate),
                round(maxRate),
                round(selectedRate)
        );
    }

    private LoanOptionSelection selectLoanOption(
            final List<Map<String, Object>> options,
            final double rateCoefficient,
            final double cssWeight,
            final boolean includeMortgageType
    ) {
        LoanOptionSelection bestSelection = null;

        for (final Map<String, Object> option : options) {
            final Double minRate = toDouble(option.get("lend_rate_min"));
            final Double maxRate = toDouble(option.get("lend_rate_max"));
            if (minRate == null || maxRate == null) {
                continue;
            }

            final double estimatedRate = estimateRate(minRate, maxRate, rateCoefficient, cssWeight);
            final Double averageRate = toDouble(option.get("lend_rate_avg"));

            final LoanOptionSelection selection = new LoanOptionSelection(
                    round(minRate),
                    round(maxRate),
                    round(estimatedRate),
                    round(averageRate != null ? averageRate : estimatedRate),
                    sanitizeNullable(option.get("lend_rate_type_nm")),
                    sanitizeNullable(option.get("rpay_type_nm")),
                    includeMortgageType ? sanitizeNullable(option.get("mrtg_type_nm")) : null
            );

            if (bestSelection == null || selection.isBetterThan(bestSelection)) {
                bestSelection = selection;
            }
        }

        return bestSelection;
    }

    private boolean isCreditLoanRateOption(final Map<String, Object> option) {
        final String rateType = String.valueOf(option.get("crdt_lend_rate_type"));
        final String rateTypeName = sanitizeNullable(option.get("crdt_lend_rate_type_nm"));
        return "A".equals(rateType) || "대출금리".equals(rateTypeName);
    }

    private Double resolveCreditRate(final Map<String, Object> option, final int cssScore) {
        final String scoreKey = resolveCreditScoreKey(cssScore);
        final Double bandRate = toDouble(option.get(scoreKey));
        if (bandRate != null) {
            return bandRate;
        }
        return toDouble(option.get("crdt_grad_avg"));
    }

    private String resolveCreditScoreKey(final int cssScore) {
        if (cssScore > 900) {
            return "crdt_grad_1";
        }
        if (cssScore >= 801) {
            return "crdt_grad_4";
        }
        if (cssScore >= 701) {
            return "crdt_grad_5";
        }
        if (cssScore >= 601) {
            return "crdt_grad_6";
        }
        if (cssScore >= 501) {
            return "crdt_grad_10";
        }
        if (cssScore >= 401) {
            return "crdt_grad_11";
        }
        if (cssScore >= 301) {
            return "crdt_grad_12";
        }
        return "crdt_grad_13";
    }

    private void collectCreditRates(final Map<String, Object> option, final List<Double> rates) {
        for (final String key : CREDIT_RATE_KEYS) {
            final Double rate = toDouble(option.get(key));
            if (rate != null) {
                rates.add(rate);
            }
        }
    }

    private List<LoanRecommendationItem> limitSorted(final List<LoanRecommendationItem> items) {
        return items.stream()
                .sorted(Comparator.comparingDouble(LoanRecommendationItem::getEstimatedRate))
                .limit(MAX_PER_CATEGORY)
                .toList();
    }

    private String keyOf(final Map<String, Object> data) {
        return data.get("fin_co_no") + "|" + data.get("fin_prdt_cd");
    }

    private String resolveBankLogo(final String bankName) {
        return BANK_LOGO_MAP.getOrDefault(bankName, "/images/banks/default.png");
    }

    private String sanitizeText(final String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\n", " ").trim();
    }

    private String sanitizeNullable(final Object value) {
        if (value == null) {
            return null;
        }

        final String sanitized = value.toString().replace("\n", " ").trim();
        if (sanitized.isBlank()) {
            return null;
        }

        return sanitized;
    }

    private Double toDouble(final Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (final NumberFormatException e) {
            return null;
        }
    }

    private double estimateRate(
            final double minRate,
            final double maxRate,
            final double rateCoefficient,
            final double cssWeight
    ) {
        final BigDecimal min = BigDecimal.valueOf(minRate);
        final BigDecimal max = BigDecimal.valueOf(maxRate);
        final BigDecimal weight = BigDecimal.valueOf(rateCoefficient * cssWeight);
        return round(min.add(max.subtract(min).multiply(weight)).doubleValue());
    }

    private double round(final double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static class CreditRateSelection {
        private final double minRate;
        private final double maxRate;
        private final double estimatedRate;

        private CreditRateSelection(final double minRate, final double maxRate, final double estimatedRate) {
            this.minRate = minRate;
            this.maxRate = maxRate;
            this.estimatedRate = estimatedRate;
        }
    }

    private static class LoanOptionSelection {
        private final double minRate;
        private final double maxRate;
        private final double estimatedRate;
        private final double averageRate;
        private final String rateTypeName;
        private final String repaymentTypeName;
        private final String mortgageTypeName;

        private LoanOptionSelection(
                final double minRate,
                final double maxRate,
                final double estimatedRate,
                final double averageRate,
                final String rateTypeName,
                final String repaymentTypeName,
                final String mortgageTypeName
        ) {
            this.minRate = minRate;
            this.maxRate = maxRate;
            this.estimatedRate = estimatedRate;
            this.averageRate = averageRate;
            this.rateTypeName = rateTypeName;
            this.repaymentTypeName = repaymentTypeName;
            this.mortgageTypeName = mortgageTypeName;
        }

        private boolean isBetterThan(final LoanOptionSelection other) {
            if (estimatedRate != other.estimatedRate) {
                return estimatedRate < other.estimatedRate;
            }
            if (averageRate != other.averageRate) {
                return averageRate < other.averageRate;
            }
            return minRate < other.minRate;
        }
    }
}
