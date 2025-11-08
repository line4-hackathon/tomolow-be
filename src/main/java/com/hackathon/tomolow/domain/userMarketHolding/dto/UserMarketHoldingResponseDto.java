package com.hackathon.tomolow.domain.userMarketHolding.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "사용자 보유 주식")
public class UserMarketHoldingResponseDto {

  @Schema(description = "마켓 심볼", example = "KRW-BTC")
  private String symbol;

  @Schema(description = "마켓 이름", example = "비트코인")
  private String name;

  @Schema(description = "마켓 이미지 URL")
  private String imageUrl;

  @Schema(description = "현재가")
  private BigDecimal price;

  @Schema(description = "전일 대비 등락률 (0.0123 = +1.23%)")
  private BigDecimal changeRate;

  @Schema(description = "관심등록 여부", example = "true")
  private Boolean interested;
}
