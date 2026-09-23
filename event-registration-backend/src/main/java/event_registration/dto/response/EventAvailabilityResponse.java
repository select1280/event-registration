package event_registration.dto.response;

/**
 * 回傳活動名額資訊，只計算 REGISTERED 狀態的報名。
 * 剩餘名額是查詢當下的結果，不代表替使用者保留名額。
 */
public record EventAvailabilityResponse(
        Long eventId,
        int capacity,
        long registeredCount,
        long remainingCapacity
) {
}