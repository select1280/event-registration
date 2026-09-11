package event_registration.controller;

import event_registration.dto.response.MemberResponse;
import event_registration.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken csrfToken){
        return csrfToken;
    }

    @GetMapping("/me")
    public ResponseEntity<MemberResponse> me(Principal principal){
        MemberResponse response = memberService.getMemberByEmail(
                principal.getName()
        );

        return ResponseEntity.ok(response);
    }
}
