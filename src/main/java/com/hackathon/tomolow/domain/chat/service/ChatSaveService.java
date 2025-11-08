package com.hackathon.tomolow.domain.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hackathon.tomolow.domain.chat.dto.ChatRedisSaveDto;
import com.hackathon.tomolow.domain.chat.dto.ChatSaveRequestDto;
import com.hackathon.tomolow.domain.chat.entity.Chat;
import com.hackathon.tomolow.domain.chat.repository.ChatRepository;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.user.exception.UserErrorCode;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatSaveService {

  private final ChatRepository chatRepository;
  private final UserRepository userRepository;
  private final ChatService chatService;

  @Transactional
  public void saveChat(ChatSaveRequestDto chatSaveRequestDto, Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    List<String> keys = chatSaveRequestDto.getKeys();

    for (String key : keys) {
      // 이미 DB에 해당 키의 채팅이 저장되어있는지 확인
      if (chatRepository.existsByUserAndKey(user, key)) continue;

      // Redis에서 채팅 조회
      ChatRedisSaveDto chatQnA = chatService.getChatQnA(key);
      String question = chatQnA.getQuestion();
      String answer = chatQnA.getAnswer();

      // 채팅 저장
      Chat chat = Chat.builder().question(question).answer(answer).key(key).user(user).build();
      chatRepository.save(chat);
    }
  }
}
