package io.ssafy.p.j14c103.homerun.api.service.character.request;

import io.ssafy.p.j14c103.homerun.domain.character.GameStat;
import io.ssafy.p.j14c103.homerun.domain.character.career.CareerCycleEffect;
import io.ssafy.p.j14c103.homerun.domain.character.career.GameCareer;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.ActionType;
import io.ssafy.p.j14c103.homerun.domain.character.schedule.TurnSlotPreviewPolicy.RequestedSlot;
import io.ssafy.p.j14c103.homerun.domain.world.cycle.CyclePhase;
import io.ssafy.p.j14c103.homerun.domain.world.housing.HousingType;
import io.ssafy.p.j14c103.homerun.global.ErrorCode;
import io.ssafy.p.j14c103.homerun.global.HomerunException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CharacterTurnResultServiceRequest {

    private static final int SLOT_COUNT = 3;
    private static final Set<Integer> VALID_SLOT_INDICES = Set.of(0, 1, 2);

    private GameCareer gameCareer;
    private GameStat gameStat;
    private HousingType housingType;
    private int currentTurn;
    private List<TurnActionRequest> turnActions;
    private CyclePhase cyclePhase;

    @Builder(access = AccessLevel.PRIVATE)
    private CharacterTurnResultServiceRequest(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final HousingType housingType,
        final int currentTurn,
        final List<TurnActionRequest> turnActions,
        final CyclePhase cyclePhase
    ) {
        validateRequest(gameCareer, gameStat, housingType, currentTurn, turnActions);

        this.gameCareer = gameCareer;
        this.gameStat = gameStat;
        this.housingType = housingType;
        this.currentTurn = currentTurn;
        this.turnActions = List.copyOf(turnActions);
        this.cyclePhase = cyclePhase;
    }

    public static CharacterTurnResultServiceRequest of(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final HousingType housingType,
        final int currentTurn,
        final List<TurnActionRequest> turnActions
    ) {
        return of(gameCareer, gameStat, housingType, currentTurn, turnActions, null);
    }

    public static CharacterTurnResultServiceRequest of(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final HousingType housingType,
        final int currentTurn,
        final List<TurnActionRequest> turnActions,
        final CyclePhase cyclePhase
    ) {
        return CharacterTurnResultServiceRequest.builder()
            .gameCareer(gameCareer)
            .gameStat(gameStat)
            .housingType(housingType)
            .currentTurn(currentTurn)
            .turnActions(turnActions)
            .cyclePhase(cyclePhase)
            .build();
    }

    public List<RequestedSlot> toRequestedSlots() {
        return turnActions.stream()
            .map(turnAction -> RequestedSlot.of(
                turnAction.getSlotIndex(),
                turnAction.getActionType()
            ))
            .toList();
    }

    public CareerCycleEffect toCareerCycleEffect() {
        return CareerCycleEffect.from(cyclePhase, gameCareer.getJobType());
    }

    private void validateRequest(
        final GameCareer gameCareer,
        final GameStat gameStat,
        final HousingType housingType,
        final int currentTurn,
        final List<TurnActionRequest> turnActions
    ) {
        if (gameCareer == null || gameStat == null || housingType == null) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
        if (currentTurn < 1) {
            throw new HomerunException(ErrorCode.CHARACTER_TURN_INVALID);
        }
        if (!Objects.equals(gameCareer.getGameId(), gameStat.getGameId())) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
        if (gameCareer.getGameId() == null || gameCareer.getGameId() < 1) {
            throw new HomerunException(ErrorCode.CHARACTER_GAME_ID_INVALID);
        }
        validateTurnActions(turnActions);
    }

    private void validateTurnActions(final List<TurnActionRequest> turnActions) {
        if (turnActions == null || turnActions.size() != SLOT_COUNT) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
        if (turnActions.stream().anyMatch(Objects::isNull)) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
        if (turnActions.stream()
            .map(TurnActionRequest::getSlotIndex)
            .collect(java.util.stream.Collectors.toSet())
            .size() != SLOT_COUNT) {
            throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class TurnActionRequest {

        private int slotIndex;
        private ActionType actionType;

        @Builder(access = AccessLevel.PRIVATE)
        private TurnActionRequest(
            final int slotIndex,
            final ActionType actionType
        ) {
            validateRequest(slotIndex, actionType);

            this.slotIndex = slotIndex;
            this.actionType = actionType;
        }

        public static TurnActionRequest of(
            final int slotIndex,
            final ActionType actionType
        ) {
            return TurnActionRequest.builder()
                .slotIndex(slotIndex)
                .actionType(actionType)
                .build();
        }

        private void validateRequest(final int slotIndex, final ActionType actionType) {
            if (!VALID_SLOT_INDICES.contains(slotIndex)) {
                throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
            }
            if (actionType == null) {
                throw new HomerunException(ErrorCode.CHARACTER_REQUEST_INVALID);
            }
        }
    }
}
