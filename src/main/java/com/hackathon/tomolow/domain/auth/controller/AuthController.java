package com.hackathon.tomolow.domain.auth.controller;

import java.time.Duration;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.tomolow.domain.auth.dto.request.LoginRequest;
import com.hackathon.tomolow.domain.auth.dto.response.LoginResponse;
import com.hackathon.tomolow.domain.auth.service.AuthService;
import com.hackathon.tomolow.domain.user.dto.request.SignUpRequest;
import com.hackathon.tomolow.domain.user.dto.response.SignUpResponse;
import com.hackathon.tomolow.domain.user.exception.UserErrorCode;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import com.hackathon.tomolow.global.exception.CustomException;
import com.hackathon.tomolow.global.response.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Auth 관련 API")
public class AuthController {

  private final AuthService authService;
  private final UserRepository userRepository;

  @Operation(summary = "사용자 로그인", description = "사용자 로그인을 위한 API")
  @PostMapping("/login")
  public ResponseEntity<BaseResponse<LoginResponse>> login(
      @RequestBody @Valid LoginRequest loginRequest, HttpServletResponse response) {
    // 로그인 처리 서비스 호출
    LoginResponse loginResponse = authService.login(loginRequest);

    // 사용자로부터 refreshToken 가져오기
    String refreshToken =
        userRepository
            .findByUsername(loginRequest.getUsername())
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND)) // 사용자 없으면 예외
            .getRefreshToken();

    // Set-Cookie 설정 (HttpOnly + Secure)
    // Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
    // ✅ ResponseCookie 로 SameSite=None; Secure; HttpOnly; Path=/; Max-Age=7d 설정
    ResponseCookie cookie =
        ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true)
            .secure(true) // SameSite=None일 땐 반드시 Secure
            .sameSite("None") // ⬅️ 핵심
            .path("/")
            .maxAge(Duration.ofDays(7))
            .build();

    // refreshTokenCookie.setHttpOnly(true); // JavaScript로 접근 불가능
    // refreshTokenCookie.setSecure(true); // HTTPS 환경에서만 전송
    // refreshTokenCookie.setPath("/"); // 모든 경로에 대해 유효
    // refreshTokenCookie.setMaxAge(60 * 60 * 24 * 7); // 7일간 유지

    // 응답에 쿠키 추가
    // response.addCookie(refreshTokenCookie);

    // 응답 헤더에 쿠키 추가
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    // 로그인 결과 응답 반환
    return ResponseEntity.ok(BaseResponse.success("로그인에 성공했습니다.", loginResponse));
  }

  @Operation(summary = "회원가입 API", description = "사용자 회원가입을 위한 API")
  @PostMapping("/sign-up")
  public ResponseEntity<BaseResponse<SignUpResponse>> signUp(
      @RequestBody @Valid SignUpRequest signUpRequest) {
    System.out.println(signUpRequest.getUsername() + ", " + signUpRequest.getPassword());
    SignUpResponse signUpResponse = authService.signUp(signUpRequest);
    return ResponseEntity.ok(BaseResponse.success("회원가입에 성공했습니다.", signUpResponse));
  }
}
