package io.ssafy.p.j14c103.homerun.api.controller.seedmoney.request;

import jakarta.validation.constraints.NotBlank;

public class SeedmoneyCreateRequest {

    @NotBlank(message = "상품 고유번호는 필수입니다.")
    private String accountTypeUniqueNo;

    protected SeedmoneyCreateRequest() {
    }

    public String getAccountTypeUniqueNo() { return accountTypeUniqueNo; }
}
