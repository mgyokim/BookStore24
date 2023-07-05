package bookstore24.v2.controller;

import bookstore24.v2.config.auth.PrincipalDetails;
import bookstore24.v2.domain.Member;
import bookstore24.v2.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class MemberSubController {

    private final MemberRepository memberRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // Authentication 을 DI(의존성 주입) 하면 authentication객체 안에 getPrincipal 객체가 있고 이걸 PrincipalDetails로 다운캐스팅해서 .getUser() 호출하면 User 객체 얻을 수 있음.
    // @AuthenticationPrincipal 통해서 getUser를 찾을 수도 있다.
    @GetMapping("/test/login")
    public @ResponseBody
    String testLogin(   // 일반적인 로컬 로그인을 하면 UserDetails 타입이 authentication 객체에 들어오는 것.
                        Authentication authentication,// // Authentication 을 DI해서 PrincipalDetails로 다운캐스팅과정 거쳐서 User객체 찾을 수도 있고,
                        @AuthenticationPrincipal PrincipalDetails userDetails) {  // @AuthenticationPrincipal 통해서 getUser를 찾을 수도 있다.

        System.out.println("/test/login ========================");
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        System.out.println("authentication : " + principalDetails.getMember());

        System.out.println("userDetails : " + userDetails.getMember());

        return "세션 정보 확인하기";
    }

    /**
     * 정리하자면
     * 서버자체가 들고있는 세션이 있는데 그 영역안에
     * 스프링 시큐리티가 관리하는시큐리티 세션영역이 있다.
     * 그리고 시큐리티가 관리하는 해당 세션에 들어갈 수 있는 타입은 Authentication 객체 뿐이다. 그래서 우리가 필요할때마다 컨트롤러에서 DI할 수 있는 것.
     * 그리고 이 Authentication 객체 안에 들어갈 수 있는 두개의 타입이 있는데, 1. UserDetails 타입, OAuth2User 타입 이다.
     * <p>
     * 시큐리티가 들고잇는 세션에는 Authentication 객체만 들어갈 수 있다.
     * 이 Authentication 객체가 들어가는 순간 로그인이 된 것이다.
     * 이 Authentication 객체 안에 들어갈 수 있는 타입은 2가지 인데, UserDetails 타입, OAuth2User 타입뿐이다.
     * 언제 UserDetails 타입이 만들어지냐면, 우리가 일반적인 로그인을 할 때만들어짐. 우리가 구글같은 OAuth로그인 이용하면 OAuth2User 타입이 Authentication 객체안에 들어간다.
     * <p>
     * 그러면, 이 객체가 들어가면 세션이 생기니까 로그인이 된 것인데,
     * 우리가 필요할 때 꺼내 써야하는데, 불편한게 있다.
     * 우리가 어떤 컨트롤러에서 일반적인 로그인을 했을때 세션에 접근하려면 어떻게 해야하냐면, @AuthenticationPrincipal PrincipalDetails userDetails를 파라미터로 받아야한다. 혹은 @AuthenticationPrincipal UserDetails userDetails.
     * 그런데 만약에 구글로 로그인 했다면, @AuthenticationPrincipal OAuth2User oauth 이렇게 받아야 한다.
     * 이렇게 두가지 로그인 방식에 해당하는 객체가 다르기 때문에 컨트롤러에서 처리하는 것에 대해 고민이 생긴다.
     * 그래서 어떻게 해야하냐면,
     * Authentication 객체 안에 들어갈 수 있는 타입은 UserDetials, OAuth2User 2개니까 X라는 클래스를 만들어서 각각을 implemetation 하도록 하고, Authentication에 X를 담는 방식을 사용하면 된다.
     * 지금 이 프로젝트애서는 X가 PrincipalDetails 이 될 것이다.
     */
    @GetMapping("/test/oauth/login")
    public @ResponseBody
    String testOauthLogin(  // 오어스 로그인을 하면, authentication 객체에 OAuth2User가 들어온다.
                            Authentication authentication,  // Authentication 을 DI해서 OAuth2User로 다운캐스팅과정 거쳐서 User객체 찾을 수도 있고,
                            @AuthenticationPrincipal OAuth2User oauth) {
        System.out.println("/test/oauth/login ========================");
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        System.out.println("authentication : " + oAuth2User.getAttributes());

        System.out.println("oauth2User : " + oauth.getAttributes());
        return "OAuth 세션 정보 확인하기";
    }

    // localhost:8080
    @GetMapping({"", "/"})
    public String index() {
        // 머스테치 기본폴더 src/main/resources/
        // 뷰리졸버 설정 : templates(prefix), mustache(suffix) 생략가능!
        return "index"; // src/main/resources/templates/index.mustache
    }

    // OAuth 로그인을 해도 PrincipalDetails
    // 일반 로그인을 해도 PrincipalDetails
    @GetMapping("/user")
    public @ResponseBody
    String user(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        System.out.println("principalDetails : " + principalDetails.getMember());
        return "user";
    }

    String user() {
        return "user";
    }

    @GetMapping("/admin")
    public @ResponseBody
    String admin() {
        return "admin";
    }

    @GetMapping("/manager")
    public @ResponseBody
    String manager() {
        return "manager";
    }

    // /login는 스프링시큐리티가 해당 주소를 낚아챈다!! -> SecurityConfig 파일 생성후에는 낚아채지 않고 의도대 동작함.
    @GetMapping("/loginForm")
    public String loginForm() {
        return "loginForm";
    }

    @GetMapping("/joinForm")
    public String joinForm() {
        return "joinForm";
    }

    @PostMapping("/join")
    public String join(Member member) {
        System.out.println("회원 가입 진행 : " + member);
        System.out.println(member.getLoginId());
        System.out.println(member.getLoginId());
        System.out.println(member.getLoginPassword());
        String rawPassword = member.getLoginPassword();
        String encPassword = bCryptPasswordEncoder.encode(rawPassword);
        member.registrationLoginPassword(encPassword);
        member.registrationRole("ROLE_USER");
        memberRepository.save(member);  // 회원가입 잘됨. 비밀번호 : 1234 => 시큐리티로 로그인을 할 수 없음. 이유는 패스워드가 암호화가 안되었기 때문.
        return "redirect:/loginForm";
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/info")
    public @ResponseBody
    String info() {
        return "개인정보";
    }

    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    @GetMapping("/data")
    public @ResponseBody
    String data() {
        return "데이터정보";
    }
}
