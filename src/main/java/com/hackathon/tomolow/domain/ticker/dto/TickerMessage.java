package com.hackathon.tomolow.domain.ticker.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TickerMessage {

  private String market; // e.g. BTC-KRW (ex.비트코인)
  private BigDecimal tradePrice; // 현재가
  private BigDecimal changeRate; // 전일대비 등락률 (예: 0.0123 = +1.23%)
  private BigDecimal accVolume; // 누적 거래량(24h)
  private long tradeTimestamp; // 틱 시각 (ms)
}
