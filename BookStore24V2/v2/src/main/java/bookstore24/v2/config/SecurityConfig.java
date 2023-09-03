package bookstore24.v2.config;

import bookstore24.v2.auth.jwt.JwtAuthenticationFilter;
import bookstore24.v2.auth.jwt.JwtAuthorizationFilter;
import bookstore24.v2.filter.IpFilter;
import bookstore24.v2.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * 1. 코드받기(인증)
 * 2. 엑세스토큰(권한)
 * 3. 사용자프로필 정보를 가져옴
 * 4-1. 그 정보를 토대로 회원가입을 자동으로 진행시킴
 * 4-2. 정보가 좀 모자르면, 자동 회원가입 시키는게 아니라 추가적인 회원가입 창이 나와서 회원가입 시킬수도 있음. ex) 쇼핑몰-집주소 등
 */
@Configuration
@EnableWebSecurity  // 스프링 시큐리티 필터가 스프링 필터체인에 등록된다.
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final MemberRepository memberRepository;

    private final CorsConfig corsConfig;

    private final IpFilter ipFilter;

    /**
     * SecurityFilterChain 을 정의하여 Spring Security 를 구성
     */

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.addFilterBefore(ipFilter, ChannelProcessingFilter.class);
        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 사용 안함, Stateless로 만들 것임
                .and()
                .addFilter(corsConfig.corsFilter()) // cors 설정 ( @CrossOrigin 을 사용하면, 인증이 필요한 요청은 해결할 수 없다. 따라서 필터에 걸어줘야 한다.)
                .formLogin().disable()  // formLogin 방식 사용 안함
                .httpBasic().disable()  // 기본적인 Http Basic 로그인 방식 사용 안하고, Bearer 방식을 사용할 것이다.
                .addFilter(new JwtAuthenticationFilter(authenticationManager(http)))   // AuthenticationManager 파라미터를 줘야함.
                .addFilter(new JwtAuthorizationFilter(authenticationManager(http), memberRepository))    // // AuthenticationManager 파라미터를 줘야함.
                .authorizeRequests()
                .antMatchers("/login").permitAll() // 로그인은 인증 없이 접근 가능하도록 설정 (/login 은 시큐리티 사용시 default 가 .permitAll() 이지만 명시적으로 작성했음)
                .antMatchers("/local/signup").permitAll() // 로컬 회원가입은 인증 없이 접근 가능하도록 설정
                .antMatchers("/auth/kakao/callback").permitAll() // 카카오 로그인은 인증 없이 접근 가능하도록 설정
                .antMatchers("/auth/naver/callback").permitAll() //  네이버 로그인은 인증 없이 접근 가능하도록 설정
                .antMatchers("/auth/google/callback").permitAll() // 구글 로그인은 인증 없이 접근 가능하도록 설정
                .antMatchers("/review/post/list").permitAll() // 리뷰 글 목록은 인증 없이 접근 가능하도록 설정
                .antMatchers("/sell/post/list").permitAll() // 판매 글 목록은 인증 없이 접근 가능하도록 설정
                .antMatchers("/book/ranking/score").permitAll() // 도서 평점 랭킹은 인증 없이 접근 가능하도록 설정
                .antMatchers("/book/ranking/view/review").permitAll() // 도서 리뷰 조회수 랭킹은 인증 없이 접근 가능하도록 설정
                .antMatchers("/book/ranking/view/sell").permitAll() // 도서 판매 조회수 랭킹은 인증 없이 접근 가능하도록 설정
                .antMatchers("/").permitAll() // 기본 홈은 인증 없이 접근 가능하도록 설정

                .antMatchers("/member/list/sub").permitAll() // 개발용 설정임(Member 목록 반환)
                .antMatchers("/book/list/sub").permitAll() // 개발용 설정임(Book 목록 반환)
                .antMatchers("/review/list/sub").permitAll() // 개발용 설정임(Review 목록 반환)
                .antMatchers("/sell/list/sub").permitAll() // 개발용 설정임(Sell 목록 반환)
                .antMatchers("/reviewcomment/list/sub").permitAll() // 개발용 설정임(ReviewComment 목록 반환)

                .anyRequest().authenticated()    // 그 외의 모든 요청은 인증을 요구
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)); // 401 Unauthorized 상태 코드를 반환하도록 설정

        // SecurityFilterChain 을 반환
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .build();
    }
}
