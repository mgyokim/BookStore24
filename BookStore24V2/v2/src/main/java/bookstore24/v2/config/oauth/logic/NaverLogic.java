package bookstore24.v2.config.oauth.logic;

import bookstore24.v2.config.oauth.profile.NaverProfile;
import bookstore24.v2.config.oauth.token.NaverOauthToken;
import bookstore24.v2.domain.Member;
import bookstore24.v2.service.MemberService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class NaverLogic {

    private final MemberService memberService;

    private final AuthenticationManager authenticationManager;

    @Value("${cos.key}")
    private String cosKey;

    @Value(("${spring.security.oauth2.client.registration.naver.client-id}"))
    private String clientId;

    @Value(("${spring.security.oauth2.client.registration.naver.client-secret}"))
    private String clientSecret;

    final String NAVER_REDIRECT_URI = "http://bookstore24.shop/auth/naver/callback";

    final String NAVER_TOKEN_REQUEST_URI = "https://nid.naver.com/oauth2.0/token";

    final String NAVER_PROFILE_REQUEST_URI = "https://openapi.naver.com/v1/nid/me";

    /**
     * 네이버 인가 코드 받기 (LoginApiController.naverLogin() 에서 처리)
     */
    // 요청 URL
    // https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=B3RGNtinEp3Va8fysxkN&redirect_uri=http://bookstore24.shop/auth/naver/callback&state='test'

    /**
     * 발급받은 인가 코드로 토큰 요청하기
     */
    public NaverOauthToken codeToToken(String code) {

        log.info("[네이버]발급받은 인가 코드로 토큰 요청 시작-----------------------------------------------------------------");

        // POST 방식으로 key=value 데이터를 요청(네이버쪽으로)
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
                NAVER_TOKEN_REQUEST_URI,  // 토큰 발급 요청 주소
                HttpMethod.POST,    // 토큰 발급 요청 메서드는 네이버 문서상의 POST
                naverTokenRequest,  // HttpBody 에 들어갈 데이터와, HttpHeader 값을 한번에 넣어줌
                String.class    // 응답받을 타입을 String 으로 지정
        );

        // ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        NaverOauthToken naverOauthToken = null;   // 네이 토큰 응답 데이터를 통째로 저장할 곳

        try {
            naverOauthToken = objectMapper.readValue(response.getBody(), NaverOauthToken.class);  // Json 데이터를 자바로 처리하기 위해 자바 오브젝트로 바꿈.
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        log.info("네이버 토큰 : " + naverOauthToken);
        log.info("[네이버]발급받은 인가 코드로 토큰 요청 완료-----------------------------------------------------------------");

        return naverOauthToken;
    }

    /**
     * 발급받은 AccessToken 을 이용하여 네이버 프로필 정보 요청하기
     * @return
     */

    public Member accessTokenToProfile(NaverOauthToken naverOauthToken) {

        log.info("[네이버]AccessToken 을 이용하여 네이버 프로필 정보 요청 시작-------------------------------------------------");

        // 네이버 토큰 응답 데이터를 각 변수에 저장
        String naver_access_token = naverOauthToken.getAccess_token();
        String naver_token_type = naverOauthToken.getToken_type();
        String naver_expires_in = naverOauthToken.getExpires_in();
        String naver_refresh_token = naverOauthToken.getRefresh_token();
        String naver_scope = naverOauthToken.getScope();
        String naver_refresh_token_expires_in = naverOauthToken.getRefresh_token_expires_in();

        RestTemplate restTemplate = new RestTemplate();

        // HttpHeader 오브젝트 생성
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + naver_access_token);   // 발급받았던 AccessToken을 프로필 정보 요청에 사용
        httpHeaders.add("Content-Type", "application/x-www-form-urlencoded");  // 내가 지금 전달할 데이터가 key=value 형태임을 알려주는 것.

        // HttpHeader 와 HttpBody 를 하나의 HttpEntity 오브젝트에 담기 -> 이렇게 해주는 이유는 아래의 restTemplate.exchange() 가 파라미터로 HttpEntity 를 받게 되있기 때문.
        HttpEntity<MultiValueMap<String, String>> naverProfileRequest = new HttpEntity<>(httpHeaders);

        // Http 요청하기 - POST 방식으로 - 그리고 reponse 변수로 응답받음
        ResponseEntity<String> response = restTemplate.exchange(
                NAVER_PROFILE_REQUEST_URI,    // 네이버 문서상의 프로필 정보 요청 주소
                HttpMethod.GET,    // 네이버
                naverProfileRequest,
                String.class
        );

        // ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        NaverProfile naverProfile = null;

        try {
            naverProfile = objectMapper.readValue(response.getBody(), NaverProfile.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        log.info("provider : " + "naver");
        log.info("providerId : " + naverProfile.getResponse().getId());
        log.info("loginId : " + "naver" + "_" + naverProfile.getResponse().getId());
        log.info("loginPassword : " + cosKey);
        log.info("email : " + naverProfile.getResponse().getEmail());
        log.info("role : " + "ROLE_USER");

        Member naverUser = Member.builder()
                .provider("naver")
                .providerId(String.valueOf(naverProfile.getResponse().getId()))
                .loginId("naver" + "_" + naverProfile.getResponse().getId())
                .loginPassword(cosKey)
                .email(naverProfile.getResponse().getEmail())
                .role("ROLE_USER")
                .build();

        log.info("[네이버]AccessToken 을 이용하여 네이 프로필 정보 요청 완료-------------------------------------------------");

        return naverUser;
    }

    /**
     * 미가입자만 체크해서 자동 회원가입
     */

    public void joinCheck(Member naverUser) {

        log.info("[네이버]회원가입 여부 체크 및 미가입자 자동 회원가입 처리 시작---------------------------------------------------");

        Member originMember = memberService.findMemberByLoginId(naverUser.getLoginId());

        if (originMember == null) {
            memberService.joinMember(naverUser);
            log.info("네이버 로그인이 최초입니다. 자동 회원가입되었습니다.");
        } else {
            log.info("네이버 로그인을 한적이 있습니다. 이미 회원가입 되어있습니다.");
        }

        log.info("[네이버]회원가입 여부 체크 및 미가입자 자동 회원가입 처리 완료---------------------------------------------------");
    }

    /**
     * 자동 로그인 처리
     */

    public void naverAutoLogin(Member naverUser) {
        log.info("[네이버]자동 로그인 시작---------------------------------------------------");

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(naverUser.getLoginId(), cosKey));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("[네이버]자동 로그인 완료---------------------------------------------------");
    }
}
