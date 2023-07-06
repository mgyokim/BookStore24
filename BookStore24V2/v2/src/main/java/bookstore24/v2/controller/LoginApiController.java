package bookstore24.v2.controller;

import bookstore24.v2.config.oauth.OAuthToken;
import bookstore24.v2.repository.MemberRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequiredArgsConstructor
public class LoginApiController {

    private final MemberRepository memberRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/auth/kakao/callback")
    public @ResponseBody String kakoCallback(String code) {

        // POST 방식으로 key=value 데이터를 요청(카카오쪽으로)
        // 사용 라이브러리 - RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // HttpHeader 오브젝트 생성
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/x-www-form-urlencoded");   // 내가 지금 전달할 데이터가 key=value 형태임을 알려주는 것.

        // HttpBody 오브젝트 생성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", "e435f34295d28879dfabc32de2bd7546");
        params.add("redirect_uri", "http://bookstore24.shop/auth/kakao/callback");
        params.add("code", code);

        // HttpHeader 와 HttpBody 를 하나의 HttpEntity 오브젝트에 담기 -> 이렇게 해주는 이유는 아래의 restTemplate.exchange() 가 파라미터로 HttpEntity 를 받게 되있기 때문.
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, httpHeaders);

        // Http 요청하기 - POST 방식으로 - 그리고 response 변수로 응답받음
        ResponseEntity<String> response = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",  // 토큰 발급 요청 주소
                HttpMethod.POST,    // 토큰 발급 요청 메서드는 카카오 문서상의 POST
                kakaoTokenRequest,  // HttpBody 에 들어갈 데이터와, HttpHeader 값을 한번에 넣어줌
                String.class    // 응답받을 타입을 String 으로 지정
        );

        // ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        OAuthToken oAuthToken = null;

        try {
            oAuthToken = objectMapper.readValue(response.getBody(), OAuthToken.class);  // Json 데이터를 자바로 처리하기 위해 자바 오브젝트로 바꿈.
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        System.out.println("카카오 access_token : " + oAuthToken.getAccess_token());
        System.out.println("카카오 token_type : "  + oAuthToken.getToken_type());
        System.out.println("카카오 refresh_token : " + oAuthToken.getRefresh_token());
        System.out.println("카카오 expires_in : " + oAuthToken.getExpires_in());
        System.out.println("카카오 scope : " + oAuthToken.getScope());
        System.out.println("카카오 refresh_token_expires_in : " + oAuthToken.getRefresh_token_expires_in());

        return "카카오 인증 완료 : 토큰 요청에 대한 응답 : " + response;
    }
}

