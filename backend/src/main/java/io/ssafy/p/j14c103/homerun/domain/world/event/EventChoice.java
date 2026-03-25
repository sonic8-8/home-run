package io.ssafy.p.j14c103.homerun.domain.world.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "event_choices")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventChoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_choice_id")
    private Integer eventChoiceId;

    @Column(name = "game_event_id", nullable = false)
    private Integer gameEventId;

    @Column(name = "choice_code", nullable = false)
    private String choiceCode;

    @Column(name = "choice_name", nullable = false)
    private String choiceName;

    @Column(name = "choice_order", nullable = false)
    private Integer choiceOrder;

    @Column(name = "choice_description")
    private String choiceDescription;

    private EventChoice(
        Integer gameEventId,
        String choiceCode,
        String choiceName,
        Integer choiceOrder,
        String choiceDescription
    ) {
        this.gameEventId = gameEventId;
        this.choiceCode = choiceCode;
        this.choiceName = choiceName;
        this.choiceOrder = choiceOrder;
        this.choiceDescription = choiceDescription;
    }

    public static EventChoice create(
        Integer gameEventId,
        String choiceCode,
        String choiceName,
        Integer choiceOrder,
        String choiceDescription
    ) {
        return new EventChoice(
            gameEventId,
            choiceCode,
            choiceName,
            choiceOrder,
            choiceDescription
        );
    }
}
