package io.ssafy.p.j14c103.homerun.api.service.world;

import io.ssafy.p.j14c103.homerun.api.service.world.result.GameWorldResult;
import io.ssafy.p.j14c103.homerun.domain.character.GameSessionRef;
import io.ssafy.p.j14c103.homerun.domain.character.GameSessionRefRepository;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.GameStatRepository;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareerRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventCondition;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventConditionRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEvent;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEventRepository;
import io.ssafy.p.j14c103.homerun.domain.world.event.WorldEventTriggerPolicy;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WorldEventTriggerService {

    private final WorldEventTriggerPolicy worldEventTriggerPolicy = new WorldEventTriggerPolicy();
    private final GameSessionRefRepository gameSessionRefRepository;
    private final GameStatRepository gameStatRepository;
    private final GameCareerRepository gameCareerRepository;
    private final GameEventRepository gameEventRepository;
    private final EventConditionRepository eventConditionRepository;

    public List<GameWorldResult.EventCandidate> calculateEventCandidates(
        final int gameSessionId,
        final Map<String, BigDecimal> eventRolls
    ) {
        final GameSessionRef gameSessionRef = gameSessionRefRepository.findById(gameSessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.WORLD_SESSION_NOT_FOUND));
        final GameStat gameStat = gameStatRepository.findById(gameSessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.WORLD_SESSION_NOT_FOUND));
        final GameCareer gameCareer = gameCareerRepository.findById(gameSessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.WORLD_SESSION_NOT_FOUND));
        final WorldEventTriggerPolicy.TriggerContext triggerContext = WorldEventTriggerPolicy.TriggerContext.of(
            requireEconomicCycleType(gameSessionRef),
            requireKnowledge(gameStat),
            requireTenureTurns(gameCareer)
        );

        return gameEventRepository.findAllByActiveYnTrueOrderByGameEventIdAsc().stream()
            .filter(gameEvent -> isTriggered(gameEvent, triggerContext, eventRolls))
            .map(this::toEventCandidate)
            .toList();
    }

    private boolean isTriggered(
        final GameEvent gameEvent,
        final WorldEventTriggerPolicy.TriggerContext triggerContext,
        final Map<String, BigDecimal> eventRolls
    ) {
        final List<EventCondition> conditions = eventConditionRepository
            .findAllByGameEventIdOrderByConditionGroupNumberAscConditionOrderAsc(gameEvent.getGameEventId());

        return worldEventTriggerPolicy.isTriggered(
            gameEvent,
            conditions,
            triggerContext,
            resolveRoll(eventRolls, gameEvent.getEventCode())
        );
    }

    private GameWorldResult.EventCandidate toEventCandidate(final GameEvent gameEvent) {
        return GameWorldResult.EventCandidate.of(
            gameEvent.getGameEventId(),
            gameEvent.getEventCode(),
            gameEvent.getEventName(),
            gameEvent.getEventPresentationType()
        );
    }

    private BigDecimal resolveRoll(
        final Map<String, BigDecimal> eventRolls,
        final String eventCode
    ) {
        if (eventRolls == null) {
            return null;
        }

        return eventRolls.get(eventCode);
    }

    private String requireEconomicCycleType(final GameSessionRef gameSessionRef) {
        if (gameSessionRef.getEconomicCycleType() == null || gameSessionRef.getEconomicCycleType().isBlank()) {
            throw new HomerunException(ErrorCode.WORLD_CYCLE_STATE_INVALID);
        }

        return gameSessionRef.getEconomicCycleType();
    }

    private Integer requireKnowledge(final GameStat gameStat) {
        if (gameStat.getKnowledge() == null) {
            throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
        }

        return gameStat.getKnowledge();
    }

    private Integer requireTenureTurns(final GameCareer gameCareer) {
        if (gameCareer.getTenureTurns() == null) {
            throw new HomerunException(ErrorCode.WORLD_RESULT_INVALID);
        }

        return gameCareer.getTenureTurns();
    }
}
