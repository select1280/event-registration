package event_registration.controller;

import event_registration.dto.request.EventRequest;
import event_registration.dto.response.EventResponse;
import event_registration.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody EventRequest request
            ){
            EventResponse response = eventService.createEvent(request);

            URI location = URI.create("/api/events/" + response.id());

            return ResponseEntity.created(location).body(response);
    }
}
