package com.hackathon.tomolow.domain.market.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.hackathon.tomolow.domain.market.entity.AssetType;
import com.hackathon.tomolow.domain.market.entity.ExchangeType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "MarketCreateRequest", description = "종목 생성을 위한 요청")
public class MarketCreateRequest {

  @NotBlank
  @Schema(description = "종목명", example = "비트코인")
  private String name;

  @NotBlank
  @Schema(description = "심볼(유니크)", example = "KRW-BTC")
  private String symbol;

  @NotNull
  @Schema(description = "자산 유형", example = "CRYPTO")
  private AssetType assetType;

  @NotNull
  @Schema(description = "거래소", example = "UPBIT")
  private ExchangeType exchangeType;

  @Schema(description = "이미지 URL", example = "https://.../btc.png")
  private String imgUrl;
}
