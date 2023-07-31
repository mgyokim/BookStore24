package bookstore24.v2.controller;

import bookstore24.v2.auth.PrincipalDetails;
import bookstore24.v2.auth.local.logic.LocalLogic;
import bookstore24.v2.auth.oauth.logic.GoogleLogic;
import bookstore24.v2.auth.oauth.logic.KakaoLogic;
import bookstore24.v2.auth.oauth.logic.NaverLogic;
import bookstore24.v2.auth.oauth.token.KakaoOauthToken;
import bookstore24.v2.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MemberSubController {

    private final KakaoLogic kakaoLogic;
    private final NaverLogic naverLogic;
    private final GoogleLogic googleLogic;
    private final LocalLogic localLogic;

    /**
     * 테스트용 컨트롤러
     */
    @GetMapping("/auth/kakao/callback")
    ResponseEntity<String> kakaoLogin2(String code) {

        // 발급받은 인가 코드로 토큰 요청
        KakaoOauthToken kakaoOauthToken = kakaoLogic.codeToToken(code);

        // 발급받은 액세스토큰으로 프로필 정보 요청
        Member member = kakaoLogic.accessTokenToProfile(kakaoOauthToken);

        // 해당 회원의 회원가입 여부 체크후 비회원만 회원가입 처리
        Member joinedMember = kakaoLogic.joinCheck(member);

        if (joinedMember.getLoginId() != null) {
            // 해당 회원 로그인 처리
            ResponseEntity<String> responseJwt = kakaoLogic.kakaoAutoLogin(member);
            // 회원의 LoginId 반환
            log.info("kakaoLogin 컨트롤러에서 로그인 정상 응답 반환 완료");
            return responseJwt;
        } else {
            String email = joinedMember.getEmail();
            String provider = joinedMember.getProvider();

            ResponseEntity<String> failResponseJwt = kakaoLogic.kakaoAutoLoginFail(email, provider);

            log.info("kakaoLogin 컨트롤러에서 로그인 실패 응답 반환 완료" + failResponseJwt);
            return failResponseJwt;
//            return joinedMember.getEmail() + " 은 " + provider +" 로그인 방식으로 이미 가입된 이메일입니다. " + provider + " 로그인 방식으로 로그인을 시도하세요.";
        }
    }

    @GetMapping("home")
    public @ResponseBody
    String home() {
        return "<h1>home</h1>";
    }

    @PostMapping("token")
    public @ResponseBody
    String token() {
        return "<h1>token</h1>";
    }

    // user, manager, admin 권한만 접근 가능
    @GetMapping("/user")
    public @ResponseBody
    String user(Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        return "user";
    }

    // manager, admin 권한만 접근 가능
    @GetMapping("/manager")
    public @ResponseBody
    String manager() {
        return "manager";
    }

    // admin 권한만 접근 가능
    @GetMapping("/admin")
    public @ResponseBody
    String admin() {
        return "admin";
    }
}
