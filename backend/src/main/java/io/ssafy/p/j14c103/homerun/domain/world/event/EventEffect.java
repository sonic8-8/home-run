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
@Table(name = "event_effects")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventEffect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_effect_id")
    private Integer eventEffectId;

    @Column(name = "game_event_id", nullable = false)
    private Integer gameEventId;

    @Column(name = "event_choice_id")
    private Integer eventChoiceId;

    @Column(name = "effect_order", nullable = false)
    private Integer effectOrder;

    @Column(name = "application_timing_type", nullable = false)
    private String applicationTimingType;

    @Column(name = "target_table_name")
    private String targetTableName;

    @Column(name = "target_column_name")
    private String targetColumnName;

    @Column(name = "operation_type")
    private String operationType;

    @Column(name = "base_number_value")
    private Integer baseNumberValue;

    @Column(name = "min_number_value")
    private Integer minNumberValue;

    @Column(name = "max_number_value")
    private Integer maxNumberValue;

    @Column(name = "base_text_value")
    private String baseTextValue;

    @Column(name = "duration_turns")
    private Integer durationTurns;

    @Column(name = "note")
    private String note;

    private EventEffect(
        Integer gameEventId,
        Integer eventChoiceId,
        Integer effectOrder,
        String applicationTimingType,
        String targetTableName,
        String targetColumnName,
        String operationType,
        Integer baseNumberValue,
        Integer minNumberValue,
        Integer maxNumberValue,
        String baseTextValue,
        Integer durationTurns,
        String note
    ) {
        this.gameEventId = gameEventId;
        this.eventChoiceId = eventChoiceId;
        this.effectOrder = effectOrder;
        this.applicationTimingType = applicationTimingType;
        this.targetTableName = targetTableName;
        this.targetColumnName = targetColumnName;
        this.operationType = operationType;
        this.baseNumberValue = baseNumberValue;
        this.minNumberValue = minNumberValue;
        this.maxNumberValue = maxNumberValue;
        this.baseTextValue = baseTextValue;
        this.durationTurns = durationTurns;
        this.note = note;
    }

    public static EventEffect create(
        Integer gameEventId,
        Integer eventChoiceId,
        Integer effectOrder,
        String applicationTimingType,
        String targetTableName,
        String targetColumnName,
        String operationType,
        Integer baseNumberValue,
        Integer minNumberValue,
        Integer maxNumberValue,
        String baseTextValue,
        Integer durationTurns,
        String note
    ) {
        return new EventEffect(
            gameEventId,
            eventChoiceId,
            effectOrder,
            applicationTimingType,
            targetTableName,
            targetColumnName,
            operationType,
            baseNumberValue,
            minNumberValue,
            maxNumberValue,
            baseTextValue,
            durationTurns,
            note
        );
    }
}
