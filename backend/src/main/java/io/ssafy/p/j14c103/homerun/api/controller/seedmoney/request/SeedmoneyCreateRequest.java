package io.ssafy.p.j14c103.homerun.api.controller.seedmoney.request;

import jakarta.validation.constraints.NotBlank;

public class SeedmoneyCreateRequest {

    @NotBlank(message = "{validation.seedmoney.create.accountTypeUniqueNo.notBlank}")
    private String accountTypeUniqueNo;

    protected SeedmoneyCreateRequest() {
    }

    public String getAccountTypeUniqueNo() { return accountTypeUniqueNo; }
}
