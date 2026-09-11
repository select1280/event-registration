package event_registration.dto.response;

import event_registration.domain.enums.RegistrationStatus;

import java.time.Instant;

/**
 * 報名結果，包含活動資訊與報名狀態
 */
public record RegistrationResponse(
    Long id,
    Long eventId,
    String eventTitle,
    RegistrationStatus status,
    Instant registeredAt,
    Instant cancelledAt
){
}
