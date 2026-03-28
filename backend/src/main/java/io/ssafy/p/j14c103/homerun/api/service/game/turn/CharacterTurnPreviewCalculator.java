package io.ssafy.p.j14c103.homerun.api.service.game.turn;

import io.ssafy.p.j14c103.homerun.api.service.character.schedule.TurnSlotPreviewService;
import io.ssafy.p.j14c103.homerun.api.service.character.schedule.request.TurnSlotPreviewRequest;
import io.ssafy.p.j14c103.homerun.api.service.character.schedule.response.TurnSlotPreviewResponse;
import io.ssafy.p.j14c103.homerun.api.service.game.port.TurnPreviewCalculator;
import io.ssafy.p.j14c103.homerun.api.service.game.turn.request.SubmitTurnSlotsServiceRequest;
import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.GameStatRepository;
import io.ssafy.p.j14c103.homerun.domain.gamesession.GameSession;
import io.ssafy.p.j14c103.homerun.domain.gamesession.turn.ActionType;
import io.ssafy.p.j14c103.homerun.domain.money.Money;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CharacterTurnPreviewCalculator implements TurnPreviewCalculator {

    private final GameStatRepository gameStatRepository;
    private final TurnSlotPreviewService turnSlotPreviewService;

    public CharacterTurnPreviewCalculator(
        final GameStatRepository gameStatRepository,
        final TurnSlotPreviewService turnSlotPreviewService
    ) {
        this.gameStatRepository = gameStatRepository;
        this.turnSlotPreviewService = turnSlotPreviewService;
    }

    @Override
    public TurnPreviewResult calculate(
        final GameSession gameSession,
        final SubmitTurnSlotsServiceRequest request
    ) {
        final GameStat gameStat = gameStatRepository.findById(Math.toIntExact(gameSession.getGameSessionId()))
            .orElseThrow(() -> new HomerunException(ErrorCode.GAME_SESSION_NOT_FOUND));
        final TurnSlotPreviewResponse previewResponse = turnSlotPreviewService.preview(
            TurnSlotPreviewRequest.of(
                gameStat,
                request.getSlots().stream()
                    .map(slot -> TurnSlotPreviewRequest.TurnSlotRequest.of(
                        slot.getSlotIndex(),
                        io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionType.valueOf(
                            slot.getActionType().name()
                        )
                    ))
                    .toList()
            )
        );

        return TurnPreviewResult.of(
            previewResponse.getSlots().stream()
                .map(slot -> PreviewSlot.of(
                    slot.getSlotIndex(),
                    ActionType.valueOf(slot.getActionType().name()),
                    slot.isForcedAction()
                ))
                .toList(),
            // Redis draft currently stores one cash delta, so keep the conservative lower bound.
            Money.of(previewResponse.getCashPreview().getMinimumCashDelta()),
            Map.of(
                "health", previewResponse.getStatPreview().getHealthDelta(),
                "fatigue", previewResponse.getStatPreview().getFatigueDelta(),
                "stress", previewResponse.getStatPreview().getStressDelta(),
                "happiness", previewResponse.getStatPreview().getHappinessDelta(),
                "knowledge", previewResponse.getStatPreview().getKnowledgeDelta()
            )
        );
    }
}
