package io.ssafy.p.j14c103.homerun.api.service.world;

import io.ssafy.p.j14c103.homerun.api.service.world.result.GameWorldResult;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousing;
import io.ssafy.p.j14c103.homerun.domain.gamesession.housing.GameHousingRepository;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleDecisionRolls;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleState;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleTransitionPolicy;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEvent;
import io.ssafy.p.j14c103.homerun.domain.world.event.GameEventRepository;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.domain.world.news.NewsMaster;
import io.ssafy.p.j14c103.homerun.domain.world.news.NewsMasterRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GameWorldResultService {

    private static final String CYCLE_SUBTYPE_SCOPE = "cycle-subtype";
    private static final String CYCLE_DURATION_SCOPE = "cycle-duration";
    private static final String NEWS_SELECTION_SCOPE = "news-selection";
    private static final String EVENT_SCOPE_PREFIX = "event:";

    private final GameSessionRepository gameSessionRepository;
    private final GameHousingRepository gameHousingRepository;
    private final CycleTransitionPolicy cycleTransitionPolicy;
    private final NewsMasterRepository newsMasterRepository;
    private final GameEventRepository gameEventRepository;
    private final WorldEventTriggerService worldEventTriggerService;
    private final GameWorldRollService gameWorldRollService;

    public GameWorldResult buildWorldResult(final Long gameSessionId, final int roll) {
        return buildWorldResult(
            gameSessionId,
            buildCycleDecisionRolls(gameSessionId, roll),
            roll
        );
    }

    public GameWorldResult buildWorldResult(
        final Long gameSessionId,
        final CycleDecisionRolls rolls
    ) {
        return buildWorldResult(gameSessionId, rolls, rolls.getPhaseRoll());
    }

    private GameWorldResult buildWorldResult(
        final Long gameSessionId,
        final CycleDecisionRolls rolls,
        final int selectionRoll
    ) {
        final GameSession gameSession = gameSessionRepository.findById(gameSessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.WORLD_SESSION_NOT_FOUND));
        final CycleState currentState = requireCycleState(gameSession);
        final CycleState nextState = cycleTransitionPolicy.nextState(currentState, rolls);
        final String description = cycleTransitionPolicy.descriptionOf(nextState.getPhase());

        return GameWorldResult.of(
            GameWorldResult.CycleResult.of(
                nextState.getPhase(),
                nextState.getType(),
                nextState.getRemainingTurns(),
                description
            ),
            buildNewsCandidates(
                gameSessionId,
                currentState.getPhase(),
                nextState.getPhase(),
                selectionRoll
            ),
            buildEventCandidates(gameSessionId, selectionRoll),
            buildHousingSnapshot(gameSession)
        );
    }

    private CycleDecisionRolls buildCycleDecisionRolls(
        final Long gameSessionId,
        final int roll
    ) {
        return CycleDecisionRolls.of(
            roll,
            gameWorldRollService.deriveRoll(gameSessionId, roll, CYCLE_SUBTYPE_SCOPE),
            gameWorldRollService.deriveRoll(gameSessionId, roll, CYCLE_DURATION_SCOPE)
        );
    }

    private CycleState requireCycleState(final GameSession gameSession) {
        if (gameSession.getCyclePhase() == null
            || gameSession.getCycleType() == null
            || gameSession.getCycleRemainingTurns() == null) {
            throw new HomerunException(ErrorCode.WORLD_CYCLE_STATE_INVALID);
        }
        try {
            return CycleState.of(
                gameSession.getCyclePhase(),
                gameSession.getCycleType(),
                gameSession.getCycleRemainingTurns()
            );
        } catch (HomerunException exception) {
            throw new HomerunException(ErrorCode.WORLD_CYCLE_STATE_INVALID, exception);
        }
    }

    private List<GameWorldResult.NewsCandidate> buildNewsCandidates(
        final Long gameSessionId,
        final CyclePhase currentPhase,
        final CyclePhase nextPhase,
        final int selectionRoll
    ) {
        final String economicCycleType = currentPhase.name() + "_TO_" + nextPhase.name();
        final List<NewsMaster> candidates = newsMasterRepository
            .findAllByEconomicCycleTypeOrderByNewsIdAsc(economicCycleType);
        if (candidates.isEmpty()) {
            return List.of();
        }

        final NewsMaster selectedNews = candidates.get(
            gameWorldRollService.selectIndex(
                gameSessionId,
                selectionRoll,
                NEWS_SELECTION_SCOPE + ":" + economicCycleType,
                candidates.size()
            )
        );
        return List.of(GameWorldResult.NewsCandidate.of(
            selectedNews.getNewsId(),
            selectedNews.getTitle()
        ));
    }

    private List<GameWorldResult.EventCandidate> buildEventCandidates(
        final Long gameSessionId,
        final int selectionRoll
    ) {
        final List<GameEvent> activeEvents = gameEventRepository.findAllByActiveYnTrueOrderByGameEventIdAsc();
        if (activeEvents.isEmpty()) {
            return List.of();
        }

        final java.util.Map<String, BigDecimal> eventRolls = activeEvents.stream()
            .collect(Collectors.toMap(
                GameEvent::getEventCode,
                event -> gameWorldRollService.deriveProbabilityRoll(
                    gameSessionId,
                    selectionRoll,
                    EVENT_SCOPE_PREFIX + event.getEventCode()
                ),
                (left, right) -> left,
                java.util.LinkedHashMap::new
            ));
        return worldEventTriggerService.calculateEventCandidates(gameSessionId, eventRolls);
    }

    private GameWorldResult.HousingSnapshot buildHousingSnapshot(final GameSession gameSession) {
        return gameHousingRepository.findByGameSessionId(gameSession.getGameSessionId())
            .map(gameHousing -> toHousingSnapshot(gameSession, gameHousing))
            .orElseGet(() -> GameWorldResult.HousingSnapshot.of(
                null,
                null,
                gameSession.getTargetPropertyId(),
                hasHousingLossSignal(gameSession, null)
            ));
    }

    private GameWorldResult.HousingSnapshot toHousingSnapshot(
        final GameSession gameSession,
        final GameHousing gameHousing
    ) {
        return GameWorldResult.HousingSnapshot.of(
            gameHousing.getCurrentHousingType(),
            gameHousing.getCurrentPropertyId(),
            gameSession.getTargetPropertyId(),
            hasHousingLossSignal(gameSession, gameHousing)
        );
    }

    private boolean hasHousingLossSignal(
        final GameSession gameSession,
        final GameHousing gameHousing
    ) {
        if (gameHousing == null) {
            final HousingType sessionHousingType = gameSession.getHousingType();
            return sessionHousingType != null && sessionHousingType != HousingType.NONE;
        }

        final HousingType currentHousingType = gameHousing.getCurrentHousingType();
        if (currentHousingType == null || currentHousingType == HousingType.NONE) {
            return true;
        }

        return currentHousingType == HousingType.OWNED_APT
            && gameHousing.getCurrentPropertyId() == null;
    }
}
