package com.hackathon.tomolow.domain.market.dto.request;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MarketAnalysisRequestDto {

  /** 현재 가격 */
  private BigDecimal currentPrice;

  /** 어제 종가 */
  private BigDecimal previousClosePrice;
}
