package bookstore24.v2.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// 스프링 시큐리티에 UsernamePasswordAuthenticationFilter 가 있음.
// /login 요청해서 username, password 을 post로 전송하면
// UsernamePasswordAuthenticationFilter 가 동작을 함.
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;

    // /login 요청을 하면 로그인 시도를 위해서 실행되는 함수
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        System.out.println("JwtAuthenticationFilter : 로그인 시도중");

        // 1. username, password 받아서
        // 2. 정상인지 로그인 시도를 해보는 것이다. authenticationManager 로 로그인 시도를 하면,
        // 3. PrincipalDetailsService 가 호출된다. 그러면 loadUserByUsername 이 자동으로 실행된다.
        // 4. PrincipalDetails 를 세션에 담고 (세션에 담지 않으면 시큐리티를 이용한 권한관리가 안된다. 권한관리 필요없으면 세션에 담지 않아도 OK)
        // 5. JWT 토큰을 만들어서 응답해주면 됨.

        return super.attemptAuthentication(request, response);
    }
}
