package bookstore24.v2.controller;

import bookstore24.v2.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LoginApiController {

    private final MemberRepository memberRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/auth/kakao/callback")
    public @ResponseBody String kakoCallback(String code) {
        return "카카오 인증 완료 : 코드값 : " + code;
    }
}
