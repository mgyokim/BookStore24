package bookstore24.v2.auth.jwt;

public interface JwtProperties {
    String SECRET = "bookstore24";  // 서버만 알고 있는 비밀값
    int EXPIRATION_TIME = 60000 * 60; // 60분 (1/1000초)
    String TOKEN_PREFIX = "Bearer ";
    String HEADER_STRING = "Authorization";
}
