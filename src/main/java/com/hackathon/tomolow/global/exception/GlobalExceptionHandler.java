package com.hackathon.tomolow.global.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.hackathon.tomolow.global.exception.model.BaseErrorCode;
import com.hackathon.tomolow.global.response.BaseResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // 커스텀 예외
  @ExceptionHandler(CustomException.class)
  public ResponseEntity<BaseResponse<Object>> handleCustomException(CustomException ex) {
    BaseErrorCode errorCode = ex.getErrorCode();
    log.error("Custom 오류 발생: {}", ex.getMessage());
    return ResponseEntity.status(errorCode.getStatus())
        .body(BaseResponse.error(errorCode.getStatus().value(), ex.getMessage()));
  }

  // Validation 실패
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<BaseResponse<Object>> handleValidationException(
      MethodArgumentNotValidException ex) {
    String errorMessages =
        ex.getBindingResult().getFieldErrors().stream()
            .map(e -> String.format("[%s] %s", e.getField(), e.getDefaultMessage()))
            .collect(Collectors.joining(" / "));
    log.warn("Validation 오류 발생: {}", errorMessages);
    return ResponseEntity.badRequest().body(BaseResponse.error(400, errorMessages));
  }

  // 예상치 못한 예외
  @ExceptionHandler(Exception.class)
  public ResponseEntity<BaseResponse<Object>> handleException(Exception ex) {
    log.error("Server 오류 발생: ", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(BaseResponse.error(500, "예상치 못한 서버 오류가 발생했습니다."));
  }

  // + 정적 리소스 404 특화 핸들러
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<?> handleNoResource(NoResourceFoundException ex) {
    String path = ex.getResourcePath();

    // 소스맵(.map)은 개발 편의용이라 없다고 에러로 시끄럽게 하지 않음
    if (path != null && (path.endsWith(".map") || path.contains(".map?"))) {
      log.debug("Ignore missing source map: {}", path);
      // 바디 없이 404만 반환 (콘솔 오염 방지)
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // 그 외 정적 리소스 404는 표준 에러 바디로
    log.warn("Static resource not found: {}", path);
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(BaseResponse.error(404, "요청한 정적 리소스를 찾을 수 없습니다."));
  }
}
