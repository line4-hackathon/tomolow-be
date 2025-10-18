package com.hackathon.tomolow.domain.auth.dto.response;

import com.hackathon.tomolow.domain.user.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(title = "LoginResponse DTO", description = "사용자 로그인에 대한 응답 반환")
public class LoginResponse {

  @Schema(description = "사용자 Access Token")
  private String accessToken;

  @Schema(description = "사용자 ID", example = "1")
  private Long userId;

  @Schema(description = "사용자 아이디 또는 이메일", example = "user01")
  private String username;

  @Schema(description = "사용자 이름", example = "김모투")
  private String name;

  @Schema(description = "사용자 닉네임", example = "모투모투")
  private String nickname;

  @Schema(description = "사용자 권한", example = "USER")
  private Role role;

  @Schema(description = "사용자 Access Token 만료 시각", example = "1800000")
  private Long expirationTime;
}