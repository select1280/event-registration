package event_registration.controller;

import event_registration.config.TestDatabaseConfig;
import event_registration.domain.Event;
import event_registration.domain.enums.EventStatus;
import event_registration.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestDatabaseConfig.class)
class RegistrationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    /**
     * 未登入者不可查看活動報名名單，應在安全性檢查時收到 401。
     */
    @Test
    void getEventRegistrations_shouldRejectAnonymousUser() throws Exception {
        mockMvc.perform(
                get("/api/events/1/registrations")
        ).andExpect(status().isUnauthorized());
    }

    /**
     * 已登入的一般會員沒有管理員權限，不可查看活動報名名單。
     */
    @Test
    @WithMockUser(
            username = "member@example.com",
            roles = "MEMBER"
    )
    void getEventRegistrations_shouldRejectMember() throws Exception{
        mockMvc.perform(
                get("/api/events/1/registrations")
        ).andExpect(status().isForbidden());
    }

    /**
     * 管理員可以查詢活動報名名單。
     * 活動存在但沒人報名時，應回傳 200 與空列表。
     */
    @Test
    @WithMockUser(
            username = "admin@example.com",
            roles = "ADMIN"
    )
    void getEventRegistrations_shouldAllowAdmin() throws Exception{
        Instant now = Instant.now();

        Event event = new Event(
                "管理員名單測試",
                "驗證管理員查詢權限",
                "線上",
                10,
                now.plus(1, ChronoUnit.DAYS),
                now.plus(2, ChronoUnit.DAYS),
                now.plus(2, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS)
        );

        Long eventId = eventRepository.save(event).getId();

        mockMvc.perform(
                get("/api/events/{eventId}/registrations", eventId)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    /**
     * 一般會員即使帶有有效 CSRF token，也不能取消活動。
     * 請求被拒絕後，活動應維持已發布狀態。
     */
    @Test
    @WithMockUser(
            username = "member@example.com",
            roles = "MEMBER"
    )
    void cancelEvent_shouldRejectMember() throws Exception{
        Instant now = Instant.now();

        Event event = new Event(
                "取消活動權限測試",
                "驗證一般會員不可取消活動",
                "線上",
                10,
                now.plus(1, ChronoUnit.DAYS),
                now.plus(2, ChronoUnit.DAYS),
                now.plus(2, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS)
        );

        event.publish(now);
        Long eventId = eventRepository.save(event).getId();

        //帶入有效 CSRF token，避免因缺少 token 而得到 403。
        mockMvc.perform(
                post("/api/admin/events/{eventId}/cancel", eventId)
                        .with(csrf())
        ).andExpect(status().isForbidden());

        //重新查詢資料庫，確認被拒絕的操作沒有取消活動。
        Event savedEvent = eventRepository.findById(eventId).orElseThrow();

        assertEquals(EventStatus.PUBLISHED, savedEvent.getStatus());
    }

    /**
     * 管理員帶有有效 CSRF token 時可以取消活動
     * 回應與資料庫中的活動狀態都應為 CANCELLED
     */
    @Test
    @WithMockUser(
            username = "admin@xeample.com",
            roles = "ADMIN"
    )
    void cancelEvent_shouldAllowAdmin() throws Exception{
            Instant now = Instant.now();

            Event event = new Event(
                    "管理員取消活動測試",
                    "驗管理員可取消活動",
                    "縣上",
                    10,
                    now.plus(1, ChronoUnit.DAYS),
                    now.plus(2, ChronoUnit.DAYS),
                    now.plus(2, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS)
            );

            event.publish(now);
            Long eventId = eventRepository.save(event).getId();

            //管理員身分發送請求，驗證 HTTP 狀態與回應內容。
            mockMvc.perform(
                    post("/api/admin/events/{eventId}/cancel", eventId)
                            .with(csrf())
            )

                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"));

            //重新讀取資料庫，確認取消結果確實儲存。
        Event savedEvent = eventRepository.findById(eventId).orElseThrow();

        assertEquals(EventStatus.CANCELLED, savedEvent.getStatus());
    }
}