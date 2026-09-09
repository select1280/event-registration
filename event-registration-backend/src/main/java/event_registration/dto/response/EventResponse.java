package event_registration.dto.response;

import event_registration.domain.enums.EventStatus;

import java.time.Instant;

public record EventResponse(
    Long id,
    String title,
    String description,
    String location,
    Integer capacity,
    EventStatus status,
    Instant registrationDeadline,
    Instant startsAt,
    Instant endsAt,
    Instant createdAt
) {
}
