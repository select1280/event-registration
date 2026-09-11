package event_registration.service.impl;

import event_registration.domain.Member;
import event_registration.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MemberUserDetailServiceImpl implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException{
        String email = username.trim().toLowerCase(Locale.ROOT);

        Member member = memberRepository.findByEmail(email).
                orElseThrow( () -> new UsernameNotFoundException("帳號或密碼不正確")
                );

        return User.withUsername((member.getEmail()))
                .password(member.getPasswordHash())
                .roles(member.getRole().name())
                .build();
    }
}
