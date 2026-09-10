package event_registration.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordConfigTest {

    private final PasswordEncoder passwordEncoder =
            new PasswordConfig().passwordEncoder();

    /**
     * 驗證密碼儲存值不是明文，
     * 正確密碼可以通過驗證，錯誤密碼則被拒絕。
     */
    @Test
    void passwordEncoder_shouldVerifyPassword() {
        String rawPassword = "PracticeOnly!2026";

        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(
                rawPassword,
                encodedPassword
        ));
        assertFalse(passwordEncoder.matches(
                "WrongPassword!2026",
                encodedPassword
        ));
    }

    /**
     * 驗證相同密碼因隨機 salt 產生不同雜湊，
     * 但兩個結果都能驗證原始密碼。
     */
    @Test
    void passwordEncoder_shouldUseRandomSalt() {
        String rawPassword = "PracticeOnly!2026";

        String firstHash = passwordEncoder.encode(rawPassword);
        String secondHash = passwordEncoder.encode(rawPassword);

        assertNotEquals(firstHash, secondHash);
        assertTrue(passwordEncoder.matches(rawPassword, firstHash));
        assertTrue(passwordEncoder.matches(rawPassword, secondHash));
    }
}