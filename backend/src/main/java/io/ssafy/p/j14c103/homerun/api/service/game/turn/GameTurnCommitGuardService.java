package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.GameSessionTurnSlotRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.TurnDraft;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.TurnDraftRepository;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameTurnCommitGuardService {

    private final GameSessionRepository gameSessionRepository;
    private final GameSessionTurnSlotRepository gameSessionTurnSlotRepository;
    private final TurnDraftRepository turnDraftRepository;
    private final UserAuthContextService userAuthContextService;

    @Transactional
    public CommitTurnGuardResult guard(final Long userId, final Long sessionId) {
        userAuthContextService.getContext(userId);

        final GameSession gameSession = gameSessionRepository.findByIdForUpdate(sessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.GAME_SESSION_NOT_FOUND));
        gameSession.assertOwner(userId);
        gameSession.assertInProgress();

        final Integer currentTurn = gameSession.getCurrentTurn();
        if (gameSessionTurnSlotRepository.existsByGameSessionIdAndTurnNumber(sessionId, currentTurn)) {
            throw new HomerunException(ErrorCode.GAME_TURN_ALREADY_COMMITTED);
        }

        final TurnDraft turnDraft = turnDraftRepository.findBySessionId(sessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.GAME_TURN_DRAFT_NOT_FOUND));
        validateTurnMatches(gameSession, turnDraft);

        return CommitTurnGuardResult.of(gameSession, turnDraft);
    }

    private void validateTurnMatches(final GameSession gameSession, final TurnDraft turnDraft) {
        if (gameSession.getCurrentTurn().equals(turnDraft.getTurnNumber())) {
            return;
        }

        throw new HomerunException(ErrorCode.GAME_TURN_DRAFT_MISMATCH);
    }

    @Getter
    public static class CommitTurnGuardResult {

        private final GameSession gameSession;
        private final TurnDraft turnDraft;

        private CommitTurnGuardResult(
            final GameSession gameSession,
            final TurnDraft turnDraft
        ) {
            this.gameSession = gameSession;
            this.turnDraft = turnDraft;
        }

        public static CommitTurnGuardResult of(
            final GameSession gameSession,
            final TurnDraft turnDraft
        ) {
            return new CommitTurnGuardResult(gameSession, turnDraft);
        }
    }
}
