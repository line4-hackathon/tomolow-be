package com.hackathon.tomolow.domain.userMarketHolding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(title = "HoldingStatusResponse", description = "사용자의 특정 종목 보유 여부 응답")
public class HoldingStatusResponse {

  @Schema(description = "해당 종목 보유 여부", example = "true")
  private boolean holding;
}
