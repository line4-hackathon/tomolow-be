package com.hackathon.tomolow.domain.portfilio.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioPnlMessage {

  private BigDecimal totalPnlAmount; // 총 손익(원)
  private BigDecimal totalPnlRate; // 총 손익률(가중)
  private long ts; // 서버 계산 시각(ms)
}
