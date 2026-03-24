package io.ssafy.p.j14c103.homerun.api.controller.pass.request;

import io.ssafy.p.j14c103.homerun.api.service.pass.request.PassSaveServiceRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PassSaveRequest {

    @NotNull(message = "{validation.pass.save.subscriptionId.notNull}")
    private Long subscriptionId;

    @NotNull(message = "{validation.pass.save.sourceAccountId.notNull}")
    private String sourceAccountId;

    @Builder
    private PassSaveRequest(final Long subscriptionId, final String sourceAccountId) {
        this.subscriptionId = subscriptionId;
        this.sourceAccountId = sourceAccountId;
    }

    public PassSaveServiceRequest toServiceRequest() {
        return PassSaveServiceRequest.builder()
                .subscriptionId(subscriptionId)
                .sourceAccountId(sourceAccountId)
                .build();
    }
}
