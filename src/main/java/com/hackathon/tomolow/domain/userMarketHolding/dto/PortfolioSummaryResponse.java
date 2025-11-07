package com.hackathon.tomolow.domain.userMarketHolding.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioSummaryResponse {

  private BigDecimal totalInvestment; // 투자자산
  private BigDecimal cashBalance; // 현금자산
  private BigDecimal totalCurrentValue; // 전체자산
  private BigDecimal totalPnlAmount; // 총손익원 (실시간 데이터)
  private BigDecimal totalPnlRate; // 총손익률 = 총손익원 / 총매입금액 (실시간 데이터)
}
