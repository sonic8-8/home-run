package io.ssafy.p.j14c103.homerun.api.service.world;

import io.ssafy.p.j14c103.homerun.api.service.world.result.GameWorldResult;
import io.ssafy.p.j14c103.homerun.domain.character.GameSessionRef;
import io.ssafy.p.j14c103.homerun.domain.character.GameSessionRefRepository;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleTransitionPolicy;
import io.ssafy.p.j14c103.homerun.domain.world.housing.GameHousing;
import io.ssafy.p.j14c103.homerun.domain.world.housing.GameHousingRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GameWorldResultService {

    private final GameSessionRefRepository gameSessionRefRepository;
    private final GameHousingRepository gameHousingRepository;
    private final CycleTransitionPolicy cycleTransitionPolicy;

    public GameWorldResult buildWorldResult(int gameSessionId, int roll) {
        GameSessionRef gameSessionRef = gameSessionRefRepository.findById(gameSessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.WORLD_SESSION_NOT_FOUND));
        CyclePhase currentPhase = parseCyclePhase(gameSessionRef.getEconomicCycleType());
        CyclePhase nextPhase = cycleTransitionPolicy.nextPhase(currentPhase, roll);
        String description = cycleTransitionPolicy.descriptionOf(nextPhase);

        return GameWorldResult.of(
            GameWorldResult.CycleResult.of(nextPhase, description),
            List.of(),
            List.of(),
            buildHousingSnapshot(gameSessionId)
        );
    }

    private CyclePhase parseCyclePhase(String economicCycleType) {
        if (economicCycleType == null || economicCycleType.isBlank()) {
            throw new HomerunException(ErrorCode.WORLD_CYCLE_STATE_INVALID);
        }

        try {
            return CyclePhase.valueOf(economicCycleType);
        } catch (IllegalArgumentException exception) {
            throw new HomerunException(ErrorCode.WORLD_CYCLE_STATE_INVALID, exception);
        }
    }

    private GameWorldResult.HousingSnapshot buildHousingSnapshot(int gameSessionId) {
        return gameHousingRepository.findByGameSessionId(gameSessionId)
            .map(this::toHousingSnapshot)
            .orElseGet(GameWorldResult.HousingSnapshot::empty);
    }

    private GameWorldResult.HousingSnapshot toHousingSnapshot(GameHousing gameHousing) {
        return GameWorldResult.HousingSnapshot.of(
            gameHousing.getCurrentHousingType(),
            gameHousing.getCurrentPropertyId(),
            gameHousing.getTargetPropertyId(),
            false
        );
    }
}
