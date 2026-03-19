package io.ssafy.p.j14c103.homerun.domain.world.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "game_pending_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GamePendingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_pending_event_id")
    private Integer gamePendingEventId;

    @Column(name = "game_session_id", nullable = false)
    private Integer gameSessionId;

    @Column(name = "turn_number", nullable = false)
    private Integer turnNumber;

    @Column(name = "game_event_id", nullable = false)
    private Integer gameEventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_presentation_type", nullable = false)
    private EventPresentationType eventPresentationType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload")
    private Map<String, Object> payload;

    @Column(name = "resolved_yn", nullable = false)
    private boolean resolvedYn;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private GamePendingEvent(
        Integer gameSessionId,
        Integer turnNumber,
        Integer gameEventId,
        EventPresentationType eventPresentationType,
        Map<String, Object> payload,
        boolean resolvedYn,
        LocalDateTime createdAt
    ) {
        this.gameSessionId = gameSessionId;
        this.turnNumber = turnNumber;
        this.gameEventId = gameEventId;
        this.eventPresentationType = eventPresentationType;
        this.payload = payload;
        this.resolvedYn = resolvedYn;
        this.createdAt = createdAt;
    }

    public static GamePendingEvent create(
        Integer gameSessionId,
        Integer turnNumber,
        Integer gameEventId,
        EventPresentationType eventPresentationType,
        Map<String, Object> payload,
        boolean resolvedYn,
        LocalDateTime createdAt
    ) {
        return new GamePendingEvent(
            gameSessionId,
            turnNumber,
            gameEventId,
            eventPresentationType,
            payload,
            resolvedYn,
            createdAt
        );
    }
}
