package com.hackathon.tomolow.domain.market.dto.response;

import com.hackathon.tomolow.domain.market.entity.AssetType;
import com.hackathon.tomolow.domain.market.entity.ExchangeType;
import com.hackathon.tomolow.domain.market.entity.Market;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "MarketResponse", description = "종목 응답 DTO")
public class MarketResponse {

  @Schema(description = "종목 ID", example = "1")
  private Long id;

  @Schema(description = "종목명", example = "비트코인")
  private String name;

  @Schema(description = "심볼", example = "KRW-BTC")
  private String symbol;

  @Schema(description = "자산 유형", example = "CRYPTO")
  private AssetType assetType;

  @Schema(description = "거래소", example = "UPBIT")
  private ExchangeType exchangeType;

  @Schema(description = "이미지 URL")
  private String imgUrl;

  public static MarketResponse from(Market m) {
    return MarketResponse.builder()
        .id(m.getId())
        .name(m.getName())
        .symbol(m.getSymbol())
        .assetType(m.getAssetType())
        .exchangeType(m.getExchangeType())
        .imgUrl(m.getImgUrl())
        .build();
  }
}
