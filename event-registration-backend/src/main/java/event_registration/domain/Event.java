package event_registration.domain;

import event_registration.domain.enums.EventStatus;
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

    @PrePersist
    private void onCreate(){
        createdAt = Instant.now();
    }
}
