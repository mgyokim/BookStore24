package bookstore24.v2.auth.oauth.logic;

import bookstore24.v2.auth.oauth.profile.GoogleProfile;
import bookstore24.v2.auth.oauth.token.GoogleOauthToken;
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
public class GoogleLogic {

    private final MemberService memberService;

    private final AuthenticationManager authenticationManager;

    @Value("${cos.key}")
    private String cosKey;

    @Value(("${spring.security.oauth2.client.registration.google.client-id}"))
    private String clientId;

    @Value(("${spring.security.oauth2.client.registration.google.client-secret}"))
    private String clientSecret;

    final String GOOGLE_REDIRECT_URI = "http://bookstore24.shop/auth/google/callback";

    final String GOOGLE_TOKEN_REQUEST_URI = "https://oauth2.googleapis.com/token";

    final String GOOGLE_PROFILE_REQUEST_URI = "https://www.googleapis.com/oauth2/v1/userinfo";

    /**
     * 구글 인가 코드 받기 (LoginApiController.googleLogin() 에서 처리)
     */
    // 요청 URL
    // https://accounts.google.com/o/oauth2/v2/auth?client_id=766446517759-t82jo5h4vk9rmj30bld1d30su7sqdde1.apps.googleusercontent.com&redirect_uri=http://bookstore24.shop/auth/google/callback&response_type=code&scope=openid%20email%20profile

    /**
     * 발급받은 인가 코드로 토큰 요청하기
     */
    public GoogleOauthToken codeToToken(String code) {

        log.info("[구글]발급받은 인가 코드로 토큰 요청 시작-----------------------------------------------------------------");

        // POST 방식으로 key=value 데이터를 요청(구글쪽으로)
        // 사용 라이브러리 - RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // HttpHeader 오브젝트 생성
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/x-www-form-urlencoded");   // 내가 지금 전달할 데이터가 key=value 형태임을 알려주는 것.

        // HttpBody 오브젝트 생성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", GOOGLE_REDIRECT_URI);
        params.add("grant_type", "authorization_code");

        // HttpHeader 와 HttpBody 를 하나의 HttpEntity 오브젝트에 담기 -> 이렇게 해주는 이유는 아래의 restTemplate.exchange() 가 파라미터로 HttpEntity 를 받게 되있기 때문.
        HttpEntity<MultiValueMap<String, String>> googleTokenRequest = new HttpEntity<>(params, httpHeaders);

        // Http 요청하기 - POST 방식으로 - 그리고 response 변수로 응답받음
        ResponseEntity<String> response = restTemplate.exchange(
                GOOGLE_TOKEN_REQUEST_URI,  // 토큰 발급 요청 주소
                HttpMethod.POST,    // 토큰 발급 요청 메서드는 구글문서상의 POST
                googleTokenRequest,  // HttpBody 에 들어갈 데이터와, HttpHeader 값을 한번에 넣어줌
                String.class    // 응답받을 타입을 String 으로 지정
        );

        // ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        GoogleOauthToken googleOauthToken = null;   // 구글 토큰 응답 데이터를 통째로 저장할 곳

        try {
            googleOauthToken = objectMapper.readValue(response.getBody(), GoogleOauthToken.class);  // Json 데이터를 자바로 처리하기 위해 자바 오브젝트로 바꿈.
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        log.info("구글 토큰 : " + googleOauthToken);
        log.info("[구글]발급받은 인가 코드로 토큰 요청 완료-----------------------------------------------------------------");

        return googleOauthToken;
    }

    /**
     * 발급받은 AccessToken 을 이용하여 구글 프로필 정보 요청하기
     *
     * @return
     */

    public Member accessTokenToProfile(GoogleOauthToken googleOauthToken) {

        log.info("[구글]AccessToken 을 이용하여 구글 프로필 정보 요청 시작-------------------------------------------------");

        // 구글 토큰 응답 데이터를 각 변수에 저장
        String google_access_token = googleOauthToken.getAccess_token();
        String google_token_type = googleOauthToken.getToken_type();
        String google_refresh_token = googleOauthToken.getRefresh_token();
        String google_expires_in = googleOauthToken.getExpires_in();
        String google_scope = googleOauthToken.getScope();
        String google_id_token = googleOauthToken.getId_token();

        RestTemplate restTemplate = new RestTemplate();

        // HttpHeader 오브젝트 생성
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + google_access_token);   // 발급받았던 AccessToken을 프로필 정보 요청에 사용
        httpHeaders.add("Content-Type", "application/x-www-form-urlencoded");  // 내가 지금 전달할 데이터가 key=value 형태임을 알려주는 것.

        // HttpHeader 와 HttpBody 를 하나의 HttpEntity 오브젝트에 담기 -> 이렇게 해주는 이유는 아래의 restTemplate.exchange() 가 파라미터로 HttpEntity 를 받게 되있기 때문.
        HttpEntity<MultiValueMap<String, String>> googleProfileRequest = new HttpEntity<>(httpHeaders);

        // Http 요청하기 - POST 방식으로 - 그리고 response 변수로 응답받음
        ResponseEntity<String> response = restTemplate.exchange(
                GOOGLE_PROFILE_REQUEST_URI,    // 구글 문서상의 프로필 정보 요청 주소
                HttpMethod.GET,    // 구글
                googleProfileRequest,
                String.class
        );

        // ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        GoogleProfile googleProfile = null;

        try {
            googleProfile = objectMapper.readValue(response.getBody(), GoogleProfile.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        log.info("provider : " + "google");
        log.info("providerId : " + googleProfile.getId());
        log.info("loginId : " + "google" + "_" + googleProfile.getId());
        log.info("loginPassword : " + cosKey);
        log.info("email : " + googleProfile.getEmail());
        log.info("role : " + "ROLE_USER");

        Member googleUser = Member.builder()
                .provider("google")
                .providerId(String.valueOf(googleProfile.getId()))
                .loginId("google" + "_" + googleProfile.getId())
                .loginPassword(cosKey)
                .email(googleProfile.getEmail())
                .role("ROLE_USER")
                .build();

        log.info("[구글]AccessToken 을 이용하여 구글 프로필 정보 요청 완료-------------------------------------------------");

        return googleUser;
    }

    /**
     * 미가입자만 체크해서 자동 회원가입
     */

    public Member joinCheck(Member googleUser) {

        log.info("[구글] Oauth 이메일 기존회원의 이메일과 중복 여부 체크 및 미중복자 자동 회원가입 처리 시작---------------------------------------------------");

        Member duplicateEmailMember = memberService.findMemberByEmail(googleUser.getEmail());

        if (duplicateEmailMember == null) {
            memberService.joinMember(googleUser);
            Member joinedMember = memberService.findMemberByEmail(googleUser.getEmail());
            log.info("구글 로그인이 최초입니다. 자동 회원가입되었습니다.");
            log.info("[구글] Oauth 이메일 기존회원의 이메일과 중복 여부 체크 및 미중복자 자동 회원가입 처리 완료---------------------------------------------------");
            return joinedMember;
        }
        if ((duplicateEmailMember != null) & (duplicateEmailMember.getProvider() == "google")) {
            log.info("구글 로그인을 한적이 있습니다. 이미 회원가입 되어있습니다.");
            log.info("[구글] Oauth 이메일 기존회원의 이메일과 중복 여부 체크 및 미중복자 자동 회원가입 처리 완료---------------------------------------------------");
            return duplicateEmailMember;
        } else {
            String provider = duplicateEmailMember.getProvider();
            log.info(googleUser.getEmail() + " 은 " + provider + " 로그인 방식으로 이미 가입된 이메일입니다. " + provider + " 로그인 방식으로 로그인을 시도하세요.");
            log.info("[구글] Oauth 이메일 기존회원의 이메일과 중복 여부 체크 및 미중복자 자동 회원가입 처리 완료---------------------------------------------------");
            googleUser.setLoginId(null);    // 컨트롤러에서 로그인 처리를 하지 않기 위한 용도
            googleUser.setProvider(provider);
            return googleUser;
        }

    }

    /**
     * 자동 로그인 처리
     */

    public void googleAutoLogin(Member googleUser) {
        log.info("[구글]자동 로그인 시작---------------------------------------------------");

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(googleUser.getLoginId(), cosKey));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("[구글]자동 로그인 완료---------------------------------------------------");
    }
}
