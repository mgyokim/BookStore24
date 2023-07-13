package bookstore24.v2.config;

import bookstore24.v2.config.oauth.PrincipalOauth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

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

    /**
     * SecurityFilterChain 을 정의하여 Spring Security 를 구성
     */

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeRequests()    // .antMatchers 는 스프링 시큐리티에서 경로에 대한 접근 권한을 설정하기 위해 사용
                .antMatchers("/user/**").authenticated()    // 해당 경로는 인증된 사용자만
                .antMatchers("/manager/**").access("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")   // 해당 경로는 어드민이거나 매니저만
                .antMatchers("/admin/**").access("hasRole('ROLE_ADMIN')")   // 해당 경로는 어드민만
                .anyRequest().permitAll()  // 그 이외경로는 모두가 권한있음
                .and()
                .formLogin()
                .loginPage("/loginForm")   // 권한 없는 경로 접근시 이 경로로 강제이동
                .loginProcessingUrl("/login")  // /login 주소가 호출이 되면 시큐리티가 낚아채서 대신 로그인을 진행해줌. 그렇기때문에 컨트롤러에 따로 /login을 만들어주지 않아도 된다.
                .defaultSuccessUrl("/")    // 로그인 성공시 / 로(메인페이지) 보내줄건데, 만약, 너가 특정 페이지를 들어가려고 했었으면 그 페이지로 보내줄게.
                .and()
                .oauth2Login()
                .loginPage("/loginForm")
                .userInfoEndpoint()
                .userService(principalOauth2UserService); // OAuth 로그인 완료된 뒤의 후처리가 필요함. Tip. 코드X, (엑세스 토큰 + 사용자 프로필 정보)
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .build();
    }
}
