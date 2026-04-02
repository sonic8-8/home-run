package io.ssafy.p.j14c103.homerun.api.service.world;

import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleTransitionPolicy;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LatestTurnNewsPhaseService {

    private final CycleTransitionPolicy cycleTransitionPolicy;

    public PhaseResolution resolve(final CyclePhase currentPhase, final int roll) {
        if (currentPhase == null) {
            throw new HomerunException(ErrorCode.WORLD_CYCLE_STATE_INVALID);
        }

        final CyclePhase nextPhase = cycleTransitionPolicy.nextPhase(currentPhase, roll);

        return PhaseResolution.of(
            currentPhase,
            nextPhase,
            currentPhase.name() + "_TO_" + nextPhase.name()
        );
    }

    @Getter
    public static class PhaseResolution {

        private final CyclePhase currentPhase;
        private final CyclePhase nextPhase;
        private final String economicCycleType;

        @Builder(access = AccessLevel.PRIVATE)
        private PhaseResolution(
            final CyclePhase currentPhase,
            final CyclePhase nextPhase,
            final String economicCycleType
        ) {
            this.currentPhase = currentPhase;
            this.nextPhase = nextPhase;
            this.economicCycleType = economicCycleType;
        }

        public static PhaseResolution of(
            final CyclePhase currentPhase,
            final CyclePhase nextPhase,
            final String economicCycleType
        ) {
            return PhaseResolution.builder()
                .currentPhase(currentPhase)
                .nextPhase(nextPhase)
                .economicCycleType(economicCycleType)
                .build();
        }
    }
}
