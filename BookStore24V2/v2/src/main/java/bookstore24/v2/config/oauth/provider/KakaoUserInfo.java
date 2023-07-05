package bookstore24.v2.config.oauth.provider;

import java.util.Map;

public class KakaoUserInfo implements OAuth2UserInfo{

    private Map<String, Object> attributes; // oauth2User.getAttributes
    private Map<String, Object> attributesAccount;   // 카카오 Attributes 를 확인해보았더니, email 은 kakao_account 안에 있음
    private Map<String, Object> attributesProfile;   // 카카오 Attributes 를 확인해보았더니, nickname 은 kakao_account 안의 profile 안에 있음

    public KakaoUserInfo(Map<String, Object> attributes) {
        /**
         * getAttributes : {
         * id=2873480678,
         * connected_at=2023-06-26T13:28:30Z,
         * properties={
         *             nickname=Mgyo Kim,
         *             profile_image=http://k.kakaocdn.net/dn/dpk9l1/btqmGhA2lKL/Oz0wDuJn1YV2DIn92f6DVK/img_640x640.jpg,
         *             thumbnail_image=http://k.kakaocdn.net/dn/dpk9l1/btqmGhA2lKL/Oz0wDuJn1YV2DIn92f6DVK/img_110x110.jpg
         *             },
         * kakao_account={
         *                profile_nickname_needs_agreement=false,
         *                profile_image_needs_agreement=false,
         *                profile={
         *                          nickname=Mgyo Kim,
         *                          thumbnail_image_url=http://k.kakaocdn.net/dn/dpk9l1/btqmGhA2lKL/Oz0wDuJn1YV2DIn92f6DVK/img_110x110.jpg,
         *                          profile_image_url=http://k.kakaocdn.net/dn/dpk9l1/btqmGhA2lKL/Oz0wDuJn1YV2DIn92f6DVK/img_640x640.jpg,
         *                          is_default_image=true
         *                          },
         *                has_email=true,
         *                email_needs_agreement=false,
         *                is_email_valid=true,
         *                is_email_verified=true,
         *                email=mgyokim@kakao.com}
         *                }
         */
        this.attributes = attributes;
        this.attributesAccount = (Map<String, Object>) attributes.get("kakao_account");
        this.attributesProfile = (Map<String, Object>) attributesAccount.get("profile");
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getProviderId() {
        return attributes.get("id").toString();
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getEmail() {
        return (String) attributesAccount.get("email");

    }

    @Override
    public String getName() {
        return (String) attributesProfile.get("nickname");
    }
}