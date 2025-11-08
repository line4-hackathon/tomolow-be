package com.hackathon.tomolow.domain.userInterestedMarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "관심 토글 결과", description = "관심 등록/해제 요청의 처리 결과")
public class InterestToggleResponse {

  @Schema(description = "관심 상태 (true=관심 등록됨)", example = "true")
  private boolean interested;

  @Schema(description = "대상 마켓 ID", example = "12")
  private Long marketId;
}
