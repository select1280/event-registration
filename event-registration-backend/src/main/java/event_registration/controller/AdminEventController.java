package event_registration.controller;

import event_registration.dto.response.EventResponse;
import event_registration.dto.response.PageResponse;
import event_registration.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final EventService eventService;

    /**
     * 管理員分頁查看全部狀態的活動，包含草稿。
     */
    @GetMapping
    public ResponseEntity<PageResponse<EventResponse>> getEvents(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ){
        return ResponseEntity.ok(
                eventService.getEvents(page, size)
        );
    }

    /**
     * 管理員查看指定活動，不限制活動狀態。
     */
    @GetMapping("{id}")
    public ResponseEntity<EventResponse> getEventById(
            @PathVariable("id") Long id
    ){
        return ResponseEntity.ok(
                eventService.getEventById(id)
        );
    }
}
