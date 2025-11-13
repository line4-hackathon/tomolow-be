package com.hackathon.tomolow.domain.market.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketAnalysisResponseDto {

  /** 1. 뉴스 요약 + 2. 현재 상승/하락 요인 + 3. 앞으로의 요인 이 3가지를 합친 본문 */
  private String analysis;

  /** 분석에 실제로 사용된 뉴스들 (url, image_url) */
  private List<MarketAnalysisSourceDto> sources;
}
