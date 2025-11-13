package com.hackathon.tomolow.domain.market.dto.response;

import java.math.BigDecimal;

import com.hackathon.tomolow.domain.transaction.entity.TradeType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MarketPendingOrderResponseDto {

  private String orderId;

  private int quantity;

  private TradeType tradeType;

  private String imageUrl;

  private BigDecimal price;
}
