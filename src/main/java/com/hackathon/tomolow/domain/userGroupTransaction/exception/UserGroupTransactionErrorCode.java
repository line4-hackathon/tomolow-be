package com.hackathon.tomolow.domain.userGroupTransaction.exception;

import org.springframework.http.HttpStatus;

import com.hackathon.tomolow.global.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserGroupTransactionErrorCode implements BaseErrorCode {

  // ===== 거래 관련 =====
  INSUFFICIENT_BALANCE("GROUP_TX_4001", "잔액이 부족합니다.", HttpStatus.BAD_REQUEST),
  INSUFFICIENT_MARKET_QUANTITY(
      "GROUP_TX_4002", "보유 종목 수량보다 많은 양을 매도할 수 없습니다.", HttpStatus.BAD_REQUEST),
  INVALID_TRADE_TYPE(
      "GROUP_TX_4003", "유효하지 않은 거래 타입입니다. (BUY 또는 SELL이어야 합니다.)", HttpStatus.BAD_REQUEST),
  TRANSACTION_NOT_FOUND("GROUP_TX_4004", "해당 거래 내역을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  USERGROUP_NOT_FOUND("GROUP_TX_4005", "그룹 내 사용자 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  MARKET_NOT_FOUND("GROUP_TX_4006", "해당 마켓 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  GROUP_INACTIVE("GROUP_TX_4007", "그룹이 모집 중이거나 종료된 상태입니다.", HttpStatus.BAD_REQUEST),
  PENDING_ORDER_NOT_EXIST("GROUP_TX_4008", "해당 대기주문을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  TRADE_TYPE_NULL("GROUP_TX_4009", "trade type이 null입니다.", HttpStatus.NOT_FOUND);

  private final String code; // 에러 코드 문자열
  private final String message; // 에러 메시지
  private final HttpStatus status; // HTTP 상태 코드
}
