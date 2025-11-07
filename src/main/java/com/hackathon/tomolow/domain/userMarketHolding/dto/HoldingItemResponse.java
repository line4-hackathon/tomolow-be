package com.hackathon.tomolow.domain.userMarketHolding.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HoldingItemResponse {

  private Long marketId;
  private String symbol; // KRW-BTC
  private String name; // 비트코인
  private String imageUrl; // 종목 이미지(없으면 null)
  private long quantity; // 보유수량
  private BigDecimal avgPrice; // 평균단가
  private BigDecimal currentPrice; // 현재가(레디스)
  private BigDecimal pnlAmount; // 손익원 = (현재가-평단)*수량
  private BigDecimal pnlRate; // 손익률 = 손익원 / (평단*수량)
}
