package bookstore24.v2.auth;

import bookstore24.v2.domain.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * 시큐리티가 /login 주소로 요청이 오면 낚아채서 로그인을 진행시킨다.
 * 로그인 진행이 완료되면 시큐리티 session을 만들어준다. (Security ContextHolder)라는 키값으로.
 * 시큐리티가 가지고 있는 세션에 들어갈 수 있는 오브젝트 -> Authentication 타입 객체
 * Authentication 타입 객체 안에 들어갈 수 있는 타입은 오직 2가지이다.
 * 1. UserDetails
 * 2. OAuth2User
 * 이렇게 두가지 타입 뿐이다.
 *
 * 정리하자면
 *      서버자체가 들고있는 세션이 있는데 그 영역안에
 *      스프링 시큐리티가 관리하는시큐리티 세션영역이 있다.
 *      그리고 시큐리티가 관리하는 해당 세션에 들어갈 수 있는 타입은 Authentication 객체 뿐이다. 그래서 우리가 필요할때마다 컨트롤러에서 DI할 수 있는 것.
 *      그리고 이 Authentication 객체 안에 들어갈 수 있는 두개의 타입이 있는데, UserDetails 타입, OAuth2User 타입이다.
 *
 *      시큐리티가 들고잇는 세션에는 Authentication 객체만 들어갈 수 있다.
 *      이 Authentication 객체가 들어가는 순간 로그인이 된 것이다.
 *      이 Authentication 객체 안에 들어갈 수 있는 타입은 2가지 인데, UserDetails 타입, OAuth2User 타입뿐이다.
 *      언제 UserDetails 타입이 만들어지냐면, 우리가 일반적인 로그인을 할 때만들어짐. 우리가 구글같은 OAuth 로그인 이용하면 OAuth2User 타입이 Authentication 객체안에 들어간다.
 *
 *      그러면, 이 객체가 들어가면 세션이 생기니까 로그인이 된 것인데,
 *      우리가 필요할 때 꺼내 써야하는데, 불편한게 있다.
 *      우리가 어떤 컨트롤러에서 일반적인 로그인을 했을때 세션에 접근하려면 어떻게 해야하냐면, @AuthenticationPrincipal PrincipalDetails2 userDetails를 파라미터로 받아야한다. 혹은 @AuthenticationPrincipal UserDetails userDetails.
 *      그런데 만약에 구글로 로그인 했다면, @AuthenticationPrincipal OAuth2User oauth 이렇게 받아야 한다.
 *      이렇게 두가지 로그인 방식에 해당하는 객체가 다르기 때문에 하나의 컨트롤러에서 처리하는 것에 대해 고민이 생긴다.
 *
 *      그래서 어떻게 해야하냐면,
 *      Authentication 객체 안에 들어갈 수 있는 타입은 UserDetials, OAuth2User 2개니까 X라는 클래스를 만들어서 각각을 implemetation 하도록 하고, Authentication에 X를 담는 방식을 사용하면 된다.
 *      지금 이 프로젝트애서는 X가 PrincipalDetails2 이 될 것이다.
 */
@Getter
public class PrincipalDetails2 implements UserDetails, OAuth2User{

    private Member member;  // 콤포지션(객체를 품고 있는 것)
    private Map<String, Object> attributes;

    // 일반 로그인
    public PrincipalDetails2(Member member) {
        this.member = member;
    }

    // OAuth 로그인
    public PrincipalDetails2(Member member, Map<String, Object> attributes) {
        this.member = member;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    // 해당 회원의 권한을 리턴하는 곳!!
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return member.getRole();
            }
        });
        return authorities;
    }

    @Override
    public String getPassword() {
        return member.getLoginPassword();
    }

    @Override
    public String getUsername() {
        return member.getLoginId();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 만약 서비스 정책으로  우리 사이트에서 1년동안 회원이 로그인을 안하면 휴먼계정으로 하기로 했다면,
        // 엔티티 필드에 마지막 로그인 시간 저장하는 필드 추가 해놓고나서,
        // 현재시간 - 로그인시간 => 1년을 초과하면 return false 로.
        return true;
    }
}