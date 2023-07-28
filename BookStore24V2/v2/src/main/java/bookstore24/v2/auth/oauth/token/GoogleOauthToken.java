package bookstore24.v2.auth.oauth.token;

import lombok.Data;

@Data
public class GoogleOauthToken {   // 구글
    private String access_token;
    private String token_type;
    private String refresh_token;
    private String expires_in;
    private String scope;
    private String id_token;
}
