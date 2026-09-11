package event_registration.controller;

import event_registration.dto.response.RegistrationResponse;
import event_registration.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
