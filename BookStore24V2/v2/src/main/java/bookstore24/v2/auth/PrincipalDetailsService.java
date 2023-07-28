package bookstore24.v2.auth;

import bookstore24.v2.domain.Member;
import bookstore24.v2.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// /login => 시큐리티 설정때문에 여기서 동작을 안한다. 따라서 필터로 등록해줘야함
@Service
@RequiredArgsConstructor
@Slf4j
public class PrincipalDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info("PrincipalDetailsService 의 loadUserByUsername() 호출");
        Member memberEntity = memberRepository.findByLoginId(username);
        log.info("loadUserByUsername() 호출되며 찾아온 memberEntity 의 loginId : " + memberEntity.getLoginId());
        return new PrincipalDetails(memberEntity);
    }

}
