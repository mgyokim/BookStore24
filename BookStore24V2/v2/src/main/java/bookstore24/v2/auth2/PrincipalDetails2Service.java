package bookstore24.v2.auth2;

import bookstore24.v2.domain.Member;
import bookstore24.v2.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// http://localhost:8080/login => 시큐리티 설정때문에 여기서 동작을 안한다. 따라서 필터로 등록해줘야함
@Service
@RequiredArgsConstructor
public class PrincipalDetails2Service implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        System.out.println("PrincipalDetails2Service 의 loadUserByUsername()");
        Member memberEntity = memberRepository.findByLoginId(username);
        return new PrincipalDetails2(memberEntity);
    }

}
