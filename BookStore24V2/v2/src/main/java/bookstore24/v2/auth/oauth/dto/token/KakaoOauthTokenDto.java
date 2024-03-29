package bookstore24.v2.auth.oauth.dto.token;

import lombok.Data;

@Data
public class KakaoOauthTokenDto {   // 카카오
    private String access_token;
    private String token_type;
    private String refresh_token;
    private String expires_in;
    private String scope;
    private String refresh_token_expires_in;
}
