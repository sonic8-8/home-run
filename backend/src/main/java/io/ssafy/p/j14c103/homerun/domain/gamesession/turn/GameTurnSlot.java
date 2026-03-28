package io.ssafy.p.j14c103.homerun.domain.gamesession.turn;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity(name = "GameSessionTurnSlot")
@Table(
    name = "game_turn_slots",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_game_turn_slots__game_session_id__turn_number__slot_index",
        columnNames = {"game_session_id", "turn_number", "slot_index"}
    )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameTurnSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_turn_slot_id")
    private Long gameTurnSlotId;

    @Column(name = "game_session_id", nullable = false)
    private Long gameSessionId;

    @Column(name = "turn_number", nullable = false)
    private Integer turnNumber;

    @Column(name = "slot_index", nullable = false)
    private Integer slotIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private ActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_category", length = 20)
    private ActionCategory actionCategory;

    @Column(name = "forced_action_yn", nullable = false)
    private boolean forcedAction;

    private GameTurnSlot(
        final Long gameSessionId,
        final Integer turnNumber,
        final Integer slotIndex,
        final ActionType actionType,
        final ActionCategory actionCategory,
        final boolean forcedAction
    ) {
        this.gameSessionId = gameSessionId;
        this.turnNumber = turnNumber;
        this.slotIndex = slotIndex;
        this.actionType = actionType;
        this.actionCategory = actionCategory;
        this.forcedAction = forcedAction;
    }

    public static GameTurnSlot create(
        final Long gameSessionId,
        final Integer turnNumber,
        final Integer slotIndex,
        final ActionType actionType,
        final ActionCategory actionCategory,
        final boolean forcedAction
    ) {
        return new GameTurnSlot(
            gameSessionId,
            turnNumber,
            slotIndex,
            actionType,
            actionCategory,
            forcedAction
        );
    }
}
