package event_registration.mapper;

import event_registration.domain.Event;
import event_registration.dto.response.EventResponse;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    public EventResponse toResponse(Event event){
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getLocation(),
                event.getCapacity(),
                event.getStatus(),
                event.getRegistrationDeadline(),
                event.getStartsAt(),
                event.getEndsAt(),
                event.getCreatedAt()
        );
    }
}
