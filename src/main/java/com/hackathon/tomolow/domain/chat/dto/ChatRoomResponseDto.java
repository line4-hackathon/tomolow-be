package com.hackathon.tomolow.domain.chat.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatRoomResponseDto {

  private String nickname;

  private List<ChatRedisSaveDto> messages;
}
