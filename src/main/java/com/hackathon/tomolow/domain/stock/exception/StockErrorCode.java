package com.hackathon.tomolow.domain.stock.exception;

import org.springframework.http.HttpStatus;

import com.hackathon.tomolow.global.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StockErrorCode implements BaseErrorCode {
  STOCK_ALREADY_EXISTS("USER_4001", "이미 존재하는 종목 아이디입니다.", HttpStatus.BAD_REQUEST),
  STOCK_NOT_FOUND("STOCK_4003", "존재하지 않는 종목입니다.", HttpStatus.NOT_FOUND);

  private final String code; // 에러 코드 문자열
  private final String message; // 에러 메시지
  private final HttpStatus status; // HTTP 상태 코드
}
