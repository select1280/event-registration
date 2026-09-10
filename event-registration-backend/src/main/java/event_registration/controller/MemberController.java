package event_registration.controller;

import event_registration.dto.request.RegisterRequest;
import event_registration.dto.response.MemberResponse;
import event_registration.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<MemberResponse> register(
            @Valid @RequestBody RegisterRequest request
    ){
        MemberResponse response = memberService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }
}
