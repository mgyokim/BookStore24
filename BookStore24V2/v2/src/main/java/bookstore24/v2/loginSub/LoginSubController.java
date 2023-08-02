package bookstore24.v2.loginSub;

import bookstore24.v2.auth.PrincipalDetails;
import bookstore24.v2.auth.oauth.token.GoogleOauthToken;
import bookstore24.v2.auth.oauth.token.KakaoOauthToken;
import bookstore24.v2.auth.oauth.token.NaverOauthToken;
import bookstore24.v2.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LoginSubController {

    private final KakaoLogicSub kakaoLogicSub;
    private final NaverLogicSub naverLogicSub;
    private final GoogleLogicSub googleLogicSub;

    /**
     * 개발용 테스트용 컨트롤러
     * [카카오 로컬개발용]
     * GET
     * https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=e435f34295d28879dfabc32de2bd7546&redirect_uri=http://localhost:8080/auth/kakao/callback
     *
     *
     *
     * [네이버 로컬 개발용]
     * GET
     * https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=B3RGNtinEp3Va8fysxkN&redirect_uri=http://localhost:8080/auth/naver/callback&state='test'
     *
     *
     * [구글 로컬 개발용]
     * GET
     * https://accounts.google.com/o/oauth2/v2/auth?client_id=766446517759-t82jo5h4vk9rmj30bld1d30su7sqdde1.apps.googleusercontent.com&redirect_uri=http://localhost:8080/auth/google/callback&response_type=code&scope=openid%20email%20profile
     */

    @GetMapping("/auth/kakao/callback")
    ResponseEntity<String> kakaoLoginSub(String code) {

        // 발급받은 인가 코드로 토큰 요청
        KakaoOauthToken kakaoOauthToken = kakaoLogicSub.codeToToken(code);

        // 발급받은 액세스토큰으로 프로필 정보 요청
        Member member = kakaoLogicSub.accessTokenToProfile(kakaoOauthToken);

        // 해당 회원의 회원가입 여부 체크후 비회원만 회원가입 처리
        Member joinedMember = kakaoLogicSub.joinCheck(member);

        if (joinedMember.getLoginId() != null) {
            // 해당 회원 로그인 처리
            ResponseEntity<String> responseJwt = kakaoLogicSub.kakaoAutoLogin(member);
            // 회원의 정보로 구성한 Jwt 반환
            log.info("kakaoLoginSub 컨트롤러에서 로그인 정상 응답 반환 완료");
            return responseJwt;
        } else {
            String email = joinedMember.getEmail();
            String provider = joinedMember.getProvider();

            ResponseEntity<String> failResponseJwt = kakaoLogicSub.kakaoAutoLoginFail(email, provider);

            log.info("kakaoLoginSub 컨트롤러에서 로그인 실패 응답 반환 완료" + failResponseJwt);
            log.info(joinedMember.getEmail() + " 은 " + provider + " 로그인 방식으로 이미 가입된 이메일입니다. " + provider + " 로그인 방식으로 로그인을 시도하세요.");

            return failResponseJwt;
        }
    }

    @GetMapping("auth/naver/callback")
    ResponseEntity<String> naverLoginSub(String code) {

        // 발급받은 인가 코드로 토큰 요청
        NaverOauthToken naverOauthToken = naverLogicSub.codeToToken(code);

        // 발급받은 액세스토큰으로 프로필 정보 요청
        Member member = naverLogicSub.accessTokenToProfile(naverOauthToken);

        // 해당 회원의 회원가입 여부 체크후 비회원만 회원가입 처리
        Member joinedMember = naverLogicSub.joinCheck(member);

        if (joinedMember.getLoginId() != null) {
            // 해당 회원 로그인 처리
            ResponseEntity<String> responseJwt = naverLogicSub.naverAutoLogin(member);
            // 회원의 정보로 구성한 Jwt 반환
            log.info("naverLoginSub 컨트롤러에서 로그인 정상 응답 반환 완료");
            return responseJwt;
        } else {
            String email = joinedMember.getEmail();
            String provider = joinedMember.getProvider();

            ResponseEntity<String> failResponseJwt = naverLogicSub.naverAutoLoginFail(email, provider);

            log.info("naverLogin 컨트롤러에서 로그인 실패 응답 반환 완료" + failResponseJwt);
            log.info(joinedMember.getEmail() + " 은 " + provider + " 로그인 방식으로 이미 가입된 이메일입니다. " + provider + " 로그인 방식으로 로그인을 시도하세요.");

            return failResponseJwt;
        }
    }

    @GetMapping("auth/google/callback")
    ResponseEntity<String> googleLoginSub(String code) {

        // 발급받은 인가 코드로 토큰 요청
        GoogleOauthToken googleOauthToken = googleLogicSub.codeToToken(code);

        // 발급받은 액세스토큰으로 프로필 정보 요청
        Member member = googleLogicSub.accessTokenToProfile(googleOauthToken);

        // 해당 회원의 회원가입 여부 체크후 비회원만 회원가입 처리
        Member joinedMember = googleLogicSub.joinCheck(member);

        if (joinedMember.getLoginId() != null) {
            // 해당 회원 로그인 처리
            ResponseEntity<String> responseJwt = googleLogicSub.googleAutoLogin(member);
            // 회원의 정보로 구성한 Jwt 반환
            log.info("googleLoginSub 컨트롤러에서 로그인 정상 응답 반환 완료");
            return responseJwt;
        } else {
            String email = joinedMember.getEmail();
            String provider = joinedMember.getProvider();

            ResponseEntity<String> failResponseJwt = googleLogicSub.googleAutoLoginFail(email, provider);

            log.info("naverLogin 컨트롤러에서 로그인 실패 응답 반환 완료" + failResponseJwt);
            log.info(joinedMember.getEmail() + " 은 " + provider + " 로그인 방식으로 이미 가입된 이메일입니다. " + provider + " 로그인 방식으로 로그인을 시도하세요.");

            return failResponseJwt;
        }
    }

    @GetMapping("home")
    public @ResponseBody
    String home() {
        return "<h1>home</h1>";
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
