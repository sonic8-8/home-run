package io.ssafy.p.j14c103.homerun.domain.character.schedule;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "게임턴슬롯")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GameTurnSlot {

    @Id
    @Column(name = "게임턴슬롯번호", nullable = false)
    private Integer turnSlotId;

    @Column(name = "게임번호", nullable = false)
    private Integer gameId;

    @Column(name = "턴번호", nullable = false)
    private Integer turnNumber;

    @Column(name = "슬롯인덱스", nullable = false)
    private Integer slotIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "행동유형")
    private ActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "행동카테고리")
    private ActionCategory actionCategory;

    @Column(name = "강제행동여부", nullable = false)
    private boolean forcedAction;
}
