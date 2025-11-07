package com.hackathon.tomolow.domain.candle.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // ✅ 모르는 필드 무시 (market 외 여분 필드 방어)
@Schema(title = "UpbitDayCandle DTO", description = "업비트 REST API로부터 받아오는 일봉(1D) 데이터")
public class UpbitDayCandle {

  @Schema(description = "마켓 코드", example = "KRW-BTC")
  @JsonProperty("market") // 에러의 직접 원인 필드 추가
  private String market;

  @Schema(description = "KST 기준 캔들 시간", example = "2025-11-05T00:00:00")
  @JsonProperty("candle_date_time_kst")
  private String candleDateTimeKst;

  @Schema(description = "시가 (Opening price)", example = "115000.0")
  @JsonProperty("opening_price")
  private BigDecimal openingPrice;

  @Schema(description = "고가 (Highest price)", example = "118000.0")
  @JsonProperty("high_price")
  private BigDecimal highPrice;

  @Schema(description = "저가 (Lowest price)", example = "113000.0")
  @JsonProperty("low_price")
  private BigDecimal lowPrice;

  @Schema(description = "종가 (Closing price)", example = "117000.0")
  @JsonProperty("trade_price")
  private BigDecimal tradePrice;

  @Schema(description = "누적 거래량 (수량 기준)", example = "1532.45")
  @JsonProperty("candle_acc_trade_volume")
  private BigDecimal accTradeVolume;

  @Schema(description = "누적 거래대금 (원화 기준)", example = "176204000.0")
  @JsonProperty("candle_acc_trade_price")
  private BigDecimal accTradePrice;
}
