package event_registration.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Email 不可空白")
    @Email(message = "Email格式不正確")
    @Size(max = 254, message = "Email不可超過254字")
    private String email;

    @NotBlank(message = "密碼不可空白")
    @Size(min = 12, max = 128, message = "密碼長度必須介於 12 到 128 字")
    private String password;

    @NotBlank(message = "顯示名稱不可空白")
    @Size(max = 100, message = "顯示名稱不可超過100字")
    private String displayName;
}

