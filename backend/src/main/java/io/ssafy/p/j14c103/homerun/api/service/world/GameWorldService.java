package io.ssafy.p.j14c103.homerun.api.service.world;

import io.ssafy.p.j14c103.homerun.api.service.world.response.GameTurnResponse;
import io.ssafy.p.j14c103.homerun.domain.character.GameSessionRef;
import io.ssafy.p.j14c103.homerun.domain.character.GameSessionRefRepository;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleTransitionPolicy;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GameWorldService {

    private final GameSessionRefRepository gameSessionRefRepository;
    private final CycleTransitionPolicy cycleTransitionPolicy;

    public GameTurnResponse getTurn(int gameSessionId) {
        GameSessionRef gameSessionRef = gameSessionRefRepository.findById(gameSessionId)
            .orElseThrow(() -> HomerunException.from(ErrorCode.WORLD_SESSION_NOT_FOUND));
        CyclePhase cyclePhase = parseCyclePhase(gameSessionRef.getEconomicCycleType());
        String description = cycleTransitionPolicy.descriptionOf(cyclePhase);

        return GameTurnResponse.of(
            gameSessionRef.getCurrentTurn(),
            gameSessionRef.getCurrentDate(),
            GameTurnResponse.EconomicCycleResponse.of(cyclePhase, description),
            GameTurnResponse.NewsResponse.emptyList()
        );
    }

    private CyclePhase parseCyclePhase(String economicCycleType) {
        if (economicCycleType == null || economicCycleType.isBlank()) {
            throw HomerunException.from(ErrorCode.WORLD_CYCLE_STATE_INVALID);
        }

        try {
            return CyclePhase.valueOf(economicCycleType);
        } catch (IllegalArgumentException exception) {
            throw HomerunException.from(ErrorCode.WORLD_CYCLE_STATE_INVALID, exception);
        }
    }
}
