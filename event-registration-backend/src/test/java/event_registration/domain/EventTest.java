package event_registration.domain;

import event_registration.domain.enums.EventStatus;
import event_registration.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

class EventTest {

    /**
     * 每次建立獨立的草稿活動，避免測試之間共用可變狀態。
     * 報名截止時間固定為 2026-10-01T01:00:00Z，
     * 讓發布測試能精確控制截止前、當下與截止後的情境。
     */
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

    /**
     * 驗證已發布活動不能重複發布。
     * 第二次發布應拋出 BusinessException，狀態仍維持 PUBLISHED。
     */
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

    /**
     * 驗證草稿活動在報名截止前可以發布，
     * 並將狀態從 DRAFT 轉為 PUBLISHED。
     */
    @Test
    void publish_shouldChangeDraftToPublished() {
        Event event = createDraftEvent();
        Instant now = Instant.parse("2026-10-01T00:00:00Z");

        event.publish(now);

        assertEquals(EventStatus.PUBLISHED, event.getStatus());
    }

    /**
     * 驗證目前時間剛好等於報名截止時間時，不允許發布。
     * 此測試確認截止邊界不包含相等情況，拒絕後仍維持 DRAFT。
     */
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

    /**
     * 驗證報名截止後不允許發布。
     * 發布應拋出 BusinessException，且活動仍維持 DRAFT。
     */
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

    /**
     * 驗證草稿活動可以修改全部七個可編輯欄位，
     * 且修改內容不會使活動自動變成已發布狀態。
     */
    @Test
    void updateDetails_shouldUpdateDraftEvent() {
        Event event = createDraftEvent();
        Instant deadline = Instant.parse("2026-11-01T01:00:00Z");
        Instant startsAt = Instant.parse("2026-11-01T02:00:00Z");
        Instant endsAt = Instant.parse("2026-11-01T05:00:00Z");

        event.updateDetails(
                "更新後的活動",
                "更新後的說明",
                "台中",
                30,
                deadline,
                startsAt,
                endsAt
        );

        assertEquals("更新後的活動", event.getTitle());
        assertEquals("更新後的說明", event.getDescription());
        assertEquals("台中", event.getLocation());
        assertEquals(Integer.valueOf(30), event.getCapacity());
        assertEquals(deadline, event.getRegistrationDeadline());
        assertEquals(startsAt, event.getStartsAt());
        assertEquals(endsAt, event.getEndsAt());
        assertEquals(EventStatus.DRAFT, event.getStatus());
    }

    /**
     * 驗證已發布活動不能修改內容。
     * 除了確認拋出 BusinessException，也檢查全部可編輯欄位保持原值，
     * 避免出現先修改部分資料、才檢查狀態並拋出例外的問題。
     */
    @Test
    void updateDetails_shouldRejectPublishedEvent() {
        Event event = createDraftEvent();
        event.publish(Instant.parse("2026-10-01T00:00:00Z"));

        String originalTitle = event.getTitle();
        String originalDescription = event.getDescription();
        String originalLocation = event.getLocation();
        Integer originalCapacity = event.getCapacity();
        Instant originalDeadline = event.getRegistrationDeadline();
        Instant originalStartsAt = event.getStartsAt();
        Instant originalEndsAt = event.getEndsAt();

        assertThrows(
                BusinessException.class,
                () -> event.updateDetails(
                        "不應更新的活動",
                        "不應更新的說明",
                        "高雄",
                        50,
                        Instant.parse("2026-11-01T01:00:00Z"),
                        Instant.parse("2026-11-01T02:00:00Z"),
                        Instant.parse("2026-11-01T05:00:00Z")
                )
        );

        assertEquals(originalTitle, event.getTitle());
        assertEquals(originalDescription, event.getDescription());
        assertEquals(originalLocation, event.getLocation());
        assertEquals(originalCapacity, event.getCapacity());
        assertEquals(originalDeadline, event.getRegistrationDeadline());
        assertEquals(originalStartsAt, event.getStartsAt());
        assertEquals(originalEndsAt, event.getEndsAt());
        assertEquals(EventStatus.PUBLISHED, event.getStatus());
    }

}
