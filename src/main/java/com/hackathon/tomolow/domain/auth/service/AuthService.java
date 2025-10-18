package com.hackathon.tomolow.domain.auth.service;

import com.hackathon.tomolow.domain.auth.dto.request.LoginRequest;
import com.hackathon.tomolow.domain.auth.dto.response.LoginResponse;
import com.hackathon.tomolow.domain.auth.mapper.AuthMapper;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.user.exception.UserErrorCode;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import com.hackathon.tomolow.global.exception.CustomException;
import com.hackathon.tomolow.global.jwt.JwtProvider;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service // Spring의 Service 계층으로 등록
@Slf4j // Lombok: 로그 사용 가능하게 함 (log.info 등)
@RequiredArgsConstructor // final 필드들을 매개변수로 받는 생성자 자동 생성
public class AuthService {

  private final AuthenticationManager authenticationManager; // Spring Security 인증 처리
  private final JwtProvider jwtProvider; // JWT 생성 및 검증 유틸
  private final UserRepository userRepository; // 사용자 DB 접근
  private final AuthMapper authMapper; // 응답 DTO 매핑용 매퍼

  @Transactional
  public LoginResponse login(LoginRequest loginRequest) {
    // 사용자 조회 (없으면 예외)
    User user = userRepository.findByUsername(loginRequest.getUsername())
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    // 사용자 인증 토큰 생성 (username, password 기반)
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(
            loginRequest.getUsername(),
            loginRequest.getPassword()
        );

    // 실제 인증 수행
    authenticationManager.authenticate(authenticationToken);

    // 액세스 토큰 및 리프레시 토큰 생성
    String accessToken = jwtProvider.createAccessToken(user.getUsername());
    String refreshToken = jwtProvider.createRefreshToken(user.getUsername(),
        UUID.randomUUID().toString()); // 리프레시 토큰은 랜덤값 추가로 보안 강화

    // 리프레시 토큰을 User 엔티티에 저장
    user.createRefreshToken(refreshToken);

    // 액세스 토큰 만료 시간 계산
    Long expirationTime = jwtProvider.getExpiration(accessToken);

    // 로그인 성공 로그 출력
    log.info("로그인 성공: {}", user.getUsername());

    // 응답 객체로 변환하여 반환
    return authMapper.toLoginResponse(user, accessToken, expirationTime);
  }
}
