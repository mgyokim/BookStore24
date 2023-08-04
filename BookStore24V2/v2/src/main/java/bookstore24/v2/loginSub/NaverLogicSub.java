package bookstore24.v2.loginSub;

import bookstore24.v2.auth.oauth.dto.profile.NaverProfileDto;
import bookstore24.v2.auth.oauth.dto.token.NaverOauthTokenDto;
import bookstore24.v2.domain.Member;
import bookstore24.v2.auth.jwt.JwtProperties;
import bookstore24.v2.member.service.MemberService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class NaverLogicSub {

    private final MemberService memberService;

    private final AuthenticationManager authenticationManager;

    @Value("${cos.key}")
    private String cosKey;

    @Value(("${spring.security.oauth2.client.registration.naver.client-id}"))
    private String clientId;

    @Value(("${spring.security.oauth2.client.registration.naver.client-secret}"))
    private String clientSecret;

    final String NAVER_REDIRECT_URI = "http://localhost:8080/auth/naver/callback";    // 로컬 개발용

    final String NAVER_TOKEN_REQUEST_URI = "https://nid.naver.com/oauth2.0/token";

    final String NAVER_PROFILE_REQUEST_URI = "https://openapi.naver.com/v1/nid/me";

    /**
     * 네이버 인가 코드 받기 (MemberApiController.naverLogin() 에서 처리)
     */
    // 요청 URL
    // 프론트에서
    // https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=B3RGNtinEp3Va8fysxkN&redirect_uri=http://localhost:3000/auth/naver&state='test'
    // 로 요청하면 인가코드 획득함.
    // 획득한 인가코드를 서버IP/auth/naver/callback?Authorization_code={} 로 POST 요청하면 서버가 인가코드 획득함.

    /**
     * 발급받은 인가 코드로 토큰 요청하기
     */
    public NaverOauthTokenDto codeToToken(String code) {

        log.info("[START] - NaverLogicSub.codeToToken / 네이버에서 발급받아 클라이언트가 요청으로 보낸 [Authorization_code : " + code + "] 를 이용하여 토큰 요청하기 시작  ---------------------------------------------------------------------------------");

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
        NaverOauthTokenDto naverOauthTokenDto = null;   // 네이 토큰 응답 데이터를 통째로 저장할 곳

        try {
            naverOauthTokenDto = objectMapper.readValue(response.getBody(), NaverOauthTokenDto.class);  // Json 데이터를 자바로 처리하기 위해 자바 오브젝트로 바꿈.
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        log.info("클라이언트가 보낸 [Authorization_code : " + code + "] 를 이용하여 발급받은 [naverOauthTokenDto : " + naverOauthTokenDto + "]----------------------------------------------------------------------------------------------------------");
        log.info("[END] - NaverLogicSub.codeToToken / 네이버에서 발급받아 클라이언트가 요청으로 보낸 [Authorization_code : " + code + "] 를 이용하여 토큰 요청하기 완료  ---------------------------------------------------------------------------------");

        return naverOauthTokenDto;
    }

    /**
     * 발급받은 AccessToken 을 이용하여 네이버 프로필 정보 요청하기
     *
     * @return
     */

    public Member accessTokenToProfile(NaverOauthTokenDto naverOauthTokenDto) {

        log.info("[START] - NaverLogicSub.accessTokenToProfile / 네이버에서 발급받은 토큰 [naverOauthTokenDto : " + naverOauthTokenDto + "] 를 이용하여 프로필 정보 요청하기 시작  ---------------------------------------------------------------------------------");

        // 네이버 토큰 응답 데이터를 각 변수에 저장
        String naver_access_token = naverOauthTokenDto.getAccess_token();
        String naver_token_type = naverOauthTokenDto.getToken_type();
        String naver_expires_in = naverOauthTokenDto.getExpires_in();
        String naver_refresh_token = naverOauthTokenDto.getRefresh_token();
        String naver_scope = naverOauthTokenDto.getScope();
        String naver_refresh_token_expires_in = naverOauthTokenDto.getRefresh_token_expires_in();

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
        NaverProfileDto naverProfileDto = null;

        try {
            naverProfileDto = objectMapper.readValue(response.getBody(), NaverProfileDto.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        log.info("네이버로부터 응답받은 프로필 정보 [naverProfileDto : " + naverProfileDto + "]");

        Member naverUser = Member.builder()
                .provider("naver")
                .providerId(String.valueOf(naverProfileDto.getResponse().getId()))
                .loginId("naver" + "_" + naverProfileDto.getResponse().getId())
                .loginPassword(cosKey)
                .email(naverProfileDto.getResponse().getEmail())
                .role("ROLE_USER")
                .build();

        log.info("프로필 정보를 이용하여 네이버 자동 회원가입용 객체 생성 [naverUser : " + naverUser + "]");
        log.info("naverUser.provider : " + naverUser.getProvider());
        log.info("naverUser.providerId : " + naverUser.getProviderId());
        log.info("naverUser.loginId : " + naverUser.getLoginId());
        log.info("naverUser.loginPassword : " + naverUser.getLoginPassword());
        log.info("naverUser.email : " + naverUser.getEmail());
        log.info("naverUser.role : " + naverUser.getRole());

        log.info("[END] - NaverLogicSub.accessTokenToProfile / 네이에서 발급받은 토큰 [naverOauthTokenDto : " + naverOauthTokenDto + "] 를 이용하여 프로필 정보 요청하기 완료  ---------------------------------------------------------------------------------");

        return naverUser;
    }

    /**
     * 미가입자만 체크해서 자동 회원가입
     */

    public Member joinCheck(Member naverUser) {

        log.info("[START] - NaverLogicSub.joinCheck / [email : " + naverUser.getEmail() + "]  email 중복여부 체크 및 회원가입 로직 시작 ----------------------------------------------------------------------------------------------------------------------------------------------------------");

        Member duplicateEmailMember = memberService.findMemberByEmail(naverUser.getEmail());

        if (duplicateEmailMember == null) {
            memberService.joinMember(naverUser);
            Member joinedMember = memberService.findMemberByEmail(naverUser.getEmail());

            log.info("네이버 로그인이 최초입니다. 자동 회원가입되었습니다.");
            log.info("[END] - NaverLogicSub.joinCheck / [email : " + naverUser.getEmail() + "]  email 중복여부 체크 및 회원가입 로직 종료 ----------------------------------------------------------------------------------------------------------------------------------------------------------");

            return joinedMember;
        }
        if ((duplicateEmailMember != null) & (duplicateEmailMember.getProvider() == "naver")) {
            log.info("네이버 로그인을 한적이 있습니다. 이미 회원가입 되어있습니다.");
            log.info("[END] - NaverLogicSub.joinCheck / [email : " + naverUser.getEmail() + "]  email 중복여부 체크 및 회원가입 로직 종료 ----------------------------------------------------------------------------------------------------------------------------------------------------------");

            return duplicateEmailMember;
        } else {
            String provider = duplicateEmailMember.getProvider();

            log.info(naverUser.getEmail() + " 은 " + provider + " 로그인 방식으로 이미 가입된 이메일입니다. " + provider + " 로그인 방식으로 로그인을 시도하세요.");

            naverUser.setLoginId(null);     // 컨트롤러에서 로그인 처리를 하지 않기 위한 용도
            naverUser.setProvider(provider);

            log.info("[END] - NaverLogicSub.joinCheck / [email : " + naverUser.getEmail() + "]  email 중복여부 체크 및 회원가입 로직 종료 ----------------------------------------------------------------------------------------------------------------------------------------------------------");
            return naverUser;
        }

    }

    /**
     * 자동 로그인 처리
     */

    public ResponseEntity<String> naverAutoLogin(Member naverUser) {
        log.info("[START] - NaverLogicSub.naverAutoLogin / [email : " + naverUser.getEmail() + "]  해당 회원은 Naver 로 회원가입 되어있으므로 자동 로그인 로직 시작 ----------------------------------------------------------------------------------------------------------------------------------------------------------");

        // 네이버 로그인 요청 회원 데이터
        String loginId = naverUser.getLoginId();
        log.info("Request loginId : " + loginId);
        String loginPassword = cosKey;
        log.info("Request loginPassword : " + loginPassword);

        // JSON 데이터로 변환
        String jsonData = "{\"loginId\":\"" + loginId + "\", \"loginPassword\":\"" + loginPassword + "\"}";

        // 요청 헤더 설정
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        // 요청 바디와 헤더를 포함하는 HttpEntity 생성
        HttpEntity<String> requestEntity = new HttpEntity<>(jsonData, httpHeaders);

        // RestTemplate 생성
        RestTemplate restTemplate = new RestTemplate();

        // /login 컨트롤러로 POST 요청 보내기
        String url = "http://localhost:8080/login"; // 로컬 통신 엔드포인트 URL
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        // 응답 결과 처리
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            HttpHeaders responseEntityHeaders = responseEntity.getHeaders();
            log.info("로그인 성공 응답 데이터 헤더 : " + responseEntityHeaders);
            log.info("[END] - NaverLogicSub.naverAutoLogin / [email : " + naverUser.getEmail() + "]  해당 회원은 Naver 로 회원가입 되어있으므로 자동 로그인 로직 종료 ----------------------------------------------------------------------------------------------------------------------------------------------------------");
            return responseEntity;
        } else {
            log.info("로그인 실패 상태 코드 : " + responseEntity.getStatusCodeValue());
        }
        return null;
    }

    public ResponseEntity<String> naverAutoLoginFail(String email, String provider) {
        log.info("[START] - NaverLogicSub.naverAutoLoginFail / [email : " + email + "] 해당 회원은 " + provider + " 로 회원가입 되어있으므로 자동 로그인 실패 응답 시작 ----------------------------------------------------------------------------------------------------------------------------------------------------------");


        HttpHeaders httpHeaders = new HttpHeaders();

        String loginFailJwt = JWT.create()
                .withSubject("bookstore24LoginFailToken")    // 토큰 제목
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.EXPIRATION_TIME))  // 토큰 만료 일자
                .withClaim("email", email) // Private claim
                .withClaim("provider", provider)  // Private claim
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));    // 토큰 사인

        httpHeaders.set(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + loginFailJwt);

        log.info("loginFailJwt = " + JwtProperties.TOKEN_PREFIX + loginFailJwt);
        log.info("[END] - NaverLogicSub.naverAutoLoginFail / [email : " + email + "] 해당 회원은 " + provider + " 로 회원가입 되어있으므로 자동 로그인 실패 응답 완료 ----------------------------------------------------------------------------------------------------------------------------------------------------------");
        return new ResponseEntity<>("naver Auto Login failed. Cause : Duplicated Email.", httpHeaders, HttpStatus.UNAUTHORIZED);
    }
}
