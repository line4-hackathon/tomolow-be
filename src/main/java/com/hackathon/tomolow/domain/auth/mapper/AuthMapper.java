package com.hackathon.tomolow.domain.auth.mapper;

import org.springframework.stereotype.Component;

import com.hackathon.tomolow.domain.auth.dto.response.LoginResponse;
import com.hackathon.tomolow.domain.user.entity.User;

@Component // Spring Bean으로 등록
public class AuthMapper {

  public LoginResponse toLoginResponse(User user, String accessToken, Long expirationTime) {
    return LoginResponse.builder()
        .accessToken(accessToken) // 발급된 액세스 토큰
        .userId(user.getId()) // 사용자 ID
        .username(user.getUsername()) // 사용자 이름
        .name(user.getName())
        .nickname(user.getNickname())
        .role(user.getRole()) // 사용자 권한
        .expirationTime(expirationTime) // 토큰 만료 시간
        .build(); // DTO 반환
  }
}
