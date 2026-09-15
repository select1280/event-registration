package event_registration.dto.response;

import event_registration.domain.enums.RegistrationStatus;

import java.time.Instant;

/**
 * 管理員查看活動報名名單使用，包含報名者資訊與報名狀態
 */
public record RegistrationAttendeeResponse(
    Long registrationAttendeeResponse,
    Long memberId,
    String displayName,
    String email,
    RegistrationStatus status,
    Instant registeredAt,
    Instant cancelledAt
   ) {

}
