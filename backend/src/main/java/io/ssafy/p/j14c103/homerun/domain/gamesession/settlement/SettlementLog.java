package io.ssafy.p.j14c103.homerun.domain.gamesession.settlement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "settlement_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "settlement_log_id")
    private Long settlementLogId;

    @Column(name = "game_session_id", nullable = false)
    private Long gameSessionId;

    @Column(name = "turn_number", nullable = false)
    private Integer turnNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_phase_type", length = 50)
    private SettlementPhaseType settlementPhaseType;

    @Column(name = "description")
    private String description;

    @Column(name = "cash_change_amount")
    private Integer cashChangeAmount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "stat_changes")
    private Map<String, Integer> statChanges;

    private SettlementLog(
        final Long gameSessionId,
        final Integer turnNumber,
        final SettlementPhaseType settlementPhaseType,
        final String description,
        final Integer cashChangeAmount,
        final Map<String, Integer> statChanges
    ) {
        this.gameSessionId = gameSessionId;
        this.turnNumber = turnNumber;
        this.settlementPhaseType = settlementPhaseType;
        this.description = description;
        this.cashChangeAmount = cashChangeAmount;
        this.statChanges = statChanges == null ? Map.of() : Map.copyOf(statChanges);
    }

    public static SettlementLog create(
        final Long gameSessionId,
        final Integer turnNumber,
        final SettlementPhaseType settlementPhaseType,
        final String description,
        final Integer cashChangeAmount,
        final Map<String, Integer> statChanges
    ) {
        return new SettlementLog(
            gameSessionId,
            turnNumber,
            settlementPhaseType,
            description,
            cashChangeAmount,
            statChanges
        );
    }
}
