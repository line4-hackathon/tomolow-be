package com.hackathon.tomolow.domain.chat.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatResponseDto {

  private String key;

  private String answer;

  private List<AIChatResponseDto.NewsSource> sources;
}
