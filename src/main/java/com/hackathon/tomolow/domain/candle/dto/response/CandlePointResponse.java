package com.hackathon.tomolow.domain.candle.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "CandlePoint", description = "차트의 한 캔들 데이터")
public class CandlePointResponse {

  @Schema(description = "캔들 시작(KST)")
  private LocalDateTime startTime;

  @Schema(description = "캔들 끝(KST) - 집계 구간의 종료 지점(포함/표시는 클라에서)")
  private LocalDateTime endTime;

  @Schema(description = "시가")
  private BigDecimal open;

  @Schema(description = "고가")
  private BigDecimal high;

  @Schema(description = "저가")
  private BigDecimal low;

  @Schema(description = "종가")
  private BigDecimal close;

  @Schema(description = "거래량(합)")
  private BigDecimal volume;
}
