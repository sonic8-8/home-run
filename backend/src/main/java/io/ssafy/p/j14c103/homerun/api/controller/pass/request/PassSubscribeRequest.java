package io.ssafy.p.j14c103.homerun.api.controller.pass.request;

import io.ssafy.p.j14c103.homerun.api.service.pass.request.PassSubscribeServiceRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PassSubscribeRequest {

    @NotNull(message = "{validation.pass.subscribe.passId.notNull}")
    private Long passId;

    @NotNull(message = "{validation.pass.subscribe.sourceAccountId.notNull}")
    private String sourceAccountId;

    @Builder
    private PassSubscribeRequest(final Long passId, final String sourceAccountId) {
        this.passId = passId;
        this.sourceAccountId = sourceAccountId;
    }

    public PassSubscribeServiceRequest toServiceRequest() {
        return PassSubscribeServiceRequest.builder()
                .passId(passId)
                .sourceAccountId(sourceAccountId)
                .build();
    }
}
