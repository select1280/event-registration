package event_registration.service;

import event_registration.dto.request.RegisterRequest;
import event_registration.dto.response.MemberResponse;

public interface MemberService {

    MemberResponse register(RegisterRequest request);
}
