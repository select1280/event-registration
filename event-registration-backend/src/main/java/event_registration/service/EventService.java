package event_registration.service;

import event_registration.dto.request.EventRequest;
import event_registration.dto.response.EventResponse;

public interface EventService {

    EventResponse createEvent(EventRequest eventRequest);
}
