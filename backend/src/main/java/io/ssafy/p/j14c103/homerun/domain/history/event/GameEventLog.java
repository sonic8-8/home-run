package io.ssafy.p.j14c103.homerun.domain.history.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "game_event_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameEventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_event_log_id")
    private Integer gameEventLogId;

    @Column(name = "game_session_id", nullable = false)
    private Long gameSessionId;

    @Column(name = "turn_number", nullable = false)
    private Integer turnNumber;

    @Column(name = "game_event_id", nullable = false)
    private Integer gameEventId;

    @Column(name = "event_choice_id")
    private Integer eventChoiceId;

    @Column(name = "selected_choice_code")
    private String selectedChoiceCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result_effects")
    private Map<String, Object> resultEffects;

    @Column(name = "result_summary")
    private String resultSummary;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    private GameEventLog(
        Long gameSessionId,
        Integer turnNumber,
        Integer gameEventId,
        Integer eventChoiceId,
        String selectedChoiceCode,
        Map<String, Object> resultEffects,
        String resultSummary,
        LocalDateTime resolvedAt
    ) {
        this.gameSessionId = gameSessionId;
        this.turnNumber = turnNumber;
        this.gameEventId = gameEventId;
        this.eventChoiceId = eventChoiceId;
        this.selectedChoiceCode = selectedChoiceCode;
        this.resultEffects = resultEffects;
        this.resultSummary = resultSummary;
        this.resolvedAt = resolvedAt;
    }

    public static GameEventLog create(
        Long gameSessionId,
        Integer turnNumber,
        Integer gameEventId,
        Integer eventChoiceId,
        String selectedChoiceCode,
        Map<String, Object> resultEffects,
        String resultSummary,
        LocalDateTime resolvedAt
    ) {
        return new GameEventLog(
            gameSessionId,
            turnNumber,
            gameEventId,
            eventChoiceId,
            selectedChoiceCode,
            resultEffects,
            resultSummary,
            resolvedAt
        );
    }
}
