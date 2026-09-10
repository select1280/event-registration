package event_registration.controller;

import event_registration.dto.request.EventRequest;
import event_registration.dto.response.EventResponse;
import event_registration.dto.response.PageResponse;
import event_registration.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(
            @PathVariable("id") Long id
    ){
        EventResponse resposne = eventService.getEventById(id);

        return ResponseEntity.ok(resposne);
    }

    @GetMapping
    public ResponseEntity<PageResponse<EventResponse>> getEvents(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ){
        PageResponse<EventResponse> response =
                eventService.getEvents(page, size);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<EventResponse> publishEvent(
            @PathVariable("id") Long id
    ){
        EventResponse response = eventService.publishEvent(id);

        return ResponseEntity.ok(response);
    }

}
