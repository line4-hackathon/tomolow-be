package com.hackathon.tomolow.global.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.user.exception.UserErrorCode;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

  private final UserRepository userRepository;

  public Long getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth == null
        || auth.getPrincipal() == null
        || auth.getPrincipal().equals("anonymousUser")) {
      return null;
    }

    String username = auth.getName();

    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(
                () -> new CustomException(UserErrorCode.USER_NOT_FOUND, "해당 id의 사용자가 존재하지 않습니다."));

    return user.getId();
  }
}
