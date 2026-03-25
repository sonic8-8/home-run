package io.ssafy.p.j14c103.homerun.api.service.seedmoney.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SeedmoneyTransferServiceRequest {

    private Long amount;
    private String toAccountNumber;

    @Builder
    private SeedmoneyTransferServiceRequest(final Long amount, final String toAccountNumber) {
        this.amount = amount;
        this.toAccountNumber = toAccountNumber;
    }
}
