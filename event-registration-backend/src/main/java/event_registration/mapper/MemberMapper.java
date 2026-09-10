package event_registration.mapper;

import event_registration.domain.Member;
import event_registration.dto.response.MemberResponse;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

    public MemberResponse toResponse(Member member){
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getDisplayName(),
                member.getRole(),
                member.getCreatedAt()
        );
    }
}
