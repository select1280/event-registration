package event_registration.service.impl;


import event_registration.domain.Event;
import event_registration.domain.Member;
import event_registration.domain.Registration;
import event_registration.domain.enums.RegistrationStatus;
import event_registration.dto.response.PageResponse;
import event_registration.dto.response.RegistrationAttendeeResponse;
import event_registration.dto.response.RegistrationResponse;
import event_registration.exception.BusinessException;
import event_registration.exception.ResourceNotFoundException;
import event_registration.mapper.RegistrationMapper;
import event_registration.repository.EventRepository;
import event_registration.repository.MemberRepository;
import event_registration.repository.RegistrationRepository;
import event_registration.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final RegistrationRepository registrationRepository;
    private final RegistrationMapper registrationMapper;

    /**
     * 在同一交易中鎖定活動、檢查報名資格與名額，再新增或恢復報名
     */
    @Override
    @Transactional
    public RegistrationResponse register(Long eventId, String email){
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        Member member = memberRepository.findByEmail(normalizedEmail)
                    .orElseThrow( () -> new ResourceNotFoundException("找不到會員")
                );

        //先鎖住活動，讓同一活動的報名請求依序檢查名額。
        Event event = eventRepository.findByIdForUpdate(eventId)
                    .orElseThrow( () -> new ResourceNotFoundException("找不到活動，ID:" + eventId)
                );

        //取得鎖後再讀取時間，避免等待鎖期間已超過報名截止時間。
        Instant now = Instant.now();
        event.validateRegistration(now);

        Registration registration = registrationRepository
                    .findByMember_IdAndEvent_Id(member.getId(), eventId)
                    .orElse(null);

        if(registration != null && registration.getStatus() == RegistrationStatus.REGISTERED){
            throw new BusinessException("你已報名此活動");
        }

        long registeredCount = registrationRepository.countByEvent_IdAndStatus(
                eventId,
                RegistrationStatus.REGISTERED
        );

        if(registeredCount >= event.getCapacity()){
            throw new BusinessException("活動名額已滿");
        }

        if(registration == null){
            //首次報名: 新增紀錄。
            registration = new Registration(member, event, now);
            registration = registrationRepository.save(registration);
        }else{
            //重新報名: 修改受管理的既有紀錄，由 dirty checking 更新。
            registration.registerAgain(now);
        }

        return registrationMapper.toResponse(registration);
    }

    /**
     * 鎖定活動後，取消登入會員自己的報名。
     * 與報名流程使用相同的鎖，協調名額變動。
     */
    @Override
    @Transactional
    public RegistrationResponse cancel(Long eventId, String email){
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        Member member = memberRepository.findByEmail(normalizedEmail)
                .orElseThrow( () -> new ResourceNotFoundException("找不到會員"));

        //先鎖活動，再查詢及修改報名，與新增報名保持相同順序。
        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow( () -> new ResourceNotFoundException("找不到活動，ID :" + eventId)
                );

        //會員與活動都必須符合避免操作別人的紀錄。
        Registration registration = registrationRepository
                .findByMember_IdAndEvent_Id(member.getId(), event.getId())
                .orElseThrow( () -> new ResourceNotFoundException("找不到你的報名紀錄")
                );

        registration.cancel(Instant.now());

        return registrationMapper.toResponse(registration);
    }

    /**
     * 根據登入Email找出會於，再查詢其報名紀錄。
     * 在唯獨交易內完成資料查詢與DTO轉換。
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<RegistrationResponse> getMyRegistrations(
            String email,
            int page,
            int size
    ){
        if(page < 0){
            throw new BusinessException("頁碼不可以小於0");
        }

        if(size < 1 || size > 100){
            throw new BusinessException("每頁筆數必須介於 1 到 100");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        Member member = memberRepository.findByEmail(normalizedEmail)
                .orElseThrow( () -> new ResourceNotFoundException("找不到會員")
                );

        //最近報名的紀錄優先;時間相同時，以唯一 ID 確保排序明確
        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("registeredAt"),
                        Sort.Order.desc("id")
                )
        );

        //Repository 的 EntityGraph 會載入 Mapper 所需的活動資料。
        Page<RegistrationResponse> result = registrationRepository
                .findByMember_Id(member.getId(), pageable)
                .map(registrationMapper::toResponse);

        return new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    /**
     * 確認活動存在後，分頁查詢報名者資訊。
     * 在唯獨交易內完成查詢與 DTO 轉換。
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<RegistrationAttendeeResponse> getEventRegistrations(
            Long eventId,
            int page,
            int size
    ){
        if(page < 0){
            throw new BusinessException("頁碼不可小於0");
        }

        if(size < 1 || size > 100){
            throw new BusinessException("每頁筆數必須介於 1 到 100");
        }

        //區分「活動不存在」與「活動存在但沒有人報名」。
        if(!eventRepository.existsById(eventId)){
            throw new ResourceNotFoundException("找不到活動， ID :" + eventId);
        }

        //最近報名的紀錄優先，ID 作為時間相同時的排序依據。
        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("registeredAt"),
                        Sort.Order.desc("id")
                )
        );

        // EntityGraph 會載入 Mapper 所需的會員資料。
        Page<RegistrationAttendeeResponse> result = registrationRepository
                .findByMember_Id(eventId, pageable)
                .map(registrationMapper::toAttendeeResponse);

        return new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }
}
