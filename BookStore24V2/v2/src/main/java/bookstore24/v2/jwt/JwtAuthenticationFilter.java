package bookstore24.v2.jwt;

import bookstore24.v2.auth.PrincipalDetails;
import bookstore24.v2.domain.Member;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
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
        // 4. PrincipalDetails2 를 세션에 담고 (세션에 담지 않으면 시큐리티를 이용한 권한관리가 안된다. 권한관리 필요없으면 세션에 담지 않아도 OK)
        // 5. JWT 토큰을 만들어서 응답해주면 됨.

        try {
//            BufferedReader bufferedReader = request.getReader();
//
//            String input = null;
//            while ((input = bufferedReader.readLine()) != null) {
//                System.out.println(input);
//            }

            ObjectMapper objectMapper = new ObjectMapper();
            Member member = objectMapper.readValue(request.getInputStream(), Member.class);
            System.out.println("LoginId : " + member.getLoginId());
            System.out.println("LoginPassword : " + member.getLoginPassword());
            System.out.println("Email : " + member.getEmail());

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(member.getLoginId(), member.getLoginPassword());

            // PrincipalDetailsService 의 loadUserByUsername() 함수가 실행된 후 정상이면 authentication 이 리턴됨.
            // DB 에 있는 username 과 password 가 일치한다.
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
            System.out.println("로그인 완료됨 : " + principalDetails.getMember().getLoginId());  // 값이 있으면 로그인 정상적으로 되었다는 뜻.

            // authentication 객체가 session 영역에 저장됨. => 로그인이 되었다는 뜻
            // 리턴의 이유는 권한 관리를 security 가 대신 해주기 때문에 편하려고 하는 것임.
            // 굳이 JWT 토큰을 사용하면서 세션을 만들 이유가 없음. 근데 단지 권한 처리 때문에 session에 넣어준다.

            return authentication;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    // attemptAuthentication() 실행 후 인증이 정상적으로 되었으면 successfulAuthentication() 메서드가 실행된다.
    // jwt 을 만들어서 request 요청한 사용자에게 jwt 을 response 해주면 된다.
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        System.out.println("successfulAuthentication() 메서드가 실행됨 : 인증이 완료되었다는 뜻임");
        super.successfulAuthentication(request, response, chain, authResult);
    }
}
