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
  /** 뉴스 제목 (원문 또는 번역문) */
  private String title;

  /** 언론사 / 출처 이름 */
  private String sourceName;

  /** 뉴스 원문 URL */
  private String url;

  /** 썸네일 이미지 URL */
  private String imageUrl;
}
