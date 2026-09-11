package event_registration.domain;

import event_registration.domain.enums.RegistrationStatus;
import event_registration.exception.BusinessException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
}
