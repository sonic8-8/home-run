package io.ssafy.p.j14c103.homerun.api.service.seedmoney.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SeedmoneyDepositServiceRequest {

    private Long amount;
    private String fromAccountNumber;

    @Builder
    private SeedmoneyDepositServiceRequest(final Long amount, final String fromAccountNumber) {
        this.amount = amount;
        this.fromAccountNumber = fromAccountNumber;
    }
}
