package event_registration.service;

import event_registration.domain.Event;
import event_registration.dto.request.EventRequest;
import event_registration.dto.response.EventAvailabilityResponse;
import event_registration.dto.response.EventResponse;
import event_registration.dto.response.PageResponse;

public interface EventService {

    EventResponse createEvent(EventRequest eventRequest);

    EventResponse getEventById(Long id);

    PageResponse<EventResponse> getEvents(int page, int size);

    EventResponse publishEvent(Long id);

    EventResponse updateEvent(Long id, EventRequest request);

    /**
     * 分頁查詢已發布活動，可依標題關鍵字搜尋。
     */
    PageResponse<EventResponse> getPublishedEvents(
            String ketyword,
            int page,
            int size
            );

    /**
     * 查詢指定的已發布活動 ; 不存在或非已發布狀態時回報查無資料。
     */
    EventResponse getPublishedEventById(Long id);

    /**
     * 查詢已發布活動的名額資訊，草稿或不存在的活動回報查無資料。
     */
    EventAvailabilityResponse getAvailability(Long eventId);
}
