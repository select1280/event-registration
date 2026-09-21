package event_registration.repository;

import event_registration.domain.Registration;
import event_registration.domain.enums.RegistrationStatus;
import jakarta.persistence.Entity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

    /**
     * 分頁查詢指定會員的報名，包含有效與已取消紀錄。
     * 同時載入活動資料，供回應取得活動標題，避免逐筆查詢活動
     */
    @EntityGraph(attributePaths = "event")
    Page<Registration> findByMember_Id(
            Long memberId,
            Pageable pageable
    );

    /**
     * 分頁查詢指定活動的報名名單，包含已取消紀錄。
     * 同時載入會員資料，供管理員查看姓名與 Email。
     */
    @EntityGraph(attributePaths = "member")
    Page<Registration> findByEvent_Id(
            Long eventId,
            Pageable pageable
    );

}
