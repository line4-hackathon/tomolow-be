package com.hackathon.tomolow.domain.userInterestedMarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "관심 마켓 목록 응답")
public class InterestedMarketListResponse {

  @Schema(description = "관심 마켓 목록")
  private java.util.List<InterestedMarketCard> items;
}
