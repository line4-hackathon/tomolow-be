package com.hackathon.tomolow.domain.userGroup.exception;

import org.springframework.http.HttpStatus;

import com.hackathon.tomolow.global.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserGroupErrorCode implements BaseErrorCode {
  USER_GROUP_NOT_FOUND("USER_GROUP_4004", "그룹 내 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

  private final String code; // 에러 코드 문자열
  private final String message; // 에러 메시지
  private final HttpStatus status; // HTTP 상태 코드
}
