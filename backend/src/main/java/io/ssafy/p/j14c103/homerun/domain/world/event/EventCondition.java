package io.ssafy.p.j14c103.homerun.domain.world.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "event_conditions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_condition_id")
    private Integer eventConditionId;

    @Column(name = "game_event_id", nullable = false)
    private Integer gameEventId;

    @Column(name = "condition_group_number")
    private Integer conditionGroupNumber;

    @Column(name = "condition_order", nullable = false)
    private Integer conditionOrder;

    @Column(name = "condition_type", nullable = false)
    private String conditionType;

    @Column(name = "target_table_name")
    private String targetTableName;

    @Column(name = "target_column_name")
    private String targetColumnName;

    @Column(name = "comparison_operator")
    private String comparisonOperator;

    @Column(name = "criteria_text_value")
    private String criteriaTextValue;

    @Column(name = "criteria_number_value_1")
    private BigDecimal criteriaNumberValue1;

    @Column(name = "criteria_number_value_2")
    private BigDecimal criteriaNumberValue2;

    @Column(name = "logical_operator_type")
    private String logicalOperatorType;

    private EventCondition(
        Integer gameEventId,
        Integer conditionGroupNumber,
        Integer conditionOrder,
        String conditionType,
        String targetTableName,
        String targetColumnName,
        String comparisonOperator,
        String criteriaTextValue,
        BigDecimal criteriaNumberValue1,
        BigDecimal criteriaNumberValue2,
        String logicalOperatorType
    ) {
        this.gameEventId = gameEventId;
        this.conditionGroupNumber = conditionGroupNumber;
        this.conditionOrder = conditionOrder;
        this.conditionType = conditionType;
        this.targetTableName = targetTableName;
        this.targetColumnName = targetColumnName;
        this.comparisonOperator = comparisonOperator;
        this.criteriaTextValue = criteriaTextValue;
        this.criteriaNumberValue1 = criteriaNumberValue1;
        this.criteriaNumberValue2 = criteriaNumberValue2;
        this.logicalOperatorType = logicalOperatorType;
    }

    public static EventCondition create(
        Integer gameEventId,
        Integer conditionGroupNumber,
        Integer conditionOrder,
        String conditionType,
        String targetTableName,
        String targetColumnName,
        String comparisonOperator,
        String criteriaTextValue,
        BigDecimal criteriaNumberValue1,
        BigDecimal criteriaNumberValue2,
        String logicalOperatorType
    ) {
        return new EventCondition(
            gameEventId,
            conditionGroupNumber,
            conditionOrder,
            conditionType,
            targetTableName,
            targetColumnName,
            comparisonOperator,
            criteriaTextValue,
            criteriaNumberValue1,
            criteriaNumberValue2,
            logicalOperatorType
        );
    }
}
