package event_registration.service;

import event_registration.dto.response.RegistrationResponse;

public interface RegistrationService {

    /**
     * 以登入會員的 Email 報名指定活動，支援取消後重新報名
     */
    RegistrationResponse register(Long id, String email);
}
