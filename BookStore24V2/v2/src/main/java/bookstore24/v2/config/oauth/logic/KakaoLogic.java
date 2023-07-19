package bookstore24.v2.config.oauth.logic;

import bookstore24.v2.config.oauth.profile.KakaoProfile;
import bookstore24.v2.config.oauth.token.KakaoOauthToken;
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
public class KakaoLogic {

    private final MemberService memberService;

    private final AuthenticationManager authenticationManager;

    @Value("${cos.key}")
    private String cosKey;

    @Value(("${spring.security.oauth2.client.registration.kakao.client-id}"))
    private String clientId;

    /**
     * 카카오 인가 코드 받기 (LoginApiController.kakaoLogin() 에서 처리)
     */
    // 요청 URL
    // https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=e435f34295d28879dfabc32de2bd7546&redirect_uri=http://bookstore24.shop/auth/kakao/callback


    /**
     * 발급받은 인가 코드로 토큰 요청하기
     */
    public KakaoOauthToken codeToToken(String code) {

        log.info("[카카오]발급받은 인가 코드로 토큰 요청 시작-----------------------------------------------------------------");

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
        KakaoOauthToken kakaoOauthToken = null;   // 카카오 토큰 응답 데이터를 통째로 저장할 곳

        try {
            kakaoOauthToken = objectMapper.readValue(response.getBody(), KakaoOauthToken.class);  // Json 데이터를 자바로 처리하기 위해 자바 오브젝트로 바꿈.
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        log.info("카카오 토큰 : " + kakaoOauthToken);
        log.info("[카카오]발급받은 인가 코드로 토큰 요청 완료-----------------------------------------------------------------");

        return kakaoOauthToken;
    }

    /**
     * 발급받은 AccessToken 을 이용하여 카카오 프로필 정보 요청하기
     */

    public Member accessTokenToProfile(KakaoOauthToken kakaoOauthToken) {

        log.info("[카카오]AccessToken 을 이용하여 카카오 프로필 정보 요청 시작-------------------------------------------------");

        // 카카오 토큰 응답 데이터를 각 변수에 저장
        String kakao_access_token = kakaoOauthToken.getAccess_token();
        String kakao_token_type = kakaoOauthToken.getToken_type();
        String kakao_expires_in = kakaoOauthToken.getExpires_in();
        String kakao_refresh_token = kakaoOauthToken.getRefresh_token();
        String kakao_scope = kakaoOauthToken.getScope();
        String kakao_refresh_token_expires_in = kakaoOauthToken.getRefresh_token_expires_in();

        RestTemplate restTemplate2 = new RestTemplate();

        // HttpHeader 오브젝트 생성
        HttpHeaders httpHeaders2 = new HttpHeaders();
        httpHeaders2.add("Authorization", "Bearer " + kakao_access_token);   // 발급받았던 AccessToken을 프로필 정보 요청에 사용
        httpHeaders2.add("Content-Type", "application/x-www-form-urlencoded");  // 내가 지금 전달할 데이터가 key=value 형태임을 알려주는 것.

        // HttpHeader 와 HttpBody 를 하나의 HttpEntity 오브젝트에 담기 -> 이렇게 해주는 이유는 아래의 restTemplate.exchange() 가 파라미터로 HttpEntity 를 받게 되있기 때문.
        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest = new HttpEntity<>(httpHeaders2);

        // Http 요청하기 - POST 방식으로 - 그리고 reponse2 변수로 응답받음
        ResponseEntity<String> response2 = restTemplate2.exchange(
                "https://kapi.kakao.com/v2/user/me",    // 카카오 문서상의 프로필 정보 요청 주소
                HttpMethod.POST,    // 카카오
                kakaoProfileRequest,
                String.class
        );

        // ObjectMapper
        ObjectMapper objectMapper2 = new ObjectMapper();
        KakaoProfile kakaoProfile = null;

        try {
            kakaoProfile = objectMapper2.readValue(response2.getBody(), KakaoProfile.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        log.info("provider : " + "kakao");
        log.info("providerId : " + kakaoProfile.getId());
        log.info("loginId : " + "kakao" + "_" + kakaoProfile.getId());
        log.info("loginPassword : " + cosKey);
        log.info("email : " + kakaoProfile.getKakao_account().getEmail());
        log.info("role : " + "ROLE_USER");

        Member kakaoUser = Member.builder()
                .provider("kakao")
                .providerId(String.valueOf(kakaoProfile.getId()))
                .loginId("kakao" + "_" + kakaoProfile.getId())
                .loginPassword(cosKey)
                .email(kakaoProfile.getKakao_account().getEmail())
                .role("ROLE_USER")
                .build();

        log.info("[카카오]AccessToken 을 이용하여 카카오 프로필 정보 요청 완료-------------------------------------------------");

        return kakaoUser;
    }

    /**
     * 미가입자만 체크해서 자동 회원가입
     */
    public void joinCheck(Member kakaoUser) {

        log.info("[카카오]회원가입 여부 체크 및 미가입자 자동 회원가입 처리 시작---------------------------------------------------");

        Member originMember = memberService.findMemberByLoginId(kakaoUser.getLoginId());

        if (originMember == null) {
            memberService.joinMember(kakaoUser);
            log.info("카카오 로그인이 최초입니다. 자동 회원가입되었습니다.");
        } else {
            log.info("카카오 로그인을 한적이 있습니다. 이미 회원가입 되어있습니다.");
        }

        log.info("[카카오]회원가입 여부 체크 및 미가입자 자동 회원가입 처리 완료---------------------------------------------------");
    }

    /**
     * 자동 로그인 처리
     */
    public void kakaoAutoLogin(Member kakaoUser) {
        log.info("[카카오]자동 로그인 시작---------------------------------------------------");

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(kakaoUser.getLoginId(), cosKey));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("[카카오]자동 로그인 완료---------------------------------------------------");
    }
}
