package com.bob.smash.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // Frontend와의 CORS 설정을 위한 클래스
    // 개발용. 배포시에는 명확한 도메인만 허용하도록 변경 필요
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 모든 경로 허용 (필요한api에 대해서만 허용하는 게 안전, 예: /smash/**)
            .allowedOrigins("http://localhost:5173") // 프론트 주소("*"사용 금지)
            .allowedMethods("*")    // 모든 HTTP 메서드 허용 (GET, POST, PUT, DELETE 등) 보통 GET, POST만 허용하는 게 안전
            .allowedHeaders("*")    // 모든 헤더 허용 (Authorization, Content-Type, X-CSRFToken 정도만 허용하는 게 안전)
            .allowCredentials(true); // ★ withCredentials(자격증명) 허용
    }
}
