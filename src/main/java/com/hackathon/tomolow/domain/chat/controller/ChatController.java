package com.hackathon.tomolow.domain.chat.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon.tomolow.domain.chat.dto.ChatRequestDto;
import com.hackathon.tomolow.domain.chat.dto.ChatResponseDto;
import com.hackathon.tomolow.domain.chat.service.ChatResponseService;
import com.hackathon.tomolow.global.response.BaseResponse;
import com.hackathon.tomolow.global.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatbot")
public class ChatController {

  private final ChatResponseService chatResponseService;

  @PostMapping("/question")
  public ResponseEntity<BaseResponse<?>> getResponse(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @Valid @RequestBody ChatRequestDto chatRequestDto) {
    Long userId = customUserDetails.getUser().getId();
    ChatResponseDto aiResponse = chatResponseService.getAIResponse(userId, chatRequestDto);
    return ResponseEntity.ok(BaseResponse.success(aiResponse));
  }
}
