package com.hackathon.tomolow.domain.user.exception;

import com.hackathon.tomolow.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements BaseErrorCode {
  USERNAME_ALREADY_EXISTS("USER_4001", "이미 존재하는 사용자 아이디입니다.", HttpStatus.BAD_REQUEST),
  //PASSWORD_REQUIRED("USER_4002", "비밀번호는 필수입니다.", HttpStatus.BAD_REQUEST),
  USER_NOT_FOUND("USER_4003", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
  INVALID_PASSWORD("USER_4005", "현재 비밀번호와 입력한 비밀번호가 다릅니다.", HttpStatus.BAD_REQUEST);


  private final String code; // 에러 코드 문자열
  private final String message; // 에러 메시지
  private final HttpStatus status; // HTTP 상태 코드
}