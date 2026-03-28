package io.ssafy.p.j14c103.homerun.api.service.world.response;

import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import lombok.Getter;

@Getter
public class GameTurnWorldStateResponse {

    private final CyclePhase phase;
    private final String description;

    private GameTurnWorldStateResponse(final CyclePhase phase, final String description) {
        validatePhase(phase);
        validateDescription(description);
        this.phase = phase;
        this.description = description;
    }

    public static GameTurnWorldStateResponse of(final CyclePhase phase, final String description) {
        return new GameTurnWorldStateResponse(phase, description);
    }

    private void validatePhase(final CyclePhase phase) {
        if (phase == null) {
            throw new IllegalArgumentException("phase는 null일 수 없습니다.");
        }
    }

    private void validateDescription(final String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description은 비어 있을 수 없습니다.");
        }
    }
}
