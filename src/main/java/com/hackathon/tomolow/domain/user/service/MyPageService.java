package com.hackathon.tomolow.domain.user.service;

import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import com.hackathon.tomolow.domain.user.dto.response.MyPageResponseDto;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.user.exception.UserErrorCode;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyPageService {

  private final UserRepository userRepository;

  public MyPageResponseDto getMyPage(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    return MyPageResponseDto.builder()
        .cashBalance(user.getCashBalance().setScale(0, RoundingMode.DOWN))
        .nickname(user.getNickname())
        .build();
  }
}
