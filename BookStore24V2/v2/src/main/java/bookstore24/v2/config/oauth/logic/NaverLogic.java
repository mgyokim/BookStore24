package bookstore24.v2.config.oauth.logic;

import bookstore24.v2.config.oauth.token.NaverOauthToken;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class NaverLogic {

    @Value("${cos.key}")
    private String cosKey;

    @Value(("${spring.security.oauth2.client.registration.naver.client-id}"))
    private String clientId;

    @Value(("${spring.security.oauth2.client.registration.naver.client-secret}"))
    private String clientSecret;

    /**
     * 네이버 인가 코드 받기 (LoginApiController.kakaoLogin() 에서 처리)
     */
    // 요청 URL
    // https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=B3RGNtinEp3Va8fysxkN&redirect_uri=http://bookstore24.shop/auth/naver/callback&state='test'

    /**
     * 발급받은 인가 코드로 토큰 요청하기
     */
    public NaverOauthToken codeToToken(String code) {

        // sout 배포전 삭제할 것.
        System.out.println("[네이버]발급받은 인가 코드로 토큰 요청 시작-----------------------------------------------------------------");

        // POST 방식으로 key=value 데이터를 요청(카카오쪽으로)
        // 사용 라이브러리 - RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // HttpHeader 오브젝트 생성
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/x-www-form-urlencoded");   // 내가 지금 전달할 데이터가 key=value 형태임을 알려주는 것.

        // HttpBody 오브젝트 생성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("state", "test");

        // HttpHeader 와 HttpBody 를 하나의 HttpEntity 오브젝트에 담기 -> 이렇게 해주는 이유는 아래의 restTemplate.exchange() 가 파라미터로 HttpEntity 를 받게 되있기 때문.
        HttpEntity<MultiValueMap<String, String>> naverTokenRequest = new HttpEntity<>(params, httpHeaders);

        // Http 요청하기 - POST 방식으로 - 그리고 response 변수로 응답받음
        ResponseEntity<String> response = restTemplate.exchange(
                "https://nid.naver.com/oauth2.0/token",  // 토큰 발급 요청 주소
                HttpMethod.POST,    // 토큰 발급 요청 메서드는 네이버 문서상의 POST
                naverTokenRequest,  // HttpBody 에 들어갈 데이터와, HttpHeader 값을 한번에 넣어줌
                String.class    // 응답받을 타입을 String 으로 지정
        );

        // ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        NaverOauthToken naverOauthToken = null;   // 카카오 토큰 응답 데이터를 통째로 저장할 곳

        try {
            naverOauthToken = objectMapper.readValue(response.getBody(), NaverOauthToken.class);  // Json 데이터를 자바로 처리하기 위해 자바 오브젝트로 바꿈.
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // sout 배포전 삭제할 것.
        System.out.println("네이버 토큰 : " + naverOauthToken);
        System.out.println("[네이버]발급받은 인가 코드로 토큰 요청 완료-----------------------------------------------------------------");

        return naverOauthToken;
    }

    /**
     * 발급받은 AccessToken 을 이용하여 카카오 프로필 정보 요청하기
     */

    /**
     * 미가입자만 체크해서 자동 회원가입
     */

    /**
     * 자동 로그인 처리
     */
}
