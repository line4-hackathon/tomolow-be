package com.hackathon.tomolow.domain.chat.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.hackathon.tomolow.domain.chat.dto.*;
import com.hackathon.tomolow.domain.chat.service.ChatResponseService;
import com.hackathon.tomolow.domain.chat.service.ChatSaveService;
import com.hackathon.tomolow.domain.chat.service.ChatService;
import com.hackathon.tomolow.domain.userMarketHolding.dto.UserMarketHoldingResponseDto;
import com.hackathon.tomolow.domain.userMarketHolding.service.UserMarketHoldingService;
import com.hackathon.tomolow.global.response.BaseResponse;
import com.hackathon.tomolow.global.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatbot")
@Tag(name = "Chat", description = "채팅 (학습) API")
public class ChatController {

  private final ChatResponseService chatResponseService;
  private final ChatService chatService;
  private final ChatSaveService chatSaveService;
  private final UserMarketHoldingService userMarketHoldingService;

  @PostMapping("/question")
  @Operation(summary = "응답 받아오기", description = "질문에 대한 응답을 받아오기 위한 API")
  public ResponseEntity<BaseResponse<?>> getResponse(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @Valid @RequestBody ChatRequestDto chatRequestDto) {
    Long userId = customUserDetails.getUser().getId();
    ChatResponseDto aiResponse = chatResponseService.getAIResponse(userId, chatRequestDto);
    return ResponseEntity.ok(BaseResponse.success(aiResponse));
  }

  @GetMapping("/room")
  @Operation(summary = "채팅방 불러오기", description = "채팅방 내용을 불러오기 위한 API")
  public ResponseEntity<BaseResponse<?>> getRoom(
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    Long userId = customUserDetails.getUser().getId();
    ChatRoomResponseDto chatMessages = chatService.getChatMessages(userId);
    return ResponseEntity.ok(BaseResponse.success(chatMessages));
  }

  @PostMapping("/save")
  @Operation(summary = "채팅 저장하기", description = "채팅 저장을 위한 API")
  public ResponseEntity<BaseResponse<?>> saveChat(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @Valid @RequestBody ChatSaveRequestDto chatSaveRequestDto) {
    Long userId = customUserDetails.getUser().getId();

    chatSaveService.saveChat(chatSaveRequestDto, userId);
    return ResponseEntity.ok(BaseResponse.success(null));
  }

  @GetMapping("/market/holding")
  @Operation(summary = "보유 주식 조회하기", description = "보유 주식 조회를 위한 API")
  public ResponseEntity<BaseResponse<?>> getMarketOwn(
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    Long userId = customUserDetails.getUser().getId();
    List<UserMarketHoldingResponseDto> userMarketHoldings =
        userMarketHoldingService.getUserMarketHoldings(userId);
    return ResponseEntity.ok(BaseResponse.success(userMarketHoldings));
  }
}
