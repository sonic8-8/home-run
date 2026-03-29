package io.ssafy.p.j14c103.homerun.api.service.game.events.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ResolveEventServiceRequest {

    private Integer choiceId;

    @Builder(access = AccessLevel.PRIVATE)
    private ResolveEventServiceRequest(final Integer choiceId) {
        this.choiceId = choiceId;
    }

    public static ResolveEventServiceRequest of(final Integer choiceId) {
        return ResolveEventServiceRequest.builder()
            .choiceId(choiceId)
            .build();
    }
}
