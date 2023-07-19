package bookstore24.v2.oauth;

import bookstore24.v2.auth.PrincipalDetails;
import bookstore24.v2.oauth.provider.GoogleUserInfo;
import bookstore24.v2.oauth.provider.KakaoUserInfo;
import bookstore24.v2.oauth.provider.NaverUserInfo;
import bookstore24.v2.oauth.provider.OAuth2UserInfo;
import bookstore24.v2.domain.Member;
import bookstore24.v2.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final MemberRepository memberRepository;

    /**
     * OAuth 서버로부터 받은 userRequest 데이터에 후처리되는 함수
     * 함수 종료시 @AuthenticationPrincipal 어노테이션이 만들어진다.
     * 오버라이딩 하지 않아도 loadUser는 정상적으로 발동을 한다. 그러면 왜 오버라이딩을 해주었는가?
     * 1. OAuth 로그인한 회원을 강제 회원가입 시키기 위해서.
     * 2. PrincipalDetails 타입으로 객체를 반환하기 위해서.
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("userRequest : " + userRequest.getClientRegistration()); // registration 으로 어떤 OAuth 로 로그인 했는지 확인 가능.
        System.out.println("getAccessToken : " + userRequest.getAccessToken().getTokenValue());

        OAuth2User oAuth2User = super.loadUser(userRequest);
        // OAuth 로그인 버튼 클릭 -> OAuth 로그인 창 -> 로그인을 완료 -> code 를 반환(OAuth2-Client 라이브러리가 받아줌) -> 해당 code 를 통해서 AccessToken 을 요청
        // userRequest 정보 -> loadUser 함수 호출(회원 프로필 받기) -> OAuth 로부터 회원 프로필 받아줌
        System.out.println("getAttributes : " + oAuth2User.getAttributes());

        // 회원가입을 강제로 진행 super.loadUser(userRequest).getAttributes() 값을 토대로.
        OAuth2UserInfo oAuth2UserInfo = null;

        if (userRequest.getClientRegistration().getRegistrationId().equals("google")) {
            System.out.println("구글 로그인 요청");
            oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
        } else if (userRequest.getClientRegistration().getRegistrationId().equals("naver")) {
            System.out.println("네이버 로그인 요청");
            oAuth2UserInfo = new NaverUserInfo((Map)oAuth2User.getAttributes().get("response"));
        } else if (userRequest.getClientRegistration().getRegistrationId().equals("kakao")) {
            System.out.println("카카오 로그인 요청");
            oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
        } else {
            System.out.println("BookStore24 는 소셜 로그인으로 구글, 네이버, 카카오를 지원합니다.");
        }

        String provider = oAuth2UserInfo.getProvider(); // google, naver, kakao
        String providerId = oAuth2UserInfo.getProviderId(); // 1251920512501298
        String loginId = provider + "_" + providerId;   // google_1251920512501298
        String loginPassword = bCryptPasswordEncoder.encode("BookStore24");
        String email = oAuth2UserInfo.getEmail();   // abcd@gmail.com
        String role = "ROLE_USER";

        Member memberEntity = memberRepository.findByLoginId(loginId);    // loginId가 일치하는 유저가 있는지

        if (memberEntity == null) {
            System.out.println(provider + " 로그인이 최초입니다.");
            memberEntity = Member.builder()
                    .loginId(loginId)
                    .loginPassword(loginPassword)
                    .email(email)
                    .role(role)
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            memberRepository.save(memberEntity);
        } else {
            System.out.println(provider + " 로그인을 이미 한적이 있습니다. 자동회원가입이 되어있습니다.");
        }

        return new PrincipalDetails(memberEntity, oAuth2User.getAttributes());
    }
}
