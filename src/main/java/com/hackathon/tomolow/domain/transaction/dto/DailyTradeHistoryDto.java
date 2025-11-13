package com.hackathon.tomolow.domain.transaction.dto;

import java.time.LocalDate;
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
@Schema(title = "DailyTradeHistory", description = "하루 단위 거래내역")
public class DailyTradeHistoryDto {

  @Schema(description = "거래일", example = "2025-10-14")
  private LocalDate date;

  @Schema(description = "해당 날짜의 거래 리스트(최신순)")
  private List<TradeHistoryItemDto> items;
}
