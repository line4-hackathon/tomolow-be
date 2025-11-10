package com.hackathon.tomolow.domain.userGroup.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserGroupRankingDto {

  private int ranking;

  private UserPnLDto userPnl;

  @Data
  @Builder
  public static class UserPnLDto {

    private Long userId;

    private String nickName;

    private BigDecimal pnL;
  }
}
