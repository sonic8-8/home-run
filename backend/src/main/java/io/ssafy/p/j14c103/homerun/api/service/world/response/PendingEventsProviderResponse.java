package io.ssafy.p.j14c103.homerun.api.service.world.response;

import io.ssafy.p.j14c103.homerun.domain.world.event.EventPresentationType;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;

@Getter
public class PendingEventsProviderResponse {

    private final List<PendingEventItem> events;

    private PendingEventsProviderResponse(final List<PendingEventItem> events) {
        this.events = List.copyOf(events);
    }

    public static PendingEventsProviderResponse of(final List<PendingEventItem> events) {
        return new PendingEventsProviderResponse(events);
    }

    public static PendingEventsProviderResponse empty() {
        return new PendingEventsProviderResponse(List.of());
    }

    @Getter
    public static class PendingEventItem {

        private final Integer eventId;
        private final EventPresentationType type;
        private final String title;
        private final String description;
        private final String imageUrl;
        private final List<PendingEventChoiceItem> choices;
        private final String sender;
        private final String receiver;
        private final LocalDate date;
        private final Integer offeredSalary;
        private final Integer currentSalary;

        private PendingEventItem(
            final Integer eventId,
            final EventPresentationType type,
            final String title,
            final String description,
            final String imageUrl,
            final List<PendingEventChoiceItem> choices,
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
            this.choices = List.copyOf(choices);
            this.sender = sender;
            this.receiver = receiver;
            this.date = date;
            this.offeredSalary = offeredSalary;
            this.currentSalary = currentSalary;
        }

        public static PendingEventItem of(
            final Integer eventId,
            final EventPresentationType type,
            final String title,
            final String description,
            final String imageUrl,
            final List<PendingEventChoiceItem> choices,
            final String sender,
            final String receiver,
            final LocalDate date,
            final Integer offeredSalary,
            final Integer currentSalary
        ) {
            return new PendingEventItem(
                eventId,
                type,
                title,
                description,
                imageUrl,
                choices,
                sender,
                receiver,
                date,
                offeredSalary,
                currentSalary
            );
        }
    }

    @Getter
    public static class PendingEventChoiceItem {

        private final Integer choiceId;
        private final String choiceCode;
        private final String choiceName;
        private final String description;

        private PendingEventChoiceItem(
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

        public static PendingEventChoiceItem of(
            final Integer choiceId,
            final String choiceCode,
            final String choiceName,
            final String description
        ) {
            return new PendingEventChoiceItem(choiceId, choiceCode, choiceName, description);
        }
    }
}
