package io.ssafy.p.j14c103.homerun.api.service.world.response;

import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class GameTurnWorldStateResponse {

    private final CyclePhase phase;
    private final String description;

    @Builder(access = AccessLevel.PRIVATE)
    private GameTurnWorldStateResponse(final CyclePhase phase, final String description) {
        this.phase = Objects.requireNonNull(phase, "phase는 null일 수 없습니다.");
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description은 비어 있을 수 없습니다.");
        }
        this.description = description;
    }

    public static GameTurnWorldStateResponse of(final CyclePhase phase, final String description) {
        return GameTurnWorldStateResponse.builder()
            .phase(phase)
            .description(description)
            .build();
    }
}
