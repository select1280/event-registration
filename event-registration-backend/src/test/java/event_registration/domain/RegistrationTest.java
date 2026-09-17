package event_registration.domain;

import event_registration.domain.enums.RegistrationStatus;
import event_registration.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationTest {

    /**
     * 建立獨立的有效報名，供測試使用，不連接資料庫。
     */
    private Registration createRegistration() {
        Member member = new Member(
                "test@example.com",
                "unused-test-hash",
                "測試會員"
        );

        Event event = new Event(
                "Java 工作坊",
                "練習報名流程",
                "台北",
                10,
                Instant.parse("2026-10-01T01:00:00Z"),
                Instant.parse("2026-10-01T02:00:00Z"),
                Instant.parse("2026-10-01T05:00:00Z")
        );

        return new Registration(
                member,
                event,
                Instant.parse("2026-09-20T00:00:00Z")
        );
    }

    /**
     * 取消後保留原報名時間，更新狀態並記錄取消時間。
     */
    @Test
    void cancel_shouldCancelRegisteredRegistration() {
        Registration registration = createRegistration();
        Instant originalRegisteredAt = registration.getRegisteredAt();
        Instant cancelledAt = Instant.parse("2026-09-21T00:00:00Z");

        registration.cancel(cancelledAt);

        assertEquals(
                RegistrationStatus.CANCELLED,
                registration.getStatus()
        );
        assertEquals(cancelledAt, registration.getCancelledAt());
        assertEquals(
                originalRegisteredAt,
                registration.getRegisteredAt()
        );
    }

    /**
     * 重複取消應被拒絕，而且不能覆蓋第一次取消的時間。
     */
    @Test
    void cancel_shouldRejectAlreadyCancelledRegistration() {
        Registration registration = createRegistration();
        Instant firstCancelledAt = Instant.parse("2026-09-21T00:00:00Z");
        registration.cancel(firstCancelledAt);

        assertThrows(
                BusinessException.class,
                () -> registration.cancel(
                        Instant.parse("2026-09-22T00:00:00Z")
                )
        );

        assertEquals(
                RegistrationStatus.CANCELLED,
                registration.getStatus()
        );
        assertEquals(firstCancelledAt, registration.getCancelledAt());
    }

    /**
     * 重新報名後恢復有效狀態、更新報名時間，並清除取消時間
     */
    @Test
    void registerAgain_shouldRestoreCanaelledRegistration(){
        Registration registration = createRegistration();
        registration.cancel(Instant.parse("2026-09-21T00:00:00Z"));
        Instant registeredAgainAt = Instant.parse("2026-09-22T00:00:00Z");

        registration.registerAgain(registeredAgainAt);

        assertEquals(
                RegistrationStatus.REGISTERED,
                registration.getStatus()
        );
        assertEquals(registeredAgainAt, registration.getRegisteredAt());
        assertNull(registration.getCancelledAt());
    }

    /**
     * 已有效報名時拒絕再次報名，且不能覆蓋原本的報名時間。
     */
    @Test
    void registerAgain_shouldRejectRegisteredRegistration(){
        Registration registration = createRegistration();
        Instant originalRegisteredAt = registration.getRegisteredAt();

        assertThrows(
                BusinessException.class,
                () -> registration.registerAgain(
                        Instant.parse("2026-09-22T00:00:00Z")
                )
        );

        assertEquals(
                RegistrationStatus.REGISTERED,
                registration.getStatus()
        );
        assertEquals(originalRegisteredAt, registration.getRegisteredAt());
        assertNull(registration.getCancelledAt());
    }
}