package bookstore24.v2.oauth.token;

import lombok.Data;

@Data
public class NaverOauthToken {  // 네이버
    private String access_token;
    private String token_type;
    private String refresh_token;
    private String expires_in;
    private String scope;
    private String refresh_token_expires_in;
}
