package bookstore24.v2.controller;

import bookstore24.v2.auth.PrincipalDetails;
import bookstore24.v2.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MemberSubController {

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
