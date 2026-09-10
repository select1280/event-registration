package event_registration.service.impl;

import event_registration.domain.Event;
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
}
