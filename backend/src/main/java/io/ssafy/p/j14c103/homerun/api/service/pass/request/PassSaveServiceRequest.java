package io.ssafy.p.j14c103.homerun.api.service.pass.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PassSaveServiceRequest {

    private Long subscriptionId;
    private String sourceAccountId;

    @Builder
    private PassSaveServiceRequest(final Long subscriptionId, final String sourceAccountId) {
        this.subscriptionId = subscriptionId;
        this.sourceAccountId = sourceAccountId;
    }
}
