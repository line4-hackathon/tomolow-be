package com.hackathon.tomolow.global.config;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

  @Value("${cors.allowed-origins}")
  private String[] allowedOrigins;

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    // 운영에선 명시적 Origin 화이트리스트
    config.setAllowedOrigins(Arrays.asList(allowedOrigins));

    // 필요 메서드만 열기(운영 보안); dev에선 *도 가능
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

    // 필요한 헤더만 허용(운영 보안); dev에선 *도 가능
    config.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "Accept"));

    // 인증정보(쿠키/Authorization) 전달 허용
    config.setAllowCredentials(true);

    // 클라이언트에서 읽을 수 있는 응답 헤더
    config.setExposedHeaders(List.of("Authorization"));

    // Preflight 캐시 1시간(과도한 OPTIONS 감소)
    config.setMaxAge(Duration.ofHours(1));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
