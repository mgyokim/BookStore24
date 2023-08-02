package bookstore24.v2.auth.jwt;

import bookstore24.v2.auth.PrincipalDetails;
import bookstore24.v2.domain.Member;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Date;

// 인
// 스프링 시큐리티에 UsernamePasswordAuthenticationFilter 가 있음.
// /login 요청해서 username, password 을 post 로 전송하면
// UsernamePasswordAuthenticationFilter 가 동작을 함.
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;

    // Authentication 객체 만들어서 리턴 => 의존 : AuthenticationManager
    // /login 요청을 하면 로그인 시도를 위해서 실행되는 함수
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        log.info("[START] - JwtAuthenticationFilter.attemptAuthentication / 로그인 시도 시작 ---------------------------------------------------------------------------------------------------------------------");

        // 1. username, password 받아서
        // 2. 정상인지 로그인 시도를 해보는 것이다. authenticationManager 로 로그인 시도를 하면,
        // 3. PrincipalDetailsService 가 호출된다. 그러면 loadUserByUsername 이 자동으로 실행된다.
        // 4. PrincipalDetails 를 세션에 담고 (세션에 담지 않으면 시큐리티를 이용한 권한관리가 안된다. 권한관리 필요없으면 세션에 담지 않아도 OK)
        // 5. JWT 을 만들어서 응답해주면 됨.

        try {
            //request 에 있는 username 과 password 를 파싱해서 자바 Object로 받기
            ObjectMapper objectMapper = new ObjectMapper();
            Member member = objectMapper.readValue(request.getInputStream(), Member.class);

            // username, password 토큰 생성
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(member.getLoginId(), member.getLoginPassword());

            log.info("JwtAuthenticationFilter : authenticationManager 인증용 토큰 생성 완료 authenticationToken: " + authenticationToken + ", loginId : " + member.getLoginId() + ", loginPassword" + member.getLoginPassword());


            // authenticate() 함수가 호출 되면 인증 프로바이더가 PrincipalDetailsService 의
            // loadUserByUsername(토큰의 첫번째 파라메터) 를 호출하고
            // UserDetails 를 리턴받아서 토큰의 두번째 파라메터(credential)과
            // UserDetails(DB값)의 getPassword()함수로 비교해서 동일하면
            // Authentication 객체를 만들어서 필터체인으로 리턴해준다.

            // Tip: 인증 프로바이더의 디폴트 서비스는 UserDetailsService 타입
            // Tip: 인증 프로바이더의 디폴트 암호화 방식은 BCryptPasswordEncoder
            // 결론은 인증 프로바이더에게 알려줄 필요가 없음.
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
            log.info("로그인 완료됨 - loginId : " + principalDetails.getMember().getLoginId());  // 값이 있으면 로그인이 정상적으로 되었다는 뜻.

            // authentication 객체가 session 영역에 저장됨. => 로그인이 되었다는 뜻
            // 리턴의 이유는 권한 관리를 security 가 대신 해주기 때문에 편하려고 하는 것임.
            // 굳이 jwt 을 사용하면서 세션을 만들 이유가 없음. 근데 단지 권한 처리 때문에 session 에 넣어준다.
            log.info("[END] - JwtAuthenticationFilter.attemptAuthentication / 로그인 시도 완료 ---------------------------------------------------------------------------------------------------------------------");
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
        log.info("[START] - JwtAuthenticationFilter.successfulAuthentication / 해당 메서드 실행됨 : 인증(로그인)이 완료되었다는 뜻임, 클라이언트에게 jwt 토큰 반환 시작 ---------------------------------------------------------------");
        PrincipalDetails principalDetails = (PrincipalDetails) authResult.getPrincipal();

        String jwtToken = JWT.create()
                .withSubject("bookstore24Token")    // 토큰 제목
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.EXPIRATION_TIME))  // 토큰 만료 일자
                .withClaim("loginId", principalDetails.getMember().getLoginId()) // Private claim
                .withClaim("nickName", principalDetails.getMember().getNickName())  // Private claim
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));    // 토큰 사인

        response.addHeader(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + jwtToken);

        log.info("클라이언트에게 반환된 access jwt = Authorization : Bearer " + jwtToken);
        log.info("[END] - JwtAuthenticationFilter.successfulAuthentication / 해당 메서드 실행됨 : 인증(로그인)이 완료되었다는 뜻임, 클라이언트에게 jwt 토큰 반환 완료 ---------------------------------------------------------------");
    }
}
