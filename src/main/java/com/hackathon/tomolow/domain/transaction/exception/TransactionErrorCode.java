package com.hackathon.tomolow.domain.transaction.exception;

import com.hackathon.tomolow.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TransactionErrorCode implements BaseErrorCode {
    PRICE_NOT_EXIST("TRANSACTION_4001", "가격 정보가 존재하지 않습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
