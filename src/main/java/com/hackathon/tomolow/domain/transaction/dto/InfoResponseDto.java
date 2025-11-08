package com.hackathon.tomolow.domain.transaction.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InfoResponseDto {

  private BigDecimal marketPrice;

  private Long maxQuantity;

  private Long userCashBalance;
}
