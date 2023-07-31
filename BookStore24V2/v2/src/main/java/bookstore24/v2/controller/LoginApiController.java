package bookstore24.v2.controller;

import bookstore24.v2.auth.local.LocalSignUpDto;
import bookstore24.v2.auth.local.logic.LocalLogic;
import bookstore24.v2.auth.oauth.logic.GoogleLogic;
import bookstore24.v2.auth.oauth.logic.KakaoLogic;
import bookstore24.v2.auth.oauth.logic.NaverLogic;
import bookstore24.v2.auth.oauth.token.GoogleOauthToken;
import bookstore24.v2.auth.oauth.token.KakaoOauthToken;
import bookstore24.v2.auth.oauth.token.NaverOauthToken;
import bookstore24.v2.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@Slf4j
public class LoginApiController {

    private final KakaoLogic kakaoLogic;
    private final NaverLogic naverLogic;
    private final GoogleLogic googleLogic;
    private final LocalLogic localLogic;

    @PostMapping("/auth/kakao/callback")
    public ResponseEntity<String> kakaoLogin(@RequestParam("Authorization_code") String code) {

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
            log.info(joinedMember.getEmail() + " 은 " + provider + " 로그인 방식으로 이미 가입된 이메일입니다. " + provider + " 로그인 방식으로 로그인을 시도하세요.");
            return failResponseJwt;
        }
    }

    @PostMapping("auth/naver/callback")
    public String naverLogin(@RequestParam("Authorization_code") String code) {
        log.info("=============인가코드========= " + code);
        // 발급받은 인가 코드로 토큰 요청
        NaverOauthToken naverOauthToken = naverLogic.codeToToken(code);

        // 발급받은 액세스토큰으로 프로필 정보 요청
        Member member = naverLogic.accessTokenToProfile(naverOauthToken);

        // 해당 회원의 회원가입 여부 체크후 비회원만 회원가입 처리
        Member joinedMember = naverLogic.joinCheck(member);

        if (joinedMember.getLoginId() != null) {
            // 해당 회원 로그인 처리
            naverLogic.naverAutoLogin(member);
            // 회원의 LoginId 반환
            return member.getLoginId();
        } else {
            String provider = joinedMember.getProvider();
            return joinedMember.getEmail() + " 은 " + provider + " 로그인 방식으로 이미 가입된 이메일입니다. " + provider + " 로그인 방식으로 로그인을 시도하세요.";
        }
    }

    @PostMapping("auth/google/callback")
    public String googleLogin(@RequestParam("Authorization_code") String code) {

        // 발급받은 인가 코드로 토큰 요청
        GoogleOauthToken googleOauthToken = googleLogic.codeToToken(code);

        // 발급받은 액세스토큰으로 프로필 정보 요청
        Member member = googleLogic.accessTokenToProfile(googleOauthToken);

        // 해당 회원의 회원가입 여부 체크후 비회원만 회원가입 처리
        Member joinedMember = googleLogic.joinCheck(member);

        if (joinedMember.getLoginId() != null) {
            // 해당 회원 로그인 처리
            googleLogic.googleAutoLogin(member);
            // 회원의 LoginId 반환
            return member.getLoginId();
        } else {
            String provider = joinedMember.getProvider();
            return joinedMember.getEmail() + " 은 " + provider + " 로그인 방식으로 이미 가입된 이메일입니다. " + provider + " 로그인 방식으로 로그인을 시도하세요.";
        }
    }

    @PostMapping("local/signup")
    public ResponseEntity<String> localSignup(@RequestBody @Valid LocalSignUpDto localSignUpDto,
                                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // 유효성 검사 오류가 있는 경우 에러 응답 변환
            return ResponseEntity.badRequest().body("Invalid signup request");
        }

        // 회원 정보 생성
        Member member = localLogic.requestJsonToMember(localSignUpDto);

        // 회원 가입 시도 (email 이 중복되지 않는 경우에만 회원가입 성공)
        Member tryJoinMember = localLogic.joinCheck(member);

        if ((tryJoinMember.getLoginId() != null) & (tryJoinMember.getEmail() != null)) {
            return ResponseEntity.ok("회원가입 성공!");
        }
        if ((tryJoinMember.getLoginId() == null) & (tryJoinMember.getEmail() != null)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("LoginId 중복입니다. 회원가입 실패!");
        }
        if ((tryJoinMember.getLoginId() != null) & (tryJoinMember.getEmail() == null)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email 중복입니다. 회원가입 실패!");
        }
        if ((tryJoinMember.getLoginId() == null) & (tryJoinMember.getEmail() == null)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("LoginId 와 Email 모두 중복입니다. 회원가입 실패!");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("회원가입 실패! 실패원인은 개발자에게 문의.");
        }
    }
}

