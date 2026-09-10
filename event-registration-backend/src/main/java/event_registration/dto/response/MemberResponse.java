package event_registration.dto.response;

import event_registration.domain.enums.MemberRole;

import java.time.Instant;

public record MemberResponse (
    Long id,
    String email,
    String displayName,
    MemberRole role,
    Instant createdAt
) {
}
