package event_registration.service;

import event_registration.dto.response.PageResponse;
import event_registration.dto.response.RegistrationAttendeeResponse;
import event_registration.dto.response.RegistrationResponse;

public interface RegistrationService {

    /**
     * 以登入會員的 Email 報名指定活動，支援取消後重新報名
     */
    RegistrationResponse register(Long id, String email);

    /**
     * 取消登入會員在指定活動的報名
     */
    RegistrationResponse cancel(Long id, String email);

    /**
     * 分頁查詢登入會員自己的報名，包含有效與已取消紀錄。
     */
    PageResponse<RegistrationResponse> getMyRegistrations(
            String email,
            int page,
            int size
    );

    /**
     * 分頁查詢指定活動的報名名單，包含有效與已取消紀錄。
     */
    PageResponse<RegistrationAttendeeResponse> getEventRegistrations(
            Long eventId,
            int page,
            int size
    );
}
