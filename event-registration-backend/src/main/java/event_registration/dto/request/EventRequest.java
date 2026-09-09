package event_registration.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class EventRequest {

    @NotBlank(message = "活動名稱不可空白")
    @Size(max = 120, message = "活動名稱不可超過120字")
    private String title;

    @NotBlank(message = "活動說明不可空白")
    private String description;

    @NotBlank(message = "活動地點不可空白")
    @Size(max = 255, message = "活動地點不可超過255字")
    private String location;

    @NotNull(message = "活動名額不可為空")
    @Positive(message = "活動名額必須大於0")
    private Integer capacity;

    @NotNull(message = "報名截止時間不可為空")
    private Instant registrationDeadline;

    @NotNull(message = "活動開始時間不可為空")
    private Instant startsAt;

    @NotNull(message = "活動結束時間不可為空")
    private Instant endsAt;
}
