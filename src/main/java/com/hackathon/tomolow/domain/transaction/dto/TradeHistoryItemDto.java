package com.hackathon.tomolow.domain.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.hackathon.tomolow.domain.transaction.entity.TradeType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "TradeHistoryItem", description = "기간 내 개별 체결 내역")
public class TradeHistoryItemDto {

  @Schema(description = "체결 시각", example = "2025-10-14T17:21:00")
  private LocalDateTime tradedAt;

  @Schema(description = "종목명", example = "삼성전자")
  private String name;

  @Schema(description = "심볼", example = "KRW-BTC")
  private String symbol;

  @Schema(description = "체결 가격", example = "97000")
  private BigDecimal price;

  @Schema(description = "체결 수량", example = "3")
  private int quantity;

  @Schema(description = "매수/매도", example = "BUY")
  private TradeType tradeType;

  @Schema(description = "체결 금액(가격 * 수량)", example = "291000")
  private BigDecimal amount;
}
