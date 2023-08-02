package bookstore24.v2.jwt;

import bookstore24.v2.auth.PrincipalDetails;
import bookstore24.v2.domain.Member;
import bookstore24.v2.repository.MemberRepository;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// 인가
// 시큐리티가 filter 를 가지고 있는데 그 필터중에 BasicAuthenticationFilter 라는 것이 있음.
// 권한이나 인증이 필요한 특정 주소를 요청했을 때 위 필터를 무조건 타게 되어있음.
// 만약에 권한이나 인증이 필요한 주소가 아니라면 이 필터를 타지 않는다.
@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private MemberRepository memberRepository;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, MemberRepository memberRepository) {
        super(authenticationManager);
        this.memberRepository = memberRepository;
    }

    // 인증이나 권한이 필요한 주소요청이 있을 때 해당 필터를 타게 됨.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        log.info("[START] - JwtAuthorizationFilter.doFilterInternal / 인증이나 권한이 필요한 주소에 대한 인가 절차 시작 (필터체인에 등록했기 때문에, 인가가 필요없는 접근에 대해서도 수행은 됨.)");
        log.info("REQUEST URI : " + request.getRequestURI() + " ClientIp : " + request.getRemoteAddr());


        String jwtHeader = request.getHeader(JwtProperties.HEADER_STRING);
        log.info("reqeust 에서 Authorization 헤더에 담긴 JWT : " + jwtHeader);

        // header 가 있는지 확인 (만약 헤더가 없거나, "Bearer" 가 아니면 권한이 인증받지 않은 사용자라 판단하고 필처체인에 태워서 리턴함.
        // 필터체인에 태우는 이유는 정상적인 사용자가 아닌 경우에도 다음 필터로 넘어가서 처리가 되도록 하기 위함.
        // 여기서 로그아웃 시키지 않고 필터를 통과시키는 이유는,
        // 만약, 다시 필터를 타게하지 않고, 로그아웃을 시켜버리면, 모든 요청에 대해 로그아웃 되어버리기 때문에,로그인하지 않은 사용자도 로그아웃 처리가 되어 버릴 수 있다.
        // 하지만 필터에 태워서 요청을 가로채고 검사하면, 로그인 되지 않은, 또는 권한이 없는 사용자에 대한 추가적인 처리를 할 수 있다.
        // 이로써 미인증 사용자에 대해 특정 작업을 수행하거나 보안적으로 중요한 부분에 접근을 제한할 수 있음.
        if (jwtHeader == null || !jwtHeader.startsWith(JwtProperties.TOKEN_PREFIX)) {

            log.info("인증 관련 헤더가 없거나, Authorization 헤더가 아님");
            log.info("[END] - JwtAuthorizationFilter.doFilterInternal / 인증이나 권한이 필요한 주소에 대한 인가 절차 종료 (필터체인에 등록했기 때문에, 인가가 필요없는 접근에 대해서도 수행은 됨.)");

            chain.doFilter(request, response);
            return;
        }

        // JWT 의 Payload 슬라이싱
        String jwtPayload = request.getHeader(JwtProperties.HEADER_STRING).replace(JwtProperties.TOKEN_PREFIX, "");

        // JWT 의 페이로드를 이용하여 토큰 검증
        try {
            // 토큰 검증 (이게 인증이기 때문에 AuthenticationManager 도 필요 없음)
            String loginId = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(jwtPayload).getClaim("loginId").asString();

            // 서명이 정상적으로 됨
            if (loginId != null) {
                log.info("JWT 서명 정상적으로 완료 - 인증이 완료된 사용자");
                Member memberEntity = memberRepository.findByLoginId(loginId);

                // 인증은 토큰 위에서 검증시 끝. 인증을 하기 위해서가 아닌 스프링 시큐리티가 수행해주는 권한 처리를 위해
                // 아래와 같이 토큰을 만들어서 Authentication 객체를 강제로 만들고 그걸 세션에 저장 할 것임!
                PrincipalDetails principalDetails = new PrincipalDetails(memberEntity);
                Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities()); // 어차피 인증된 사용자이므로 패스워드는 null 로 처리.

                // 내가 SecurityContext 에 집적접근해서 세션을 만들때 자동으로 UserDetailsService  에 있는 loadByUsername 이 호출됨.
                // 강제로 시큐리티의 세션에 접근하여 Authentication 객체를 저장.
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.info("인증이 완료된 사용자의 loginId : " + memberEntity.getLoginId() + " 에 대한 인가 완료");
                log.info("[END] - JwtAuthorizationFilter.doFilterInternal / 인증이나 권한이 필요한 주소에 대한 인가 절차 종료 (필터체인에 등록했기 때문에, 인가가 필요없는 접근에 대해서도 수행은 됨.)");

                // 서명이 비정상적
            } else {
                log.info("인가 실패 : 유효하지 않은 JWT");
                log.info("[END] - JwtAuthorizationFilter.doFilterInternal / 인증이나 권한이 필요한 주소에 대한 인가 절차 종료 (필터체인에 등록했기 때문에, 인가가 필요없는 접근에 대해서도 수행은 됨.)");
            }
        } catch (SignatureVerificationException e) {
            log.info("인가 실패 : 유효하지 않은 JWT- SignatureVerificationException");
            log.info("[END] - JwtAuthorizationFilter.doFilterInternal / 인증이나 권한이 필요한 주소에 대한 인가 절차 종료 (필터체인에 등록했기 때문에, 인가가 필요없는 접근에 대해서도 수행은 됨.)");
        }
        catch (JWTDecodeException e) {
            log.info("인가 실패 : 유효하지 않은 JWT - JWTDecodeException");
            log.info("[END] - JwtAuthorizationFilter.doFilterInternal / 인증이나 권한이 필요한 주소에 대한 인가 절차 종료 (필터체인에 등록했기 때문에, 인가가 필요없는 접근에 대해서도 수행은 됨.)");
        }
        chain.doFilter(request, response);
    }
}
