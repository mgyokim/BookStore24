package bookstore24.v2.controller;

import bookstore24.v2.auth.LocalSignUpDto;
import bookstore24.v2.oauth.logic.GoogleLogic;
import bookstore24.v2.oauth.logic.KakaoLogic;
import bookstore24.v2.oauth.logic.LocalLogic;
import bookstore24.v2.oauth.logic.NaverLogic;
import bookstore24.v2.oauth.token.GoogleOauthToken;
import bookstore24.v2.oauth.token.KakaoOauthToken;
import bookstore24.v2.oauth.token.NaverOauthToken;
import bookstore24.v2.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@Slf4j
public class LoginApiController {

    private final KakaoLogic kakaoLogic;
    private final NaverLogic naverLogic;
    private final GoogleLogic googleLogic;
    private final LocalLogic localLogic;

    @GetMapping("/auth/kakao/callback")
    public @ResponseBody
    String kakaoLogin(String code) {

        // 발급받은 인가 코드로 토큰 요청
        KakaoOauthToken kakaoOauthToken = kakaoLogic.codeToToken(code);

        // 발급받은 액세스토큰으로 프로필 정보 요청
        Member member = kakaoLogic.accessTokenToProfile(kakaoOauthToken);

        // 해당 회원의 회원가입 여부 체크후 비회원만 회원가입 처리
        Member joinedMember = kakaoLogic.joinCheck(member);

        if (joinedMember.getLoginId() != null) {
            // 해당 회원 로그인 처리
            kakaoLogic.kakaoAutoLogin(member);
            // 회원의 LoginId 반환
            return member.getLoginId();
        } else {
            String provider = joinedMember.getProvider();
            return joinedMember.getEmail() + " 은 " + provider +" 로그인 방식으로 이미 가입된 이메일입니다. " + provider + " 로그인 방식으로 로그인을 시도하세요.";
        }
    }

    @GetMapping("auth/naver/callback")
    public @ResponseBody
    String naverLogin(String code) {

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
            return joinedMember.getEmail() + " 은 " + provider +" 로그인 방식으로 이미 가입된 이메일입니다. " + provider + " 로그인 방식으로 로그인을 시도하세요.";
        }
    }

    @GetMapping("auth/google/callback")
    public @ResponseBody
    String googleLogin(String code) {

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
            return joinedMember.getEmail() + " 은 " + provider +" 로그인 방식으로 이미 가입된 이메일입니다. " + provider + " 로그인 방식으로 로그인을 시도하세요.";
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
        }
        else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("회원가입 실패! 실패원인은 개발자에게 문의.");
        }
    }
}

