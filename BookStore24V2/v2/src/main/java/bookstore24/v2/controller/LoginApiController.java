package bookstore24.v2.controller;

import bookstore24.v2.config.oauth.token.KakaoOauthToken;
import bookstore24.v2.config.oauth.logic.KakaoLogic;
import bookstore24.v2.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LoginApiController {

    private final KakaoLogic kakaoLogic;

    @GetMapping("/auth/kakao/callback")
    public @ResponseBody
    String kakaoLogin(String code) {

        KakaoOauthToken kakaoOauthToken = kakaoLogic.codeToToken(code);
        Member member = kakaoLogic.accessTokenToProfile(kakaoOauthToken);
        kakaoLogic.joinCheck(member);

        return "";
    }
}

