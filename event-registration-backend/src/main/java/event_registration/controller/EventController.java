package event_registration.controller;

import event_registration.dto.request.EventRequest;
import event_registration.dto.response.EventAvailabilityResponse;
import event_registration.dto.response.EventResponse;
import event_registration.dto.response.PageResponse;
import event_registration.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.connector.Response;
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

    /** 分頁列出已發布活鄧 */
    @GetMapping
    public ResponseEntity<PageResponse<EventResponse>> getEvents(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ){
        PageResponse<EventResponse> response =
                eventService.getPublishedEvents(keyword, page, size);

        return ResponseEntity.ok(response);
    }

    /** 查詢已發布活動，草稿與不存在的活動都回傳 */
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(
            @PathVariable("id") Long id
    ){
        EventResponse resposne = eventService.getPublishedEventById(id);

        return ResponseEntity.ok(resposne);
    }

    /** 查詢已發布活動目前的報名人數與剩餘名額 */
    @GetMapping("{id}/availability")
    public ResponseEntity<EventAvailabilityResponse> getAvailability(
            @PathVariable("id") Long id
    ){
        EventAvailabilityResponse response = eventService.getAvailability(id);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<EventResponse> publishEvent(
            @PathVariable("id") Long id
    ){
        EventResponse response = eventService.publishEvent(id);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable("id") Long id,
            @Valid @RequestBody EventRequest request
    ){
        EventResponse response = eventService.updateEvent(id, request);

        return ResponseEntity.ok(response);
    }

}
