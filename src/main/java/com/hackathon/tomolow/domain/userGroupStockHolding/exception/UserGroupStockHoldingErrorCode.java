package com.hackathon.tomolow.domain.userGroupStockHolding.exception;

import org.springframework.http.HttpStatus;

import com.hackathon.tomolow.global.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserGroupStockHoldingErrorCode implements BaseErrorCode {

  // ===== 보유 주식 관련 =====
  ALREADY_HOLDING_STOCK("GROUP_STOCK_HOLDING_4001", "이미 해당 종목을 보유 중입니다.", HttpStatus.BAD_REQUEST),
  INSUFFICIENT_QUANTITY(
      "GROUP_STOCK_HOLDING_4002", "보유 수량보다 많은 수량을 매도할 수 없습니다.", HttpStatus.BAD_REQUEST),
  HOLDING_NOT_FOUND("GROUP_STOCK_HOLDING_4003", "보유 중인 종목을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

  private final String code; // 에러 코드 문자열
  private final String message; // 에러 메시지
  private final HttpStatus status; // HTTP 상태 코드
}
