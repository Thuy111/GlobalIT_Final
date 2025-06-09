import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/smash/member/**").authenticated()  // 이 API만 인증 필요
                .anyRequest().permitAll() // 나머지는 전부 허용
            )
            // .oauth2Login(); // OAuth2 로그인 사용 (구형방식.사라질 예정)
            .oauth2Login(Customizer.withDefaults()); 

        return http.build();
    }
}
