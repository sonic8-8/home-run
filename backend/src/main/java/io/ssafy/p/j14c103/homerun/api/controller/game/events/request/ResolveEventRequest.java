package io.ssafy.p.j14c103.homerun.api.controller.game.events.request;

import io.ssafy.p.j14c103.homerun.api.service.game.events.request.ResolveEventServiceRequest;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ResolveEventRequest {

    private Integer choiceId;

    @Builder(access = AccessLevel.PRIVATE)
    private ResolveEventRequest(final Integer choiceId) {
        this.choiceId = choiceId;
    }

    public static ResolveEventRequest empty() {
        return ResolveEventRequest.builder()
            .choiceId(null)
            .build();
    }

    public ResolveEventServiceRequest toServiceRequest() {
        return ResolveEventServiceRequest.of(choiceId);
    }
}
