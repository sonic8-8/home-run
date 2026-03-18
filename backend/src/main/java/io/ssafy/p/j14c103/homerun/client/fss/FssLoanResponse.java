package io.ssafy.p.j14c103.homerun.client.fss;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 금감원 대출 상품 API 응답 래퍼
 */
public class FssLoanResponse {

    private final List<Map<String, Object>> baseList;
    private final List<Map<String, Object>> optionList;

    private FssLoanResponse(final List<Map<String, Object>> baseList,
                            final List<Map<String, Object>> optionList) {
        this.baseList = baseList;
        this.optionList = optionList;
    }

    public static FssLoanResponse of(final List<Map<String, Object>> baseList,
                                     final List<Map<String, Object>> optionList) {
        return new FssLoanResponse(
                baseList != null ? baseList : Collections.emptyList(),
                optionList != null ? optionList : Collections.emptyList());
    }

    public static FssLoanResponse empty() {
        return new FssLoanResponse(Collections.emptyList(), Collections.emptyList());
    }

    public List<Map<String, Object>> getBaseList() {
        return baseList;
    }

    public List<Map<String, Object>> getOptionList() {
        return optionList;
    }

    public boolean isEmpty() {
        return baseList.isEmpty();
    }
}
