package event_registration.repository;

import event_registration.domain.Registration;
import event_registration.domain.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    //查詢會員是否已有這場活動的報名紀錄，包含已取消的紀錄。
    Optional<Registration> findByMember_IdAndEvent_Id(
            Long memberId,
            Long eventId
    );

    //計算活動指定狀態的報名數，之後只計算 REGISTERED。
    long countByEvent_IdAndStatus(
            Long eventId,
            RegistrationStatus status
    );

}
