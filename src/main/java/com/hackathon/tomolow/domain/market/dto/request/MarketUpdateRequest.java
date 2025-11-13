package com.hackathon.tomolow.domain.market.dto.request;

import jakarta.validation.constraints.NotBlank;

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
@Schema(title = "MarketUpdateRequest", description = "종목 수정을 위한 요청")
public class MarketUpdateRequest {

  @NotBlank
  @Schema(description = "수정 대상 심볼", example = "KRW-BTC")
  private String symbol;

  @Schema(description = "새 종목명(옵션)", example = "비트코인")
  private String newName;

  @Schema(description = "새 이미지 URL(옵션)", example = "https://.../btc-v2.png")
  private String newImgUrl;

  @Schema(description = "새 자산 유형(옵션)", example = "CRYPTO")
  private AssetType newAssetType;

  @Schema(description = "새 거래소(옵션)", example = "UPBIT")
  private ExchangeType newExchangeType;

  @Schema(description = "새 심볼(옵션)", example = "KRW-0G")
  private String newSymbol; // 필요할 때만 사용
}
