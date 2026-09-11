package event_registration.domain;

import event_registration.domain.enums.EventStatus;
import event_registration.exception.BusinessException;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@Getter
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status = EventStatus.DRAFT;

    @Column(name = "registration_deadline", nullable = false)
    private Instant registrationDeadline;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Event() {

    }

    public Event(
            String title,
            String description,
            String location,
            Integer capacity,
            Instant registrationDeadline,
            Instant startsAt,
            Instant endsAt
    ){
        this.title = title;
        this.description = description;
        this.location = location;
        this.capacity = capacity;
        this.registrationDeadline = registrationDeadline;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    public void publish(Instant now){
        if(status != EventStatus.DRAFT){
            throw new BusinessException("只有草稿活動可以發布");
        }

        if(!registrationDeadline.isAfter(now)){
            throw new BusinessException("報名已截止，無法發布活動");
        }

        status = EventStatus.PUBLISHED;
    }

    public void updateDetails(
            String title,
            String description,
            String location,
            Integer capacity,
            Instant registrationDeadline,
            Instant startsAt,
            Instant endsAt
    ){
        if(status != EventStatus.DRAFT){
            throw new BusinessException("只有草稿活動可以修改");
        }

        this.title = title;
        this.description = description;
        this.location = location;
        this.capacity = capacity;
        this.registrationDeadline = registrationDeadline;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    /**
     * 報名時必須是已發布活動，且尚未到達報名截止時間。
     */
    public void validateRegistration(Instant now){
        if(status != EventStatus.PUBLISHED){
            throw new BusinessException("只有已發布活動可以報名");
        }

        if(!registrationDeadline.isAfter(now)){
            throw new BusinessException("報名已截止");
        }
    }

    @PrePersist
    private void onCreate(){
        createdAt = Instant.now();
    }
}
