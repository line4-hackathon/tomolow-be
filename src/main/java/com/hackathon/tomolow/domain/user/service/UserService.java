package com.hackathon.tomolow.domain.user.service;

import com.hackathon.tomolow.domain.user.dto.request.SignUpRequest;
import com.hackathon.tomolow.domain.user.dto.response.SignUpResponse;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.user.exception.UserErrorCode;
import com.hackathon.tomolow.domain.user.mapper.UserMapper;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import com.hackathon.tomolow.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;  // 사용자 DB 접근 객체
  private final PasswordEncoder passwordEncoder; // 비밀번호 암호화를 위한 인코더
  private final UserMapper userMapper; // User → SignUpResponse 변환 매퍼

  @Transactional
  public SignUpResponse signUp(SignUpRequest request) {  // 회원가입 처리 메서드

    // 이미 존재하는 username인지 확인!!
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new CustomException(UserErrorCode.USERNAME_ALREADY_EXISTS);
    }

    // 비밀번호 암호화
    String encodedPassword = passwordEncoder.encode(request.getPassword());

    // 유저 엔티티 생성
    User user = User.builder()
        .username(request.getUsername())
        .password(encodedPassword)
        .name(request.getName())
        .nickname(request.getNickname())   // 필수
        .build();

    // 저장 및 로깅
    User savedUser = userRepository.save(user);
    log.info("New user registered: {}", savedUser.getUsername());

    // 응답 DTO로 변환 후 반환
    return userMapper.toSignUpResponse(savedUser);
  }

}
