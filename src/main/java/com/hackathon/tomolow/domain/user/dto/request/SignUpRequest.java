package com.hackathon.tomolow.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "SignUpRequest DTO", description = "사용자 회원가입을 위한 데이터 전송")
public class SignUpRequest {

  @NotBlank(message = "사용자 아이디 항목은 필수입니다.")
  @Schema(description = "사용자 아이디", example = "user01")
  private String username;

  @NotBlank(message = "비밀번호 항목은 필수입니다.")
  @Pattern(
      regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,}$",
      message = "비밀번호는 영문, 숫자, 특수문자 포함 8자 이상 작성해야 합니다.")
  @Schema(description = "비밀번호", example = "password01!")
  private String password;

  @NotBlank(message = "이름는 필수입니다.")
  @Schema(description = "이름", example = "김모투")
  private String name;

  @NotBlank(message = "닉네임는 필수입니다.")
  @Schema(description = "닉네임", example = "모투모투")
  private String nickname;
}
