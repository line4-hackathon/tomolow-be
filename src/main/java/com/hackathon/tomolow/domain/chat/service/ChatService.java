package com.hackathon.tomolow.domain.chat.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.tomolow.domain.chat.dto.ChatRedisSaveDto;
import com.hackathon.tomolow.domain.chat.dto.ChatRoomResponseDto;
import com.hackathon.tomolow.domain.chat.exception.ChatErrorCode;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.user.exception.UserErrorCode;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import com.hackathon.tomolow.global.exception.CustomException;
import com.hackathon.tomolow.global.redis.RedisUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

  private final RedisUtil redisUtil;
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private final UserRepository userRepository;

  /** 사용자 채팅방 내 채팅 조회하기 */
  public ChatRoomResponseDto getChatMessages(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    List<String> keys = redisUtil.getList("CHAT_KEYS:" + userId);

    List<ChatRedisSaveDto> messages = new ArrayList<>();
    for (String key : keys) {
      ChatRedisSaveDto chatQnA = getChatQnA(key);
      if (chatQnA == null) continue;
      messages.add(getChatQnA(key));
    }

    return ChatRoomResponseDto.builder().messages(messages).nickname(user.getNickname()).build();
  }

  /** Redis에서 조회한 json 데이터 역직렬화 */
  public ChatRedisSaveDto getChatQnA(String key) {
    String json = redisUtil.getData(key);
    if (json == null) return null;
    try {
      return objectMapper.readValue(json, ChatRedisSaveDto.class);
    } catch (Exception e) {
      log.error("역직렬화 실패 - " + e.getMessage());
      throw new CustomException(ChatErrorCode.JSON_MAPPING_ERROR, "JSON 역직렬화 과정에서 문제가 발생했습니다.");
    }
  }
}
