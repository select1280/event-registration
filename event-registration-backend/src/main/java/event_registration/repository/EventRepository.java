package event_registration.repository;

import event_registration.domain.Event;
import event_registration.domain.enums.EventStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * 鎖定指定活動，供報名交易依序檢查與使用名額。
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);

    /**
     * 依狀態分頁查詢活動，供會員列表只顯示已發布活動。
     */
    Page<Event> findByStatus(
            EventStatus status,
            Pageable pageable
    );

    /**
     * 同時依活動 ID 與狀態查詢，避免會員透過猜測 ID 讀取草稿。
     */
    Optional<Event> findByIdAndStatus(
            Long id,
            EventStatus status
    );
}
