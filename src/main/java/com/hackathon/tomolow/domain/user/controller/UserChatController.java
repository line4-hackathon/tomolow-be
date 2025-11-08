package com.hackathon.tomolow.domain.user.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.tomolow.domain.user.dto.response.UserChatResponseDto;
import com.hackathon.tomolow.domain.user.service.UserChatService;
import com.hackathon.tomolow.global.response.BaseResponse;
import com.hackathon.tomolow.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
@Tag(name = "UserChat", description = "저장된 채팅 조회 API")
public class UserChatController {

  private final UserChatService userChatService;

  @GetMapping("/saved-chat")
  @Operation(summary = "채팅 조회", description = "저장된 채팅 조회")
  public ResponseEntity<BaseResponse<?>> getSavedChats(
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    Long userId = customUserDetails.getUser().getId();

    List<UserChatResponseDto> userChats = userChatService.getUserChats(userId);

    return ResponseEntity.ok(BaseResponse.success(userChats));
  }
}
