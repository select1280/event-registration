package event_registration.controller;

import event_registration.dto.response.PageResponse;
import event_registration.dto.response.RegistrationResponse;
import event_registration.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/me/registrations")
@RequiredArgsConstructor
public class MyRegistrationController {

    private final RegistrationService registrationService;

    /**
     * 查詢目前登入會員的報名，僅接受分頁參數，不接受會員ID。
     */
    @GetMapping
    public ResponseEntity<PageResponse<RegistrationResponse>> getMyRegistrations(
            Principal principal,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ){
        PageResponse<RegistrationResponse> response =
                registrationService.getMyRegistrations(
                        principal.getName(),
                        page,
                        size
                );

        return ResponseEntity.ok(response);
    }
}
