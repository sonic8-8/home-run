package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import io.ssafy.p.j14c103.homerun.api.service.game.port.TurnPreviewCalculator;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.request.SubmitTurnSlotsServiceRequest;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.GameStatRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.TurnPreviewPolicy;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CharacterTurnPreviewCalculator implements TurnPreviewCalculator {

    private final GameStatRepository gameStatRepository;
    private final TurnPreviewPolicy turnPreviewPolicy;

    public CharacterTurnPreviewCalculator(
        final GameStatRepository gameStatRepository,
        final TurnPreviewPolicy turnPreviewPolicy
    ) {
        this.gameStatRepository = gameStatRepository;
        this.turnPreviewPolicy = turnPreviewPolicy;
    }

    @Override
    public TurnPreviewResult calculate(
        final GameSession gameSession,
        final SubmitTurnSlotsServiceRequest request
    ) {
        final GameStat gameStat = gameStatRepository.findById(Math.toIntExact(gameSession.getGameSessionId()))
            .orElseThrow(() -> new HomerunException(ErrorCode.GAME_SESSION_NOT_FOUND));
        final TurnPreviewPolicy.PreviewResult previewResult = turnPreviewPolicy.preview(
            gameStat,
            gameSession.getCurrentTurn(),
            request.getSlots().stream()
                .map(slot -> TurnPreviewPolicy.RequestedSlot.of(
                    slot.getSlotIndex(),
                    slot.getActionType()
                ))
                .toList()
        );

        return TurnPreviewResult.of(
            previewResult.slots().stream()
                .map(slot -> PreviewSlot.of(
                    slot.slotIndex(),
                    slot.actionType(),
                    slot.forcedAction()
                ))
                .toList(),
            Money.of(previewResult.cashPreview().minimumCashDelta()),
            Money.of(previewResult.cashPreview().minimumCashDelta()),
            Money.of(previewResult.cashPreview().maximumCashDelta()),
            Map.of(
                "health", previewResult.statPreview().healthDelta(),
                "fatigue", previewResult.statPreview().fatigueDelta(),
                "stress", previewResult.statPreview().stressDelta(),
                "happiness", previewResult.statPreview().happinessDelta(),
                "knowledge", previewResult.statPreview().knowledgeDelta()
            )
        );
    }
}
