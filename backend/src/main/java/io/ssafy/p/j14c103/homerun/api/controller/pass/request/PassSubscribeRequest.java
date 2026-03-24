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

    @Builder
    private PassSubscribeRequest(final Long passId) {
        this.passId = passId;
    }

    public PassSubscribeServiceRequest toServiceRequest() {
        return PassSubscribeServiceRequest.builder()
                .passId(passId)
                .build();
    }
}
