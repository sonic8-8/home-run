package io.ssafy.p.j14c103.homerun.api.service.game.events.response;

import io.ssafy.p.j14c103.homerun.api.service.world.response.PendingEventsProviderResponse;
import io.ssafy.p.j14c103.homerun.domain.world.event.EventPresentationType;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PendingEventsResponse {

    private final List<PendingEventResponse> events;

    @Builder(access = AccessLevel.PRIVATE)
    private PendingEventsResponse(final List<PendingEventResponse> events) {
        this.events = List.copyOf(events);
    }

    public static PendingEventsResponse of(final List<PendingEventResponse> events) {
        return PendingEventsResponse.builder()
            .events(events)
            .build();
    }

    public static PendingEventsResponse from(final PendingEventsProviderResponse response) {
        return PendingEventsResponse.of(
            response.getEvents().stream()
                .map(PendingEventResponse::from)
                .toList()
        );
    }

    @Getter
    public static class PendingEventResponse {

        private final Integer eventId;
        private final EventPresentationType type;
        private final String title;
        private final String description;
        private final String imageUrl;
        private final List<PendingEventChoiceResponse> choices;
        private final String sender;
        private final String receiver;
        private final LocalDate date;
        private final Integer offeredSalary;
        private final Integer currentSalary;

        @Builder(access = AccessLevel.PRIVATE)
        private PendingEventResponse(
            final Integer eventId,
            final EventPresentationType type,
            final String title,
            final String description,
            final String imageUrl,
            final List<PendingEventChoiceResponse> choices,
            final String sender,
            final String receiver,
            final LocalDate date,
            final Integer offeredSalary,
            final Integer currentSalary
        ) {
            this.eventId = eventId;
            this.type = type;
            this.title = title;
            this.description = description;
            this.imageUrl = imageUrl;
            this.choices = choices == null ? null : List.copyOf(choices);
            this.sender = sender;
            this.receiver = receiver;
            this.date = date;
            this.offeredSalary = offeredSalary;
            this.currentSalary = currentSalary;
        }

        public static PendingEventResponse of(
            final Integer eventId,
            final EventPresentationType type,
            final String title,
            final String description,
            final String imageUrl,
            final List<PendingEventChoiceResponse> choices,
            final String sender,
            final String receiver,
            final LocalDate date,
            final Integer offeredSalary,
            final Integer currentSalary
        ) {
            return PendingEventResponse.builder()
                .eventId(eventId)
                .type(type)
                .title(title)
                .description(description)
                .imageUrl(imageUrl)
                .choices(choices)
                .sender(sender)
                .receiver(receiver)
                .date(date)
                .offeredSalary(offeredSalary)
                .currentSalary(currentSalary)
                .build();
        }

        public static PendingEventResponse from(
            final PendingEventsProviderResponse.PendingEventItem event
        ) {
            final List<PendingEventChoiceResponse> choices = event.getChoices() == null
                ? null
                : event.getChoices().stream()
                    .map(PendingEventChoiceResponse::from)
                    .toList();

            return PendingEventResponse.of(
                event.getEventId(),
                event.getType(),
                event.getTitle(),
                event.getDescription(),
                event.getImageUrl(),
                choices,
                event.getSender(),
                event.getReceiver(),
                event.getDate(),
                event.getOfferedSalary(),
                event.getCurrentSalary()
            );
        }
    }

    @Getter
    public static class PendingEventChoiceResponse {

        private final Integer choiceId;
        private final String choiceCode;
        private final String choiceName;
        private final String description;

        @Builder(access = AccessLevel.PRIVATE)
        private PendingEventChoiceResponse(
            final Integer choiceId,
            final String choiceCode,
            final String choiceName,
            final String description
        ) {
            this.choiceId = choiceId;
            this.choiceCode = choiceCode;
            this.choiceName = choiceName;
            this.description = description;
        }

        public static PendingEventChoiceResponse of(
            final Integer choiceId,
            final String choiceCode,
            final String choiceName,
            final String description
        ) {
            return PendingEventChoiceResponse.builder()
                .choiceId(choiceId)
                .choiceCode(choiceCode)
                .choiceName(choiceName)
                .description(description)
                .build();
        }

        public static PendingEventChoiceResponse from(
            final PendingEventsProviderResponse.PendingEventChoiceItem choice
        ) {
            return PendingEventChoiceResponse.of(
                choice.getChoiceId(),
                choice.getChoiceCode(),
                choice.getChoiceName(),
                choice.getDescription()
            );
        }
    }
}
