package event_registration.controller;

import event_registration.config.TestDatabaseConfig;
import event_registration.domain.Event;
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
}