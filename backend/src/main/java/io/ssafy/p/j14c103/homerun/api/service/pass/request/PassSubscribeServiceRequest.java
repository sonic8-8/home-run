package io.ssafy.p.j14c103.homerun.api.service.pass.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PassSubscribeServiceRequest {

    private Long passId;

    @Builder
    private PassSubscribeServiceRequest(final Long passId) {
        this.passId = passId;
    }
}
