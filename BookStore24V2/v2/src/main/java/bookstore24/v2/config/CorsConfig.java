package bookstore24.v2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);   // 내 서버가 응답을 할 때 json 을 자바스크립트에서 처리할 수 있게 할지를 설정하는 것
//        config.addAllowedOrigin("*");   // 모든 ip에 응답을 허용하겠다
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://localhost:8080");
        config.addAllowedOrigin("http://bookstore24.shop");
        config.addAllowedOrigin("https://yundevingv.github.io");
        config.addAllowedHeader("*");   // 모든 header 에 응답을 허용하겠다.
        config.addAllowedMethod("*");   // 모든 post, get, put, delete, patch 요청을 허용하겠다.
        config.addExposedHeader("Authorization");

        // 모든 URL 에 대해 CORS 를 허용하도록 설정
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
