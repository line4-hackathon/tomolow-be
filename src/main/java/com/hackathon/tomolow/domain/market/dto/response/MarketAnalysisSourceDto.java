package com.hackathon.tomolow.domain.market.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketAnalysisSourceDto {
  private String url;
  private String imageUrl;
}
