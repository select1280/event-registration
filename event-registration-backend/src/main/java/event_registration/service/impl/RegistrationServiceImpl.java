package event_registration.service.impl;


import event_registration.domain.Event;
import event_registration.domain.Member;
import event_registration.domain.Registration;
import event_registration.domain.enums.RegistrationStatus;
import event_registration.dto.response.RegistrationResponse;
import event_registration.exception.BusinessException;
import event_registration.exception.ResourceNotFoundException;
import event_registration.mapper.RegistrationMapper;
import event_registration.repository.EventRepository;
import event_registration.repository.MemberRepository;
import event_registration.repository.RegistrationRepository;
import event_registration.service.RegistrationService;
import lombok.RequiredArgsConstructor;
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
}
