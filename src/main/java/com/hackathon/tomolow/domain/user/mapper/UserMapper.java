package com.hackathon.tomolow.domain.user.mapper;

import org.springframework.stereotype.Component;

import com.hackathon.tomolow.domain.user.dto.response.SignUpResponse;
import com.hackathon.tomolow.domain.user.entity.User;

@Component // 스프링 빈으로 등록되는 클래스 (DI 대상)
public class UserMapper {

  // User 엔티티를 SignUpResponse DTO로 변환하는 메서드
  public static SignUpResponse toSignUpResponse(User user) {
    return SignUpResponse.builder()
        .userId(user.getId())
        .username(user.getUsername())
        .build(); // DTO 객체 생성
  }
}
