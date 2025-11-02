package com.hackathon.tomolow.global.exception;

import com.hackathon.tomolow.global.exception.model.BaseErrorCode;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

  private final BaseErrorCode errorCode;

  public CustomException(BaseErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  // 에러 메시지 커스텀
  public CustomException(BaseErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

}
