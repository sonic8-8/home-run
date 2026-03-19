package io.ssafy.p.j14c103.homerun.api.service.seedmoney.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SeedmoneyCreateServiceRequest {

    private String accountTypeUniqueNo;

    @Builder
    private SeedmoneyCreateServiceRequest(final String accountTypeUniqueNo) {
        this.accountTypeUniqueNo = accountTypeUniqueNo;
    }
}
