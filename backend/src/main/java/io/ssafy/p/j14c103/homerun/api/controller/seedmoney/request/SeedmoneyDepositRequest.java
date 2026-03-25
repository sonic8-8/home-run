package io.ssafy.p.j14c103.homerun.api.controller.seedmoney.request;

import io.ssafy.p.j14c103.homerun.api.service.seedmoney.request.SeedmoneyDepositServiceRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SeedmoneyDepositRequest {

    @NotNull(message = "{validation.seedmoney.deposit.amount.notNull}")
    @Positive(message = "{validation.seedmoney.deposit.amount.positive}")
    private Long amount;

    @NotNull(message = "{validation.seedmoney.deposit.fromAccountNumber.notNull}")
    private String fromAccountNumber;

    @Builder
    private SeedmoneyDepositRequest(final Long amount, final String fromAccountNumber) {
        this.amount = amount;
        this.fromAccountNumber = fromAccountNumber;
    }

    public SeedmoneyDepositServiceRequest toServiceRequest() {
        return SeedmoneyDepositServiceRequest.builder()
                .amount(amount)
                .fromAccountNumber(fromAccountNumber)
                .build();
    }
}
