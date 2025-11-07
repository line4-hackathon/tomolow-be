package com.hackathon.tomolow.domain.chat.exception;

import org.springframework.http.HttpStatus;

import com.hackathon.tomolow.global.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChatErrorCode implements BaseErrorCode {
  EXTERNAL_API_ERROR(
      "CHAT_4001", "외부 API와 연결에 실패했거나 응답이 유효하지 않습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  JSON_MAPPING_ERROR("CHAT_4002", "채팅 JSON 직렬화 과정에서 문제가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  ;

  private final String code;
  private final String message;
  private final HttpStatus status;
}
