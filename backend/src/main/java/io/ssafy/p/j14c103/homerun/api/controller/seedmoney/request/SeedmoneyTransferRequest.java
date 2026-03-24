package io.ssafy.p.j14c103.homerun.api.controller.seedmoney.request;

import io.ssafy.p.j14c103.homerun.api.service.seedmoney.request.SeedmoneyTransferServiceRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SeedmoneyTransferRequest {

    @NotNull(message = "{validation.seedmoney.transfer.amount.notNull}")
    @Positive(message = "{validation.seedmoney.transfer.amount.positive}")
    private Long amount;

    @NotNull(message = "{validation.seedmoney.transfer.toAccountNumber.notNull}")
    private String toAccountNumber;

    @Builder
    private SeedmoneyTransferRequest(final Long amount, final String toAccountNumber) {
        this.amount = amount;
        this.toAccountNumber = toAccountNumber;
    }

    public SeedmoneyTransferServiceRequest toServiceRequest() {
        return SeedmoneyTransferServiceRequest.builder()
                .amount(amount)
                .toAccountNumber(toAccountNumber)
                .build();
    }
}
