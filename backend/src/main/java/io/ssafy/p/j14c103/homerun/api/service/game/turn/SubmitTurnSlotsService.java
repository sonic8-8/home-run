package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import io.ssafy.p.j14c103.homerun.api.service.game.port.TurnPreviewCalculator;
import io.ssafy.p.j14c103.homerun.api.service.game.port.TurnPreviewCalculator.PreviewSlot;
import io.ssafy.p.j14c103.homerun.api.service.game.port.TurnPreviewCalculator.TurnPreviewResult;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.request.SubmitTurnSlotsServiceRequest;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.response.TurnPreviewResponse;
import io.ssafy.p.j14c103.homerun.api.service.user.UserAuthContextService;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSessionRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.TurnDraft;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.TurnDraftRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.TurnDraftSlot;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubmitTurnSlotsService {

    private final GameSessionRepository gameSessionRepository;
    private final UserAuthContextService userAuthContextService;
    private final TurnPreviewCalculator turnPreviewCalculator;
    private final TurnDraftRepository turnDraftRepository;

    @Transactional
    public TurnPreviewResponse submitTurnSlots(
        final Long userId,
        final Long sessionId,
        final SubmitTurnSlotsServiceRequest request
    ) {
        userAuthContextService.getContext(userId);
        final GameSession gameSession = getOwnedGameSession(userId, sessionId);
        gameSession.assertInProgress();

        final TurnPreviewResult previewResult = turnPreviewCalculator.calculate(gameSession, request);
        final TurnDraft turnDraft = TurnDraft.of(
            gameSession.getGameSessionId(),
            gameSession.getCurrentTurn(),
            toTurnDraftSlots(previewResult.getSlots()),
            previewResult.getPreviewCashChange(),
            previewResult.getPreviewCashMinChange(),
            previewResult.getPreviewCashMaxChange(),
            previewResult.getPreviewStatChanges()
        );
        turnDraftRepository.save(turnDraft);

        return TurnPreviewResponse.from(previewResult);
    }

    private List<TurnDraftSlot> toTurnDraftSlots(final List<PreviewSlot> previewSlots) {
        return previewSlots.stream()
            .map(slot -> TurnDraftSlot.of(
                slot.getSlotIndex(),
                slot.getActionType(),
                slot.isForcedAction()
            ))
            .toList();
    }

    private GameSession getOwnedGameSession(final Long userId, final Long sessionId) {
        final GameSession gameSession = gameSessionRepository.findById(sessionId)
            .orElseThrow(() -> new HomerunException(ErrorCode.GAME_SESSION_NOT_FOUND));
        gameSession.assertOwner(userId);
        return gameSession;
    }
}
