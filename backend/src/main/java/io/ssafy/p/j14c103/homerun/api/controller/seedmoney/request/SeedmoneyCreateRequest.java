package io.ssafy.p.j14c103.homerun.api.controller.seedmoney.request;

import io.ssafy.p.j14c103.homerun.api.service.seedmoney.request.SeedmoneyCreateServiceRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SeedmoneyCreateRequest {

    @NotBlank(message = "{validation.seedmoney.create.accountTypeUniqueNo.notBlank}")
    private String accountTypeUniqueNo;

    @Builder
    private SeedmoneyCreateRequest(final String accountTypeUniqueNo) {
        this.accountTypeUniqueNo = accountTypeUniqueNo;
    }

    public SeedmoneyCreateServiceRequest toServiceRequest() {
        return SeedmoneyCreateServiceRequest.builder()
                .accountTypeUniqueNo(accountTypeUniqueNo)
                .build();
    }
}
