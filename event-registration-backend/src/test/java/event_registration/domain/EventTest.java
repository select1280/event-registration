package event_registration.domain;

import event_registration.domain.enums.EventStatus;
import event_registration.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

class EventTest {

     private Event createDraftEvent(){
         return new Event(
                "JAVA工作坊",
                "練習 Spring Boot",
                "台北",
                 20,
                 Instant.parse("2026-10-01T01:00:00Z"),
                 Instant.parse("2026-10-01T02:00:00Z"),
                 Instant.parse("2026-10-01T05:00:00Z")
         );
     }

     @Test
     void publish_shouldRejectAlreadyPublishedEvent(){
         Event event = createDraftEvent();
         Instant now = Instant.parse("2026-10-01T00:00:00Z");
         event.publish(now);

         assertThrows(
                 BusinessException.class,
                 () -> event.publish(now)
         );

         assertEquals(EventStatus.PUBLISHED, event.getStatus());
     }

    @Test
    void publish_shouldChangeDraftToPublished() {
        Event event = createDraftEvent();
        Instant now = Instant.parse("2026-10-01T00:00:00Z");

        event.publish(now);

        assertEquals(EventStatus.PUBLISHED, event.getStatus());
    }

    @Test
    void publish_shouldRejectAtRegistrationDeadline() {
        Event event = createDraftEvent();
        Instant now = Instant.parse("2026-10-01T01:00:00Z");

        assertThrows(
                BusinessException.class,
                () -> event.publish(now)
        );

        assertEquals(EventStatus.DRAFT, event.getStatus());
    }

    @Test
    void publish_shouldRejectAfterRegistrationDeadline() {
        Event event = createDraftEvent();
        Instant now = Instant.parse("2026-10-01T01:00:01Z");

        assertThrows(
                BusinessException.class,
                () -> event.publish(now)
        );

        assertEquals(EventStatus.DRAFT, event.getStatus());
    }

}
