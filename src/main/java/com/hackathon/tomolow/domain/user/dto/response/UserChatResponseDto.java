package com.hackathon.tomolow.domain.user.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserChatResponseDto {

  private String question;

  private String answer;
}
