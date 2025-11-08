package com.hackathon.tomolow.domain.user.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hackathon.tomolow.domain.chat.entity.Chat;
import com.hackathon.tomolow.domain.chat.repository.ChatRepository;
import com.hackathon.tomolow.domain.user.dto.response.UserChatResponseDto;
import com.hackathon.tomolow.domain.user.entity.User;
import com.hackathon.tomolow.domain.user.exception.UserErrorCode;
import com.hackathon.tomolow.domain.user.repository.UserRepository;
import com.hackathon.tomolow.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserChatService {

  private final UserRepository userRepository;
  private final ChatRepository chatRepository;

  public List<UserChatResponseDto> getUserChats(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    List<Chat> chats = chatRepository.findByUserId(userId).orElse(null);

    List<UserChatResponseDto> userChatResponseDtos = new ArrayList<>();
    for (Chat chat : chats) {
      UserChatResponseDto userChatResponseDto =
          UserChatResponseDto.builder()
              .question(chat.getQuestion())
              .answer(chat.getAnswer())
              .build();
      userChatResponseDtos.add(userChatResponseDto);
    }

    return userChatResponseDtos;
  }
}
