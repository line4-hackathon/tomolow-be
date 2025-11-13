package com.hackathon.tomolow.domain.userGroupTransaction.dto;

import com.hackathon.tomolow.domain.transaction.entity.TradeType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserGroupPendingOrderDto {

  private String orderId;

  private Long marketId;

  private String marketName;

  private String marketSymbol;

  private int quantity;

  private TradeType tradeType;

  private String imageUrl;
}
