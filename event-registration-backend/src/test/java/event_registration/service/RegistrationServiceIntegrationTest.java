package event_registration.service;

import event_registration.config.TestDatabaseConfig;
import event_registration.domain.Event;
import event_registration.domain.Member;
import event_registration.domain.Registration;
import event_registration.domain.enums.RegistrationStatus;
import event_registration.dto.response.RegistrationResponse;
import event_registration.repository.EventRepository;
import event_registration.repository.MemberRepository;
import event_registration.repository.RegistrationRepository;
import org.hibernate.validator.cfg.defs.UUIDDef;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestDatabaseConfig.class)
public class RegistrationServiceIntegrationTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    /**
     * 呼叫真實報名 Service，確認交易提交後能從資料庫查到有效報名。
     */
    @Test
    void register_shouldPersistRegistration(){
        //使用不同Email，避免測試資料因唯一約束互相衝突。
        String email = "member-" + UUID.randomUUID() + "@example.com";

        Member member = memberRepository.save(
                new Member(email, "unused-test-hash", "測是會員")
        );

        //日期相對於現在建立，避免測試隨日曆時間經過而失效。
        Instant now = Instant.now();

        Event event = new Event(
                "整合測試活動",
                "驗證報名資料寫入",
                "線上",
                1,
                now.plus(1, ChronoUnit.DAYS),
                now.plus(2, ChronoUnit.DAYS),
                now.plus(2, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS)
        );

        event.publish(now);
        event = eventRepository.save(event);

        //呼叫 Spring 管理的 Service，實際執行交易與資料庫鎖定。
        RegistrationResponse response =
                registrationService.register(event.getId(), email);

        //Service 已返回，再從資料庫重新查詢，確認資料確實存在。
        Registration saved = registrationRepository
                .findByMember_IdAndEvent_Id(member.getId(), event.getId())
                .orElseThrow( () ->
                        new AssertionError("報名成功後，資料庫應存在報名紀錄")
                );

        assertNotNull(response.id());
        assertEquals(response.id(), saved.getId());
        assertEquals(RegistrationStatus.REGISTERED, saved.getStatus());
        assertNotNull(saved.getRegisteredAt());
        assertNull(saved.getCancelledAt());

        assertEquals(
                1L,
                registrationRepository.countByEvent_IdAndStatus(
                        event.getId(),
                        RegistrationStatus.REGISTERED
                )
        );
    }

    /**
     * 驗證取消後與重新報名都修改原紀錄，
     * 重新報名後 ID 不變，且資料庫只保留一筆該會員的活動報名。
     */
    @Test
    void registerAgain_shouldReuseExistingRegistration(){
        String email = "member-" + UUID.randomUUID() + "@example.com";

        Member member = memberRepository.save(
                new  Member(email, "unused-test-hash", "測試會員")
        );

        Instant now = Instant.now();

        Event event = new Event(
                "重新報名測試",
                "驗證沿用原紀錄",
                "線上",
                1,
                now.plus(1, ChronoUnit.DAYS),
                now.plus(2, ChronoUnit.DAYS),
                now.plus(3, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS)
        );

        event.publish(now);
        event = eventRepository.save(event);

        //首次報名，保存資料庫產生的報名 ID。
        RegistrationResponse first = registrationService.register(event.getId(), email);

        //取消交易完成後，重新查詢資料庫確認狀態與名額已更新。
        registrationService.cancel(event.getId(), email);

        Registration cancelled = registrationRepository
                .findByMember_IdAndEvent_Id(member.getId(), event.getId())
                .orElseThrow();

        assertEquals(RegistrationStatus.CANCELLED, cancelled.getStatus());
        assertNotNull(cancelled.getCancelledAt());
        assertEquals(
                0L,
                registrationRepository.countByEvent_IdAndStatus(
                        event.getId(),
                        RegistrationStatus.REGISTERED
                )
        );

        //再次報名後重新查詢，確認恢復的是同一筆紀錄。
        RegistrationResponse second = registrationService.register(event.getId(), email);

        Registration restored = registrationRepository
                .findByMember_IdAndEvent_Id(member.getId(), event.getId())
                .orElseThrow();

        assertEquals(first.id(), second.id());
        assertEquals(first.id(),  restored.getId());
        assertEquals(RegistrationStatus.REGISTERED, restored.getStatus());
        assertNull(restored.getCancelledAt());
        assertEquals(
                1L,
                registrationRepository.countByEvent_IdAndStatus(
                        event.getId(),
                        RegistrationStatus.REGISTERED
                )
        );
    }
}
