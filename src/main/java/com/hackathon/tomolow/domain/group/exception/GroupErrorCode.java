package com.hackathon.tomolow.domain.group.exception;

import org.springframework.http.HttpStatus;

import com.hackathon.tomolow.global.exception.model.BaseErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GroupErrorCode implements BaseErrorCode {
  GROUP_NOT_FOUND("GROUP_4001", "존재하지 않는 그룹입니다.", HttpStatus.NOT_FOUND),
  GROUP_CODE_DUPLICATED("GROUP_4002", "이미 존재하는 그룹 코드입니다.", HttpStatus.BAD_REQUEST),
  GROUP_NAME_DUPLICATED("GROUP_4003", "이미 존재하는 그룹명입니다.", HttpStatus.BAD_REQUEST),
  GROUP_MEMBER_LIMIT_EXCEEDED("GROUP_4004", "그룹의 최대 인원 수를 초과했습니다.", HttpStatus.BAD_REQUEST),
  GROUP_ALREADY_JOINED("GROUP_4005", "이미 해당 그룹에 참여 중입니다.", HttpStatus.BAD_REQUEST),
  GROUP_PERMISSION_DENIED("GROUP_4006", "그룹에 대한 권한이 없습니다.", HttpStatus.FORBIDDEN),
  GROUP_INSUFFICIENT_BALANCE("GROUP_4007", "잔액이 부족하여 그룹에 참여할 수 없습니다.", HttpStatus.BAD_REQUEST);

  private final String code; // 에러 코드 문자열
  private final String message; // 에러 메시지
  private final HttpStatus status; // HTTP 상태 코드
}
