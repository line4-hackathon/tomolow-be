package com.hackathon.tomolow.domain.transaction.dto;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "TradeHistoryResponse", description = "기간 내 거래내역 + 손익 요약")
public class TradeHistoryResponse {

  @Schema(description = "기간 내 총 손익 금액 (매도합 - 매수합)", example = "-15000")
  private BigDecimal periodPnlAmount;

  @Schema(description = "기간 내 손익률 (총손익 / 총매수금액)", example = "-0.0245")
  private BigDecimal periodPnlRate;

  @Schema(description = "기간 내 총 매수 금액 합계", example = "154000444")
  private BigDecimal totalBuyAmount;

  @Schema(description = "기간 내 총 매도 금액 합계", example = "153995444")
  private BigDecimal totalSellAmount;

  @Schema(description = "날짜별 거래내역 (최신날짜 순)")
  private List<DailyTradeHistoryDto> days;
}
