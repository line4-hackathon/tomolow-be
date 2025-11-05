package com.hackathon.tomolow.domain.userInterestedStock.exception;

import org.springframework.http.HttpStatus;

import com.hackathon.tomolow.global.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserInterestedStockErrorCode implements BaseErrorCode {
  ALREADY_INTERESTED("USER_INTERESTED_STOCK_4003", "이미 관심 등록된 종목입니다.", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
