package event_registration.controller;

import event_registration.dto.response.PageResponse;
import event_registration.dto.response.RegistrationAttendeeResponse;
import event_registration.dto.response.RegistrationResponse;
import event_registration.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    /**
     * 以目前登入會員的身分報名活動，不接受前端指定會員帳號。
     */
    @PostMapping("/{eventId}/registrations")
    public ResponseEntity<RegistrationResponse> register(
            @PathVariable("eventId") Long eventId,
            Principal principal
    ){
        RegistrationResponse response = registrationService.register(
                eventId,
                principal.getName()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 取消目前登入會員在指定活動的報名，不接受前端指定會員身分。
     */
    @PostMapping("/{eventId}/registrations/cancel")
    public ResponseEntity<RegistrationResponse> cancel(
            @PathVariable("eventId") Long eventId,
            Principal principal
    ){
        RegistrationResponse response = registrationService.cancel(
                eventId,
                principal.getName()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 分頁查詢指定活動的報名名單。
     * 此端點由 SecurityConfig 限制只有管理員可以存取。
     */
    @GetMapping("/{eventId}/registrations")
    public ResponseEntity<PageResponse<RegistrationAttendeeResponse>> getEventRegistrations(
            @PathVariable("eventId") Long eventId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10")int size
    ){
        PageResponse<RegistrationAttendeeResponse> response =
                registrationService.getEventRegistrations(
                        eventId,
                        page,
                        size
                );

        return ResponseEntity.ok(response);
    }
}
