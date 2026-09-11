package event_registration.service.impl;

import event_registration.domain.Member;
import event_registration.dto.request.RegisterRequest;
import event_registration.dto.response.MemberResponse;
import event_registration.exception.BusinessException;
import event_registration.exception.ResourceNotFoundException;
import event_registration.mapper.MemberMapper;
import event_registration.repository.MemberRepository;
import event_registration.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public MemberResponse register(RegisterRequest request){
        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if(memberRepository.existsByEmail(email)){
            throw new BusinessException("此 Email 已被註冊");
        }

        String passwordHash = passwordEncoder.encode(
                request.getPassword()
        );

        Member member = new Member(
                email,
                passwordHash,
                request.getDisplayName()
        );

        Member savedMember = memberRepository.save(member);

        return memberMapper.toResponse(savedMember);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponse getMemberByEmail(String email){
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        Member member = memberRepository.findByEmail(normalizedEmail)
                    .orElseThrow( () -> new ResourceNotFoundException("找不到會員")
                );

        return memberMapper.toResponse(member);
    }
}
