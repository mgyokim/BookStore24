package bookstore24.v2.config;

import bookstore24.v2.filter.MyFilter3;
import bookstore24.v2.jwt.JwtAuthenticationFilter;
import bookstore24.v2.oauth.PrincipalOauth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;

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

    private final PrincipalOauth2UserService principalOauth2UserService;

    private final CorsConfig corsConfig;

    /**
     * SecurityFilterChain 을 정의하여 Spring Security 를 구성
     */

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.addFilterBefore(new MyFilter3(), LogoutFilter.class);
        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션 사용 안함, Stateless로 만들 것임
                .and()
                .addFilter(corsConfig.corsFilter()) // cors 설정 ( @CrossOrigin 을 사용하면, 인증이 필요한 요청은 해결할 수 없다. 따라서 필터에 걸어줘야 한다.)
                .formLogin().disable()  // formLogin 방식 사용 안함
                .httpBasic().disable()  // 기본적인 Http Basic 로그인 방식 사용 안하고, Bearer 방식을 사용할 것이다.
                .addFilter(new JwtAuthenticationFilter(authenticationManager(http)))   // AuthenticationManager 파라미터를 줘야함.
                .authorizeRequests()
                .antMatchers("/user/**").access("hasRole('ROLE_USER')")
                .anyRequest().permitAll();

        // SecurityFilterChain 을 반환
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .build();
    }
}
