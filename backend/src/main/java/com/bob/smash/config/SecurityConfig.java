package com.bob.smash.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${front.server.url}")
    private String frontServerUrl;  // application.properties에서 값 주입

    
    @Autowired
    private CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

    // SecurityFilterChain 빈 등록
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("@@@ filterChain Bean 등록됨!");
        
        http
            .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())) // JS에서도 읽을 수 있게
            .formLogin(form -> form.disable()) // 기본 폼 로그인 비활성화
            .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화 (API 서버에서는 보통 비활성화)
            .cors(Customizer.withDefaults()) // CORS 설정 적용
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/**").permitAll()
                
                // .anyRequest() // 이외 모든 요청에 대해
                // .authenticated() : 요청에 대해 인증 필요
                // .permitAll() : 요청에 대해 인증 불필요
            )
            // 로그인 설정 (커스텀)
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login-page")
                .successHandler(customOAuth2SuccessHandler)
                .failureUrl(frontServerUrl + "/profile")  // 로그인 실패 시 이동할 URL
            )
            // 로그아웃 설정
            .logout(logout -> logout
                .logoutUrl("/logout")
                .permitAll() // 로그아웃 URL 허용
                .logoutSuccessHandler((req, res, auth) -> {
                    // 로그아웃 후 원하는 로직 수행
                    // res.sendRedirect(frontServerUrl + "/"); // 프론트 첫 화면으로 리디렉션
                    res.setStatus(HttpServletResponse.SC_OK); // redirect 하지 않고 200 OK 반환
                })
                .invalidateHttpSession(true) // 세션 무효화 (SecurityContextLogoutHandler 포함)
                .deleteCookies("JSESSIONID") // 쿠키 삭제
                .permitAll()
            );

        return http.build();
    }

    // CORS 설정 빈 등록
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));  // 프론트 주소
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 허용 메서드
        configuration.setAllowedHeaders(List.of("*"));  // 허용 헤더 (필요시 구체적으로 설정 가능)
        configuration.setAllowCredentials(true); // 쿠키, 인증 정보 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // 모든 경로에 적용
        return source;
    }
}
