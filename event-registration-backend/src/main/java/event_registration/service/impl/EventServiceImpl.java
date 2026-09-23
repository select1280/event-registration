package event_registration.service.impl;

import event_registration.domain.Event;
import event_registration.domain.enums.EventStatus;
import event_registration.dto.request.EventRequest;
import event_registration.dto.response.EventResponse;
import event_registration.dto.response.PageResponse;
import event_registration.exception.ResourceNotFoundException;
import event_registration.mapper.EventMapper;
import event_registration.repository.EventRepository;
import event_registration.service.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import event_registration.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public EventResponse createEvent(EventRequest request){
        validateEventDates(request);

        Event event = new Event(
                request.getTitle(),
                request.getDescription(),
                request.getLocation(),
                request.getCapacity(),
                request.getRegistrationDeadline(),
                request.getStartsAt(),
                request.getEndsAt()
        );

        Event saveEvent = eventRepository.save(event);

        return eventMapper.toResponse(saveEvent);
    }

    private void validateEventDates(EventRequest request){
        if(request.getRegistrationDeadline().isAfter(request.getStartsAt())){
            throw new BusinessException("報名截止時間不可晚於活動開始時間");
        }

        if(!request.getEndsAt().isAfter(request.getStartsAt())){
            throw new BusinessException("活動結束時間必須晚於開始時間");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEventById(Long id){
        Event event = eventRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("找不到活動，ID:" + id)
                );

        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EventResponse> getEvents(int page, int size){
        if(page < 0){
            throw new BusinessException("頁碼不可小於0");
        }

        if(size < 1 || size > 100){
            throw new BusinessException("每頁筆數必須介於 1 到 100");
        }

        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "id")
        );

        Page<EventResponse> result =eventRepository.findAll(pageable)
                .map(eventMapper::toResponse);

        return new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Override
    @Transactional
    public EventResponse publishEvent(Long id){
        Event event = eventRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("找不到活動， ID :" + id)
                );

        event.publish(Instant.now());

        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional
    public EventResponse updateEvent(Long id, EventRequest request){
        Event event = eventRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("找不到活動， ID:" + id)
                );

        validateEventDates(request);

        event.updateDetails(
                request.getTitle(),
                request.getDescription(),
                request.getLocation(),
                request.getCapacity(),
                request.getRegistrationDeadline(),
                request.getStartsAt(),
                request.getEndsAt()
        );

        return eventMapper.toResponse(event);
    }

    /**
     * 關鍵字有內容時搜尋標題;未提供或只有空白時列出全部已發布活動。
     * 狀態與搜尋條件在資料庫套用，確保分頁總筆數正確。
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<EventResponse> getPublishedEvents(String keyword, int page, int size){
        if(page < 0){
            throw new BusinessException("頁碼不可小於0");
        }

        if(size < 1 || size > 100){
            throw new BusinessException("每頁筆數必須介於 1 到 100");
        }

        String normalizedkeyword = keyword == null ? "" : keyword.strip();

        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "id")
        );

        Page<Event> events;

        if(normalizedkeyword.isEmpty()){
            //沒有搜尋條件，沿用原本的已發布活動查詢。
            events = eventRepository.findByStatus(
                    EventStatus.PUBLISHED,
                    pageable
            );
        } else{
            //狀態固定為 PUBLISHED，會員不能透過搜尋取得草稿。
            events = eventRepository.findByStatusAndTitleContainingIgnoreCase(
                    EventStatus.PUBLISHED,
                    normalizedkeyword,
                    pageable
            );
        }

        Page<EventResponse> result = events.map(eventMapper::toResponse);

        return new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    /**
     * 將活動 ID 與已發布狀態一起查詢，避免透過 ID 取得草稿。
     */
    @Override
    @Transactional(readOnly = true)
    public EventResponse getPublishedEventById(Long id){
        Event event = eventRepository
                .findByIdAndStatus(id, EventStatus.PUBLISHED)
                .orElseThrow( () ->
                        new ResourceNotFoundException("找不到活動，ID :" + id)
                );

        return eventMapper.toResponse(event);
    }


}
