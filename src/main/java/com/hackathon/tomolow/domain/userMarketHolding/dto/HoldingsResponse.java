package com.hackathon.tomolow.domain.userMarketHolding.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HoldingsResponse {

  private List<HoldingItemResponse> items;
  private PortfolioSummaryResponse portfolio;
}
