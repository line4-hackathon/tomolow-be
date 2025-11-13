package com.hackathon.tomolow.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.tomolow.domain.user.dto.response.MyPageResponseDto;
import com.hackathon.tomolow.domain.user.service.MyPageService;
import com.hackathon.tomolow.global.response.BaseResponse;
import com.hackathon.tomolow.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
@Tag(name = "MyPage", description = "마이페이지 조회 API")
public class MyPageController {

  private final MyPageService myPageService;

  @GetMapping
  public ResponseEntity<BaseResponse<?>> getMyPage(
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    Long userId = customUserDetails.getUser().getId();
    MyPageResponseDto myPage = myPageService.getMyPage(userId);
    return ResponseEntity.ok(BaseResponse.success(myPage));
  }
}
