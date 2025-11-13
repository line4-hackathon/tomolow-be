package com.hackathon.tomolow.domain.user.dto.response;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MyPageResponseDto {

  private String nickname;

  private BigDecimal cashBalance;
}
