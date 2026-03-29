package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import io.ssafy.p.j14c103.homerun.api.service.game.turn.response.CommitTurnResponse;
import io.ssafy.p.j14c103.homerun.api.service.world.GameWorldResultService;
import io.ssafy.p.j14c103.homerun.api.service.world.result.GameWorldResult;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.ActionCatalog;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.GameSessionTurnSlotRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.GameTurnSlot;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.TurnDraft;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.TurnDraftRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.TurnDraftSlot;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CycleState;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommitTurnService {

    private static final int DEFAULT_WORLD_ROLL = 50;
    private static final String MARKET_UPDATE_PHASE = "MARKET_UPDATE";
    private static final String ACTION_RESULT_PHASE = "ACTION_RESULT";
    private static final String ACTION_RESULT_DESCRIPTION = "턴 행동 결과를 반영한다";

    private final GameTurnCommitGuardService gameTurnCommitGuardService;
    private final GameWorldResultService gameWorldResultService;
    private final GameSessionTurnSlotRepository gameSessionTurnSlotRepository;
    private final TurnDraftRepository turnDraftRepository;
    private final ActionCatalog actionCatalog;

    @Transactional
    public CommitTurnResponse commitTurn(final Long userId, final Long sessionId) {
        final GameTurnCommitGuardService.CommitTurnGuardResult guardResult =
            gameTurnCommitGuardService.guard(userId, sessionId);
        final GameSession gameSession = guardResult.getGameSession();
        final TurnDraft turnDraft = guardResult.getTurnDraft();
        final GameWorldResult worldResult = gameWorldResultService.buildWorldResult(
            sessionId,
            DEFAULT_WORLD_ROLL
        );
        final Integer committedTurn = gameSession.getCurrentTurn();

        saveCommittedSlots(gameSession.getGameSessionId(), committedTurn, turnDraft);
        final SessionAdvanceResult sessionAdvanceResult = advanceSession(gameSession, turnDraft, worldResult);
        turnDraftRepository.deleteBySessionId(sessionId);

        return CommitTurnResponse.of(
            committedTurn,
            buildSettlementLog(worldResult, turnDraft),
            CommitTurnResponse.UpdatedAssetsResponse.of(
                toLong(sessionAdvanceResult.nextCash()),
                0L,
                0L,
                toLong(sessionAdvanceResult.nextNetWorth())
            ),
            CommitTurnResponse.StatChangesResponse.from(turnDraft.getPreviewStatChanges()),
            CommitTurnResponse.FlagsResponse.of(
                sessionAdvanceResult.nextNetWorth().isNegative(),
                false,
                false,
                false,
                !worldResult.getEventCandidates().isEmpty()
            )
        );
    }

    private void saveCommittedSlots(
        final Long sessionId,
        final Integer committedTurn,
        final TurnDraft turnDraft
    ) {
        final List<GameTurnSlot> committedSlots = turnDraft.getSlots().stream()
            .sorted(Comparator.comparing(TurnDraftSlot::getSlotIndex))
            .map(slot -> GameTurnSlot.create(
                sessionId,
                committedTurn,
                slot.getSlotIndex(),
                slot.getActionType(),
                actionCatalog.getDefinition(slot.getActionType()).category(),
                false
            ))
            .toList();
        gameSessionTurnSlotRepository.saveAllAndFlush(committedSlots);
    }

    private SessionAdvanceResult advanceSession(
        final GameSession gameSession,
        final TurnDraft turnDraft,
        final GameWorldResult worldResult
    ) {
        final Money previewCashChange = turnDraft.getPreviewCashChange();
        final Money nextCash = gameSession.getCashBalance().add(previewCashChange);
        final Money nextTotalAssets = gameSession.getTotalAssets().add(previewCashChange);
        final Money nextNetWorth = gameSession.getNetWorth().add(previewCashChange);
        final LocalDate nextDate = requireCurrentDate(gameSession).plusMonths(1);
        final CycleState nextCycleState = CycleState.of(
            worldResult.getCycleResult().getNextPhase(),
            worldResult.getCycleResult().getNextType(),
            worldResult.getCycleResult().getRemainingTurns()
        );

        gameSession.advanceTurn(
            gameSession.getCurrentTurn() + 1,
            nextDate,
            nextCash,
            nextTotalAssets,
            nextNetWorth,
            nextCycleState
        );
        return new SessionAdvanceResult(nextCash, nextNetWorth);
    }

    private LocalDate requireCurrentDate(final GameSession gameSession) {
        if (gameSession.getCurrentDate() == null) {
            throw new HomerunException(ErrorCode.GLOBAL_CONFIGURATION_INVALID);
        }
        return gameSession.getCurrentDate();
    }

    private List<CommitTurnResponse.SettlementLogItemResponse> buildSettlementLog(
        final GameWorldResult worldResult,
        final TurnDraft turnDraft
    ) {
        return List.of(
            CommitTurnResponse.SettlementLogItemResponse.of(
                MARKET_UPDATE_PHASE,
                worldResult.getCycleResult().getDescription(),
                0L,
                CommitTurnResponse.StatChangesResponse.zero()
            ),
            CommitTurnResponse.SettlementLogItemResponse.of(
                ACTION_RESULT_PHASE,
                ACTION_RESULT_DESCRIPTION,
                toLong(turnDraft.getPreviewCashChange()),
                CommitTurnResponse.StatChangesResponse.from(turnDraft.getPreviewStatChanges())
            )
        );
    }

    private Long toLong(final Money money) {
        return money.getAmount().longValueExact();
    }

    private record SessionAdvanceResult(
        Money nextCash,
        Money nextNetWorth
    ) {
    }
}
