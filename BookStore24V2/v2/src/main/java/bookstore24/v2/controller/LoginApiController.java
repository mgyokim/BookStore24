package bookstore24.v2.controller;

import bookstore24.v2.config.oauth.logic.KakaoLogic;
import bookstore24.v2.config.oauth.logic.NaverLogic;
import bookstore24.v2.config.oauth.token.KakaoOauthToken;
import bookstore24.v2.config.oauth.token.NaverOauthToken;
import bookstore24.v2.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LoginApiController {

    private final KakaoLogic kakaoLogic;
    private final NaverLogic naverLogic;

    @GetMapping("/auth/kakao/callback")
    public @ResponseBody
    String kakaoLogin(String code) {

        // 발급받은 인가 코드로 토큰 요청
        KakaoOauthToken kakaoOauthToken = kakaoLogic.codeToToken(code);
        // 발급받은 액세스토큰으로 프로필 정보 요청
        Member member = kakaoLogic.accessTokenToProfile(kakaoOauthToken);
        // 해당 회원의 회원가입 여부 체크후 비회원만 회원가입 처리
        kakaoLogic.joinCheck(member);
        // 해당 회원 로그인 처리
        kakaoLogic.kakaoAutoLogin(member);

        // 회원의 LoginId 반환
        return member.getLoginId();
    }

    @GetMapping("auth/naver/callback")
    public @ResponseBody
    String naverLogin(String code) {

        // 발급받은 인가 코드로 토큰 요청
        NaverOauthToken naverOauthToken = naverLogic.codeToToken(code);

        // 발급받은 액세스토큰으로 프로필 정보 요청
        Member member = naverLogic.accessTokenToProfile(naverOauthToken);

        // 해당 회원의 회원가입 여부 체크후 비회원만 회원가입 처리
        naverLogic.joinCheck(member);

        // 해당 회원 로그인 처리
        kakaoLogic.kakaoAutoLogin(member);

        // 회원의 LoginId 반환
        return member.getLoginId();
    }
}

