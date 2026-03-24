package io.ssafy.p.j14c103.homerun.api.service.pass.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PassSaveServiceRequest {

    private Long subscriptionId;

    @Builder
    private PassSaveServiceRequest(final Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
}
