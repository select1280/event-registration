package event_registration.domain;

import event_registration.domain.enums.RegistrationStatus;
import event_registration.exception.BusinessException;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@Getter
@Entity
@Table(name = "registrations")
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RegistrationStatus status = RegistrationStatus.REGISTERED;

    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    protected Registration(){
    }

    /**
     *將已取消的紀錄恢復為有效報名，更新報名時間並清除取消時間。
     * 已在報名中的紀錄不可重複報名。
     */
    public void registerAgain(Instant now){
        if(status != RegistrationStatus.CANCELLED){
            throw new BusinessException("你已報名此活動");
        }

        status = RegistrationStatus.REGISTERED;
        registeredAt = now;
        cancelledAt = null;
    }

    public Registration(Member member, Event event, Instant now){
        this.member = member;
        this.event = event;
        this.registeredAt = now;
    }

    /**
     * 取消有效報名並記錄取消時間
     * 保留原紀錄，已取消的報名不可以再次取消。
     */
    public void cancell(Instant now){
        //先檢查狀態，不符合就停止，避免修改任何欄位。
        if(status != RegistrationStatus.REGISTERED){
            throw new BusinessException("此報名已取消");
        }

        status = RegistrationStatus.CANCELLED;
        cancelledAt = now;
    }
}
