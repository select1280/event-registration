package event_registration.domain;

import event_registration.domain.enums.MemberRole;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.Locale;

@Getter
@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role = MemberRole.MEMBER;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Member(){

    }

    public Member(
            String email,
            String passwordHash,
            String displayName
    ){
        this.email = email.trim().toLowerCase(Locale.ROOT);
        this.passwordHash = passwordHash;
        this.displayName = displayName;
    }

    @PrePersist
    private void onCreate(){
        createdAt = Instant.now();
    }
}
