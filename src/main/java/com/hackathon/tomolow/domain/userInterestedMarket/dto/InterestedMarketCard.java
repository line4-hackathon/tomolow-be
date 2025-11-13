package com.hackathon.tomolow.domain.userInterestedMarket.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "관심 마켓 카드", description = "관심 목록에 노출되는 간단 카드 정보")
public class InterestedMarketCard {

  @Schema(description = "마켓 ID", example = "2")
  private Long marketId;

  @Schema(description = "심볼 (코드)", example = "KRW-ETH")
  private String symbol;

  @Schema(description = "마켓 이름", example = "이더리움")
  private String name;

  @Schema(description = "마켓 이미지 URL", example = "https://.../eth.png")
  private String imageUrl;

  @Schema(description = "현재가", example = "87000")
  private BigDecimal price; // ✅ 추가

  @Schema(description = "등락률 (소수, 예: 0.105는 +10.5%)", example = "0.105")
  private BigDecimal changeRate; // ✅ 추가
}
