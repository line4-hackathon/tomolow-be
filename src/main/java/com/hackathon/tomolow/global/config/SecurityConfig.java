package com.hackathon.tomolow.global.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.hackathon.tomolow.global.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CorsConfig corsConfig;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // CSRF 완전 비활성화 (JWT/Stateless 환경)
        .csrf(AbstractHttpConfigurer::disable)
        // CORS 설정 활성화(보통은 CORS 설정 활성화 하지 않음. 서버에서 NginX로 CORS 검증)
        .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
        // HTTP Basic 인증 기본 설정
        // .httpBasic(Customizer.withDefaults())
        // 세션을 생성하지 않음 (JWT 사용으로 인한 Stateless 설정)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        // HTTP 요청에 대한 권한 설정
        .authorizeHttpRequests(
            request ->
                request
                    // Preflight 전면 허용 (가장 위에!)
                    .requestMatchers(HttpMethod.OPTIONS, "/**")
                    .permitAll()
                    // WebSocket 핸드셰이크 & STOMP 브로커 경로 허용
                    .requestMatchers("/ws/**", "/ws-sockjs/**", "/topic/**", "/queue/**", "/app/**")
                    .permitAll()
                    // Swagger 경로 인증 필요
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                    .permitAll()
                    // 인증 없이 허용할 경로
                    .requestMatchers("/api/auth/**", "/api/ticker/**")
                    .permitAll()
                    // 개발자 전용 API — 지금은 임시로 공개
                    .requestMatchers("/api/dev/**")
                    .permitAll()
                    // 정적 리소스 (공통 위치: /static, /public, /resources, /META-INF/resources)
                    .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                    .permitAll()
                    // 우리가 쓰는 파일/경로 추가 허용
                    .requestMatchers(
                        "/",
                        "/index.html",
                        "/test.html",
                        "/interest-test.html",
                        "/holding-test.html",
                        "holding-ws-test.html",
                        "/favicon.ico")
                    .permitAll()
                    .requestMatchers("/js/**", "/css/**", "/images/**", "/webjars/**")
                    .permitAll()

                    // 그 외 모든 요청은 모두 인증 필요
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  /** 비밀번호 인코더 Bean 등록 */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /** 인증 관리자 Bean 등록 */
  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }
}
