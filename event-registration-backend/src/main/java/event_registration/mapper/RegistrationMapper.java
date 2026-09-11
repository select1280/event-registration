package event_registration.mapper;

import event_registration.domain.Registration;
import event_registration.dto.response.RegistrationResponse;
import org.springframework.stereotype.Component;

@Component
public class RegistrationMapper {

    public RegistrationResponse toResponse(Registration registration) {
        return new RegistrationResponse(
                registration.getId(),
                registration.getEvent().getId(),
                registration.getEvent().getTitle(),
                registration.getStatus(),
                registration.getRegisteredAt(),
                registration.getCancelledAt()
        );
    }

}
