package com.hackathon.tomolow.domain.userGroupTransaction.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupInfoResponseDto {

  private BigDecimal marketPrice;

  private Long maxQuantity;

  private Long userGroupCashBalance;
}
