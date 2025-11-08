package com.hackathon.tomolow.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRedisSaveDto {
  private String key;

  private String question;

  private String answer;
}
