package com.hackathon.tomolow.domain.userGroupStockHolding.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserGroupMarketHoldingPnLDto {

  private List<SinglePnLDto> pnLDtos;

  @Data
  @Builder
  public static class SinglePnLDto {
    private Long marketId;

    private String marketName;

    private BigDecimal pnL;

    private BigDecimal pnLRate;
  }
}
