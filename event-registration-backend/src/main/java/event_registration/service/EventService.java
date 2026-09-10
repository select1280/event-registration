package event_registration.service;

import event_registration.dto.request.EventRequest;
import event_registration.dto.response.EventResponse;
import event_registration.dto.response.PageResponse;

public interface EventService {

    EventResponse createEvent(EventRequest eventRequest);

    EventResponse getEventById(Long id);

    PageResponse<EventResponse> getEvents(int page, int size);

    EventResponse publishEvent(Long id);

    EventResponse updateEvent(Long id, EventRequest request);
}
