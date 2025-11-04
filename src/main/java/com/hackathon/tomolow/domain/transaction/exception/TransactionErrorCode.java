package com.hackathon.tomolow.domain.transaction.exception;

import org.springframework.http.HttpStatus;

import com.hackathon.tomolow.global.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionErrorCode implements BaseErrorCode {
  PRICE_NOT_EXIST("TRANSACTION_4001", "가격 정보가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
  INSUFFICIENT_BALANCE("TRANSACTION_4002", "잔액이 부족합니다.", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus status;
}
