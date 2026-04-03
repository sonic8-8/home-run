package io.ssafy.p.j14c103.homerun.domain.world.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "game_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_event_id")
    private Integer gameEventId;

    @Column(name = "event_type_code", nullable = false)
    private String eventTypeCode;

    @Column(name = "event_code", nullable = false, unique = true)
    private String eventCode;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_presentation_type", nullable = false)
    private EventPresentationType eventPresentationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_trigger_type", nullable = false)
    private EventTriggerType eventTriggerType;

    @Column(name = "event_trigger_value")
    private BigDecimal eventTriggerValue;

    @Column(name = "choice_required_yn", nullable = false)
    private boolean choiceRequiredYn;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "sender_name")
    private String senderName;

    @Column(name = "receiver_name")
    private String receiverName;

    @Column(name = "description")
    private String description;

    @Column(name = "active_yn", nullable = false)
    private boolean activeYn;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private GameEvent(
        String eventTypeCode,
        String eventCode,
        String eventName,
        EventPresentationType eventPresentationType,
        EventTriggerType eventTriggerType,
        BigDecimal eventTriggerValue,
        boolean choiceRequiredYn,
        String imageUrl,
        String senderName,
        String receiverName,
        String description,
        boolean activeYn
    ) {
        this.eventTypeCode = eventTypeCode;
        this.eventCode = eventCode;
        this.eventName = eventName;
        this.eventPresentationType = eventPresentationType;
        this.eventTriggerType = eventTriggerType;
        this.eventTriggerValue = eventTriggerValue;
        this.choiceRequiredYn = choiceRequiredYn;
        this.imageUrl = imageUrl;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.description = description;
        this.activeYn = activeYn;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public static GameEvent create(
        String eventTypeCode,
        String eventCode,
        String eventName,
        EventPresentationType eventPresentationType,
        EventTriggerType eventTriggerType,
        BigDecimal eventTriggerValue,
        boolean choiceRequiredYn,
        String imageUrl,
        String senderName,
        String receiverName,
        String description,
        boolean activeYn
    ) {
        return new GameEvent(
            eventTypeCode,
            eventCode,
            eventName,
            eventPresentationType,
            eventTriggerType,
            eventTriggerValue,
            choiceRequiredYn,
            imageUrl,
            senderName,
            receiverName,
            description,
            activeYn
        );
    }
}
