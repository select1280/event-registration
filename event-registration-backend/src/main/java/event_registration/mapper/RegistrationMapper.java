package event_registration.mapper;

import event_registration.domain.Registration;
import event_registration.dto.response.RegistrationAttendeeResponse;
import event_registration.dto.response.RegistrationResponse;
import org.springframework.stereotype.Component;

@Component
public class RegistrationMapper {

    public RegistrationResponse toResponse(Registration registration) {
        return new RegistrationResponse(
                registration.getId(),
                registration.getEvent().getId(),
                registration.getEvent().getTitle(),
                registration.getStatus(),
                registration.getRegisteredAt(),
                registration.getCancelledAt()
        );
    }

    /**
     * 將報名記錄轉成管理員名單資料
     * 會讀取會員關聯，應在交易內或會員資料已載入時呼叫。
     */
    public RegistrationAttendeeResponse toAttendeeResponse(
            Registration registration
    ){
        return new RegistrationAttendeeResponse(
                registration.getId(),
                registration.getMember().getId(),
                registration.getMember().getDisplayName(),
                registration.getMember().getEmail(),
                registration.getStatus(),
                registration.getRegisteredAt(),
                registration.getCancelledAt()
        );
    }

}
