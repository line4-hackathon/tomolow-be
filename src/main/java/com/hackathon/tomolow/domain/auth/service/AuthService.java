package com.hackathon.tomolow.domain.auth.service;

import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackathon.tomolow.domain.auth.dto.request.LoginRequest;
import com.hackathon.tomolow.domain.auth.dto.response.LoginResponse;
import com.hackathon.tomolow.domain.auth.mapper.AuthMapper;
import com.hackathon.tomolow.domain.user.dto.request.SignUpRequest;
import com.hackathon.tomolow.domain.user.dto.response.SignUpResponse;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.user.exception.UserErrorCode;
import com.hackathon.tomolow.domain.user.mapper.UserMapper;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import com.hackathon.tomolow.global.exception.CustomException;
import com.hackathon.tomolow.global.jwt.JwtProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final JwtProvider jwtProvider;
  private final UserRepository userRepository;
  private final AuthMapper authMapper; // 응답 DTO 매핑용 매퍼
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper; // User → SignUpResponse 변환 매퍼

  @Transactional
  public LoginResponse login(LoginRequest loginRequest) {
    // 사용자 조회 (없으면 예외)
    User user =
        userRepository
            .findByUsername(loginRequest.getUsername())
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    // 사용자 인증 토큰 생성 (username, password 기반)
    UsernamePasswordAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(
            loginRequest.getUsername(), loginRequest.getPassword());

    // 실제 인증 수행
    authenticationManager.authenticate(authenticationToken);

    // 액세스 토큰 및 리프레시 토큰 생성
    String accessToken = jwtProvider.createAccessToken(user.getUsername());
    String refreshToken =
        jwtProvider.createRefreshToken(
            user.getUsername(), UUID.randomUUID().toString()); // 리프레시 토큰은 랜덤값 추가로 보안 강화

    // 리프레시 토큰을 User 엔티티에 저장
    user.createRefreshToken(refreshToken);

    // 액세스 토큰 만료 시간 계산
    Long expirationTime = jwtProvider.getExpiration(accessToken);

    // 로그인 성공 로그 출력
    log.info("로그인 성공: {}", user.getUsername());

    // 응답 객체로 변환하여 반환
    return authMapper.toLoginResponse(user, accessToken, expirationTime);
  }

  @Transactional
  public SignUpResponse signUp(SignUpRequest request) { // 회원가입 처리 메서드

    // 이미 존재하는 username인지 확인!!
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new CustomException(UserErrorCode.USERNAME_ALREADY_EXISTS);
    }

    // 비밀번호 암호화
    String encodedPassword = passwordEncoder.encode(request.getPassword());

    // 유저 엔티티 생성
    User user =
        User.builder()
            .username(request.getUsername())
            .password(encodedPassword)
            .name(request.getName())
            .nickname(request.getNickname()) // 필수
            .build();

    // 저장 및 로깅
    User savedUser = userRepository.save(user);
    log.info("New user registered: {}", savedUser.getUsername());

    // 응답 DTO로 변환 후 반환
    return userMapper.toSignUpResponse(savedUser);
  }
}
