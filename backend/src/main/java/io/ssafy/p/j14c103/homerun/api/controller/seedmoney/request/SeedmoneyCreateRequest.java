package io.ssafy.p.j14c103.homerun.api.controller.seedmoney.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SeedmoneyCreateRequest {

    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;

    @NotBlank(message = "userKey는 필수입니다.")
    private String userKey;

    @NotBlank(message = "상품 고유번호는 필수입니다.")
    private String accountTypeUniqueNo;

    protected SeedmoneyCreateRequest() {
    }

    public Long getUserId() { return userId; }
    public String getUserKey() { return userKey; }
    public String getAccountTypeUniqueNo() { return accountTypeUniqueNo; }
}
