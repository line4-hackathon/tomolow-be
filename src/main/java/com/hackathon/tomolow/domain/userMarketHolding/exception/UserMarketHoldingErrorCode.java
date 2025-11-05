package com.hackathon.tomolow.domain.userMarketHolding.exception;

import org.springframework.http.HttpStatus;

import com.hackathon.tomolow.global.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserMarketHoldingErrorCode implements BaseErrorCode {
  INSUFFICIENT_QUANTITY("USER_MARKET_4001", "보유 수량보다 많은 수량을 매도할 수 없습니다.", HttpStatus.BAD_REQUEST),
  HOLDING_NOT_FOUND("USER_MARKET_4002", "해당 주식 보유 내역을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
